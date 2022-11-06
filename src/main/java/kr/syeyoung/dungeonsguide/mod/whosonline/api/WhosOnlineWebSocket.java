package kr.syeyoung.dungeonsguide.mod.whosonline.api;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import kr.syeyoung.dungeonsguide.mod.stomp.StompClient;
import kr.syeyoung.dungeonsguide.mod.whosonline.WhosOnlineManager;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.AbstractMessage;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.MessageParser;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.client.C00Connect;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.client.C06Ping;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.server.S01ConnectAck;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.server.S03IsOnlineAck;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.server.S05areOnlineAck;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.server.S07Pong;
import lombok.Getter;
import lombok.val;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

public class WhosOnlineWebSocket extends WebSocketClient {

    private final Logger logger = LogManager.getLogger("WhosOnlineWebSocket");
    private final String playerUuid;
    private final ConcurrentHashMap<String, Tuple<Long, CountDownLatch>> sentMessages = new ConcurrentHashMap<>();
    private final ScheduledExecutorService se;
    @Getter
    private StompClient.StompClientStatus status = StompClient.StompClientStatus.CONNECTING;
    private boolean sendPingLock;
    volatile long lastPong = -1;
    @Getter
    private long ping;
    private ScheduledFuture<?> heartbeat = null;

    private ScheduledFuture<?> update = null;

    public WhosOnlineWebSocket(final String remote, ScheduledExecutorService se, final String playerUuid) {
        super(URI.create(remote));
        this.playerUuid = playerUuid;

        setConnectionLostTimeout(0);

        this.se = se;
    }

    void update() {
        // clear TimedOut Requests
        for (Iterator<Map.Entry<String, Tuple<Long, CountDownLatch>>> iterator = sentMessages.entrySet().iterator(); iterator.hasNext(); ) {
            val stringTupleEntry = iterator.next();
            long whenToTimeout = stringTupleEntry.getValue().getFirst();
            if (whenToTimeout < System.currentTimeMillis()) {
                iterator.remove();
            }
        }

        // check for timeout
        if (lastPong != -1 && System.currentTimeMillis() - lastPong > (10 * 1000)) {
            this.close();
            logger.info("Channel timed out");
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.info("WebSocket opened");

        send(WhosOnlineManager.gson.toJson(new C00Connect(playerUuid)));

        update = se.scheduleAtFixedRate(this::update, 5, 20, TimeUnit.MILLISECONDS);

        heartbeat = se.scheduleAtFixedRate(() -> {
            if (sendPingLock) {
                sendPingLock = false;
                send(WhosOnlineManager.gson.toJson(new C06Ping(System.currentTimeMillis())));
            }
        }, 1, 3, TimeUnit.SECONDS);
    }

    @Override
    public void onMessage(String message) {

        AbstractMessage res = MessageParser.parse(message);

        if(res == null){
            logger.error("failed to parse message: {}", message);
            return;
        }

        if (res instanceof S01ConnectAck) {
            if (status == StompClient.StompClientStatus.CONNECTING) {
                status = StompClient.StompClientStatus.CONNECTED;
                logger.info("Connection established");
            } else {
                logger.info("Received unexpected \"connected\" ");
            }
        } else if (res instanceof S03IsOnlineAck) {
            S03IsOnlineAck c = (S03IsOnlineAck) res;

            WhosOnlineCache.onlineppl.put(c.uuid, c.is_online);

            releaseAsyncGet(c.nonce);
        } else if (res instanceof S05areOnlineAck) {
            S05areOnlineAck c = (S05areOnlineAck) res;

            val entries = c.getUsers();

            WhosOnlineCache.onlineppl.putAll(entries);

            releaseAsyncGet(c.getNonce());
        } else if (res instanceof S07Pong) {
            S07Pong c = (S07Pong) res;

            long started = c.getC();
            this.ping = System.currentTimeMillis() - started;
            this.lastPong = System.currentTimeMillis();
            this.sendPingLock = true;
        } else {
            logger.error("failed to parse message: {}", message);
        }

    }

    void releaseAsyncGet(String id) {
        if (id == null) return;


        Tuple<Long, CountDownLatch> longCountDownLatchTuple = sentMessages.get(id);
        if (longCountDownLatchTuple != null) {
            longCountDownLatchTuple.getSecond().countDown();
        }
        sentMessages.remove(id);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if(update != null) update.cancel(true);
        if (heartbeat != null) heartbeat.cancel(true);
        logger.info("Websocket closed code: {}  reason: {}", code, reason);
        status = StompClient.StompClientStatus.DISCONNECTED;
        MinecraftForge.EVENT_BUS.post(new WhosOnlineManager.WhosOnlineDied());
    }

    @Override
    public void onError(Exception ex) {
        logger.error("execution in websocket");
        logger.error(ex);
    }

    @Override
    public void onWebsocketPong(WebSocket conn, Framedata f) {
        logger.info("received a pong");
    }


    @Override
    public void onWebsocketPing(WebSocket conn, Framedata f) {
        logger.info("recived a ping");
    }

    public void sendAndBlock(String message, String id, long timeout) {
        val c = new CountDownLatch(1);
        logger.info("Sending \"{}\"", message);
        sentMessages.put(id, new Tuple<>(timeout, c));
        this.send(message);

        try {
            c.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

}

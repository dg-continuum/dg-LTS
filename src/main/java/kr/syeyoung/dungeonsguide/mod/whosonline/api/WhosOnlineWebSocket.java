package kr.syeyoung.dungeonsguide.mod.whosonline.api;

import kr.syeyoung.dungeonsguide.mod.stomp.StompClient;
import kr.syeyoung.dungeonsguide.mod.utils.SimpleFuse;
import kr.syeyoung.dungeonsguide.mod.whosonline.WhosOnlineManager;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.AbstractMessage;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.MessageParser;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.client.C00Connect;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.client.C03Ping;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.server.S00ConnectAck;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.server.S01IsOnlineAck;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.server.S02areOnlineAck;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.server.S04Pong;
import lombok.Getter;
import lombok.val;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

public class WhosOnlineWebSocket extends WebSocketClient {

    private final Logger logger = LogManager.getLogger("WhosOnlineWebSocket");
    private final String playerUuid;
    private final ConcurrentHashMap<String, Tuple<Long, CountDownLatch>> sentMessages;
    private final ScheduledExecutorService se;
    private final SimpleFuse timeoutFuse;
    long lastPong = -1;
    @Getter
    private StompClient.StompClientStatus status;
    @Getter
    private long ping;

    private ScheduledFuture<?> update = null;
    private long nextPing;
    private WhosOnlineCache cache;

    public WhosOnlineWebSocket(@NotNull final String remote,
                               @NotNull final ScheduledExecutorService se,
                               WhosOnlineCache cache,
                               @NotNull final String playerUuid) {
        super(URI.create(remote));
        this.cache = cache;
        setConnectionLostTimeout(0);


        this.playerUuid = playerUuid;
        this.se = se;
        status = StompClient.StompClientStatus.CONNECTING;
        timeoutFuse = new SimpleFuse();
        sentMessages = new ConcurrentHashMap<>();
    }

    void update() {
        // clear TimedOut'ed  Requests
        for (Iterator<Map.Entry<String, Tuple<Long, CountDownLatch>>> iterator = sentMessages.entrySet().iterator(); iterator.hasNext(); ) {
            val stringTupleEntry = iterator.next();
            long whenToTimeout = stringTupleEntry.getValue().getFirst();
            if (whenToTimeout < System.currentTimeMillis()) {
                stringTupleEntry.getValue().getSecond().countDown();
                iterator.remove();
            }
        }

        // check for timeout
        if (!timeoutFuse.isBlown()) {
            long msPassedSincePong = System.currentTimeMillis() - lastPong;
            if (lastPong != -1 && msPassedSincePong > (60 * 1000)) {
                timeoutFuse.blow();
                this.close();
                logger.info("Channel timed out");
            }

        }

        // send pings
        long now = System.currentTimeMillis();
        if (this.status == StompClient.StompClientStatus.CONNECTED) {
            if (nextPing > now) return;

            String msg = WhosOnlineManager.gson.toJson(new C03Ping(String.valueOf(now)));
            send(msg);
            nextPing = now + 10000;
        }


    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.info("WebSocket opened");

        logger.info("sending welcome message: {}", WhosOnlineManager.gson.toJson(new C00Connect(playerUuid)));

        send(WhosOnlineManager.gson.toJson(new C00Connect(playerUuid)));

        update = se.scheduleAtFixedRate(this::update, 5, 20, TimeUnit.MILLISECONDS);

        nextPing = System.currentTimeMillis() + 3000;
    }

    @Override
    public void onMessage(String message) {

        if(!message.startsWith("{\"t\":\"/pong\"")) {
            logger.info("Received message: {}", message);
        }

        AbstractMessage res = MessageParser.parse(message);

        if (res == null) {
            logger.error("failed to parse message: {}", message);
            return;
        }

        if (res instanceof S00ConnectAck) {
            if (status == StompClient.StompClientStatus.CONNECTING) {
                status = StompClient.StompClientStatus.CONNECTED;
                logger.info("Connection established");
            } else {
                logger.info("Received unexpected \"connected\" ");
            }
        } else if (res instanceof S01IsOnlineAck) {
            S01IsOnlineAck c = (S01IsOnlineAck) res;

            cache.putInCache(c.uuid, c.is_online);

            releaseAsyncGet(c.nonce);
        } else if (res instanceof S02areOnlineAck) {
            S02areOnlineAck c = (S02areOnlineAck) res;

            val entries = c.getUsers();

            for (val stringBooleanEntry : entries.entrySet()) {
                cache.putInCache(stringBooleanEntry.getKey(), stringBooleanEntry.getValue());
            }

            releaseAsyncGet(c.getNonce());
        } else if (res instanceof S04Pong) {
            S04Pong c = (S04Pong) res;

            long started = c.getC();
            this.ping = System.currentTimeMillis() - started;
            this.lastPong = System.currentTimeMillis();
        } else {
            logger.error("failed to parse message: {}", message);
        }


    }

    void releaseAsyncGet(@NotNull String id) {
        Tuple<Long, CountDownLatch> longCountDownLatchTuple = sentMessages.get(id);

        if (longCountDownLatchTuple != null && longCountDownLatchTuple.getSecond() != null)
            longCountDownLatchTuple.getSecond().countDown();

        sentMessages.remove(id);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (update != null) update.cancel(true);
        logger.info("Websocket closed code: {}  reason: {}", code, reason);

        for (val i : sentMessages.entrySet()) {
            val j = i.getValue();
            if(j != null){
                CountDownLatch k = j.getSecond();
                if(k != null){
                    k.countDown();
                }
            }
        }

        sentMessages.clear();

        status = StompClient.StompClientStatus.DISCONNECTED;
        MinecraftForge.EVENT_BUS.post(new WhosOnlineManager.WhosOnlineDied());
    }

    @Override
    public void onError(Exception ex) {
        logger.error("execution in websocket");
        logger.error(ex);
    }


    public void sendAndBlock(String message, String id, long timeout) {
        val c = new CountDownLatch(1);
        logger.info("Sending \"{}\"", message);
        sentMessages.put(id, new Tuple<>(timeout, c));
        this.send(message);

        try {
            logger.info("waiting for {}", id);
            c.await();
            logger.info("finished waiting for {}", id);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}

package kr.syeyoung.dungeonsguide.mod.whosonline.api;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import kr.syeyoung.dungeonsguide.mod.stomp.StompClient;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.AbstractMessage;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.impl.CConnect;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.impl.CPing;
import lombok.Getter;
import lombok.val;
import net.minecraft.util.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class WhosOnlineWebSocket extends WebSocketClient {
    private final Logger logger = LogManager.getLogger("WhosOnlineWebSocket");
    private final String playerUuid;
    @Getter
    final Gson gson = new Gson();
    @Getter
    private StompClient.StompClientStatus status = StompClient.StompClientStatus.CONNECTING;

    private final ConcurrentHashMap<String, Tuple<Long, CountDownLatch>> sentMessages = new ConcurrentHashMap<>();
    private boolean sendPingLock;

    public WhosOnlineWebSocket(final String remote, final String playerUuid) {
        super(URI.create(remote));
        this.playerUuid = playerUuid;

        setConnectionLostTimeout(0);

        val namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Dg WhosOnline update pool").build();
        val scheduler = Executors.newScheduledThreadPool(1, namedThreadFactory);
        scheduler.scheduleAtFixedRate(this::update, 5,20, TimeUnit.MILLISECONDS);
    }

    private long ping;
    volatile long lastPong = -1;

    void update(){
        // clear TimedOut Requests
        for (Iterator<Map.Entry<String, Tuple<Long, CountDownLatch>>> iterator = sentMessages.entrySet().iterator(); iterator.hasNext(); ) {
            val stringTupleEntry = iterator.next();
            long whenToTimeout = stringTupleEntry.getValue().getFirst();
            if(whenToTimeout < System.currentTimeMillis()){
                iterator.remove();
            }
        }

        // check for timeout
        if (lastPong != -1 && System.currentTimeMillis() - lastPong > (10 * 1000)) {
            this.close();
            logger.info("Channel timed out");
        }
    }

    private ScheduledFuture<?> heartbeat = null;

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.info("WebSocket opened");

        send(gson.toJson(new CConnect(playerUuid)));

        val ex = Executors.newSingleThreadScheduledExecutor();
        heartbeat = ex.scheduleAtFixedRate(() -> {
            if(sendPingLock){
                send(gson.toJson(new CPing(String.valueOf(System.currentTimeMillis()))));
                sendPingLock = false;
            }
        },1, 3, TimeUnit.SECONDS);
    }

    @Override
    public void onMessage(String message) {
        logger.info("Received Message: {}", message);

        AbstractMessage res = gson.fromJson(message, AbstractMessage.class);

        switch (res.t) {
            case "/connected":
                if (status == StompClient.StompClientStatus.CONNECTING) {
                    status = StompClient.StompClientStatus.CONNECTED;
                    logger.info("Connection established");
                } else {
                    logger.info("Received unexpected \"connected\" ");
                }
                break;
            case "/is_online":
                JsonElement isonline = res.c.getAsJsonObject().get("is_online");
                JsonElement uuid1 = res.c.getAsJsonObject().get("uuid");

                if (isonline != null && uuid1 != null) {
                    WhosOnlineCache.onlineppl.put(uuid1.getAsString(), isonline.getAsBoolean());
                }


                releaseAsyncGet(res.c.getAsJsonObject().get("nonce").getAsString());

                break;
            case "/is_online/bulk":
                Set<Map.Entry<String, JsonElement>> entries = res.c.getAsJsonObject().get("users").getAsJsonObject().entrySet();
                for (Map.Entry<String, JsonElement> entry : entries) {
                    WhosOnlineCache.onlineppl.put(entry.getKey(), entry.getValue().getAsBoolean());
                }

                releaseAsyncGet(res.c.getAsJsonObject().get("nonce").getAsString());

                break;

            case "/pong":
                long started = res.c.getAsLong();
                this.ping = System.currentTimeMillis() - started;
                this.lastPong = System.currentTimeMillis();
                this.sendPingLock = true;
                break;
            default:
                logger.info("Unknown message: {}", res.t);
                break;
        }

    }

    void releaseAsyncGet(String id){
        if (id == null) return;


        Tuple<Long, CountDownLatch> longCountDownLatchTuple = sentMessages.get(id);
        if(longCountDownLatchTuple != null){
            longCountDownLatchTuple.getSecond().countDown();
        }
        sentMessages.remove(id);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (heartbeat != null) heartbeat.cancel(true);
        logger.info("Websocket closed code: {}  reason: {}", code, reason);
        status = StompClient.StompClientStatus.DISCONNECTED;
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

    public void sendAndBlock(String message, String id, long timeout){
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

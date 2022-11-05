package kr.syeyoung.dungeonsguide.mod.whosonline.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.mod.stomp.StompClient;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.AbstractMessage;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.impl.CConnect;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;

public class WhosOnlineWebSocket extends WebSocketClient {
    @Getter
    final Gson gson;
    private final Logger logger = LogManager.getLogger("WhosOnlineWebSocket");
    private final String playerUuid;
    @Getter
    private StompClient.StompClientStatus status = StompClient.StompClientStatus.CONNECTING;

    public WhosOnlineWebSocket(final String remote, final String playerUuid) {
        super(URI.create(remote));
        this.playerUuid = playerUuid;
        gson = new Gson();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.info("WebSocket opened");
        send(gson.toJson(new CConnect(playerUuid)));
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
                JsonObject c = res.c;
                JsonElement isonline = c.get("is_online");
                JsonElement uuid1 = c.get("uuid");

                if (isonline != null && uuid1 != null) {
                    WhosOnlineCache.onlineppl.put(uuid1.getAsString(), isonline.getAsBoolean());
                }
                break;
            case "/is_online/bulk":
                for (Map.Entry<String, JsonElement> entry : res.c.entrySet()) {
                    WhosOnlineCache.onlineppl.put(entry.getKey(), entry.getValue().getAsBoolean());
                }
                break;
            default:
                logger.info("Unknown message: {}", res.t);
                break;
        }

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("Websocket closed code: {}  reason: {}", code, reason);
        status = StompClient.StompClientStatus.DISCONNECTED;
    }

    @Override
    public void onError(Exception ex) {
        logger.error("execution in websocket");
        logger.error(ex);
    }

}

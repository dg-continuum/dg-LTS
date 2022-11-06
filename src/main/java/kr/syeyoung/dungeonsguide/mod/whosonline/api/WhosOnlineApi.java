package kr.syeyoung.dungeonsguide.mod.whosonline.api;

import kr.syeyoung.dungeonsguide.mod.stomp.StompClient;
import kr.syeyoung.dungeonsguide.mod.whosonline.WhosOnlineManager;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.client.C02IsOnline;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.client.C04OnlineCheckBulk;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Api wrapper for whos online websocket server
 * @author Eryk Ruta
 */
public class WhosOnlineApi {
    private static final long TIMEOUT_VALUE = 500;
    private final WhosOnlineWebSocket client;
    private final ExecutorService executor;

    public WhosOnlineApi(WhosOnlineWebSocket client, ExecutorService ex) {
        this.client = client;

        executor = ex;
    }

    Logger logger = LogManager.getLogger("WhosOnlineApi");


    /**
     * @return -s if websocket is ok
     */
    boolean stateCheck() {
        return client.getStatus() != StompClient.StompClientStatus.CONNECTED;
    }

    /**
     * @param uuid UUid of player to check
     * @return true if player is online with dg
     */
    public @Nullable Future<Boolean> isOnline(@NotNull final String uuid) {
        if (stateCheck()) return null;
        return executor.submit(() -> {
            val messageId = UUID.randomUUID().toString();
            val message = WhosOnlineManager.gson.toJson(new C02IsOnline(new C02IsOnline.OBJ(uuid, messageId)));

            client.sendAndBlock(message, messageId, TIMEOUT_VALUE);

            logger.info("Is online: {} uuid: {} ", uuid , WhosOnlineCache.onlineppl.getOrDefault(uuid, false));

            return WhosOnlineCache.onlineppl.getOrDefault(uuid, false);

        });
    }

    /**
     * @param uuids uuids of player to check
     * @return array of their statuses
     */
    public @Nullable Future<Boolean[]> areOnline(@NotNull final String[] uuids) {
        if (stateCheck()) return null;
        return executor.submit(() -> {
            val messageId = UUID.randomUUID().toString();
            val message = WhosOnlineManager.gson.toJson(new C04OnlineCheckBulk(new C04OnlineCheckBulk.OBJ(uuids, messageId)));

            client.sendAndBlock(message, messageId, TIMEOUT_VALUE);

            val returnVals = new Boolean[uuids.length];

            for (int i = 0; i < uuids.length; i++) {
                val uuid = uuids[i];
                returnVals[i] = WhosOnlineCache.onlineppl.getOrDefault(uuid, false);
            }

            return returnVals;

        });
    }


}

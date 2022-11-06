package kr.syeyoung.dungeonsguide.mod.whosonline.api;

import com.google.gson.Gson;
import kr.syeyoung.dungeonsguide.mod.stomp.StompClient;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.impl.COnlineCheck;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.impl.COnlineCheckBulk;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WhosOnlineApi {
    private static final long TIMEOUT_VALUE = 500;
    private final WhosOnlineWebSocket client;
    private final ExecutorService executor;
    private final Gson gson;

    public WhosOnlineApi(WhosOnlineWebSocket client) {
        this.client = client;
        gson = client.getGson();

        executor = Executors.newCachedThreadPool();
    }


    /**
     * @return true if everything is ok
     */
    boolean stateCheck() {
        return client.getStatus() == StompClient.StompClientStatus.CONNECTED;
    }

    /**
     * @param uuid UUid of player to check
     * @return true if player is online with dg
     */
    public @Nullable Future<Boolean> isOnline(@NotNull final String uuid) {
        if (!stateCheck()) return null;
        return executor.submit(() -> {
            val messageId = UUID.randomUUID().toString();
            val message = gson.toJson(new COnlineCheck(new COnlineCheck.OBJ(uuid, messageId)));

            client.sendAndBlock(message, messageId, TIMEOUT_VALUE);

            return WhosOnlineCache.onlineppl.getOrDefault(uuid, false);

        });
    }

    /**
     * @param uuids uuids of player to check
     * @return array of their statuses
     */
    public @Nullable Future<Boolean[]> areOnline(@NotNull final String[] uuids) {
        if (!stateCheck()) return null;
        return executor.submit(() -> {
            val messageId = UUID.randomUUID().toString();
            val message = gson.toJson(new COnlineCheckBulk(new COnlineCheckBulk.OBJ(uuids, messageId)));

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

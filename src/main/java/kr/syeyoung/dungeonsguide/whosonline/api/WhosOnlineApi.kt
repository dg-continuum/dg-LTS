package kr.syeyoung.dungeonsguide.whosonline.api;

import kr.syeyoung.dungeonsguide.stomp.StompClient;
import kr.syeyoung.dungeonsguide.whosonline.WhosOnlineManager;
import kr.syeyoung.dungeonsguide.whosonline.api.messages.client.C01IsOnline;
import kr.syeyoung.dungeonsguide.whosonline.api.messages.client.C02OnlineCheckBulk;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Api wrapper for whos online websocket server
 * @author Eryk Ruta
 */
public class WhosOnlineApi {
    private static final long TIMEOUT_VALUE = 500;
    private final WhosOnlineWebSocket client;
    private WhosOnlineCache cache;
    private final ExecutorService executor;

    public WhosOnlineApi(WhosOnlineWebSocket client, WhosOnlineCache cache, ExecutorService ex) {
        this.client = client;
        this.cache = cache;

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
    public @NotNull Future<Boolean> isOnline(@NotNull final String uuid) {
        if(cache.isCached(uuid)){
            return CompletableFuture.completedFuture(cache.isOnline(uuid));
        }
        if (stateCheck()) {
            return CompletableFuture.completedFuture(false);
        }


        return executor.submit(() -> {

            val messageId = UUID.randomUUID().toString();
            val message = WhosOnlineManager.gson.toJson(new C01IsOnline(new C01IsOnline.OBJ(uuid, messageId)));

            client.sendAndBlock(message, messageId, TIMEOUT_VALUE);

            logger.info("Is online: {} uuid: {} ", uuid , cache.isOnline(uuid));

            return cache.isOnline(uuid);

        });
    }

    /**
     * @param uuids uuids of player to check
     * @return array of their statuses
     */
    public @NotNull Future<Boolean[]> areOnline(@NotNull final String[] uuids) {
        if (stateCheck()) {
            val nulls = new Boolean[uuids.length];
            Arrays.fill(nulls, false);
            return CompletableFuture.completedFuture(nulls);
        }

        Set<String> notCached = new HashSet<String>();
        val cached = new HashMap<String, Boolean>();

        for (String uuid : uuids) {
            val iscached = cache.isCached(uuid);

            if (iscached) {
                notCached.add(uuid);
            } else {
                cached.put(uuid, cache.isOnline(uuid));
            }
        }

        // in case that all the nicks are not cached
        if(notCached.isEmpty()){
            val res = new Boolean[uuids.length];
            // we do this to preserve the order which they were added in
            for (int i = 0; i < uuids.length; i++) {
                String uuid = uuids[i];

                res[i] = cached.get(uuid);
            }

            return CompletableFuture.completedFuture(res);
        }


        return executor.submit(() -> {
            val messageId = UUID.randomUUID().toString();

            val message = WhosOnlineManager.gson.toJson(new C02OnlineCheckBulk(new C02OnlineCheckBulk.OBJ((String[]) notCached.toArray(), messageId)));

            client.sendAndBlock(message, messageId, TIMEOUT_VALUE);

            val returnVals = new Boolean[uuids.length];

            for (int i = 0; i < uuids.length; i++) {
                val uuid = uuids[i];
                returnVals[i] = cache.isOnline(uuid);
            }

            return returnVals;

        });
    }


}

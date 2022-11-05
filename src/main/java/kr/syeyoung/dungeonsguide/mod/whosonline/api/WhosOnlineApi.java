package kr.syeyoung.dungeonsguide.mod.whosonline.api;

import com.google.gson.Gson;
import kr.syeyoung.dungeonsguide.mod.stomp.StompClient;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.impl.COnlineCheck;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.impl.COnlineCheckBulk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WhosOnlineApi {
    private static final float TIMEOUT_VALUE = 500;
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
        if (stateCheck()) return null;
        return executor.submit(() -> {
            client.send(gson.toJson(new COnlineCheck(uuid)));

            long started = System.currentTimeMillis();
            boolean shouldStop = false;

            while (!shouldStop) {
                if (!WhosOnlineCache.onlineppl.containsKey(uuid)) {
                    if (started + TIMEOUT_VALUE < System.currentTimeMillis()) {
                        shouldStop = true;
                    } else {
                        Thread.sleep(20);
                    }
                } else {
                    shouldStop = true;
                }
            }

            return WhosOnlineCache.onlineppl.getOrDefault(uuid, false);

        });
    }

    /**
     * @param uuids uuids of player to check
     * @return array of their statuses
     */
    @Nullable
    public Future<Boolean[]> areOnline(final String[] uuids) {
        if (stateCheck()) return null;
        return executor.submit(() -> {
            client.send(gson.toJson(new COnlineCheckBulk(uuids)));

//            long started = System.currentTimeMillis();
//            boolean shouldStop = false;

//            while (!shouldStop) {
//                if (!WhosOnlineCache.onlineppl.containsKey(uuid)) {
//                    if (started + TIMEOUT_VALUE < System.currentTimeMillis()) {
//                        shouldStop = true;
//                    } else {
//                        Thread.sleep(20);
//                    }
//                } else {
//                    shouldStop = true;
//                }
//            }

            final Boolean[] returnVals = new Boolean[uuids.length];

            for (int i = 0; i < uuids.length; i++) {
                String uuid = uuids[i];
                returnVals[i] = WhosOnlineCache.onlineppl.getOrDefault(uuid, false);
            }

            return returnVals;

        });
    }



}

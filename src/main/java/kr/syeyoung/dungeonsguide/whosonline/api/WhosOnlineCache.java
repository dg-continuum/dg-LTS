package kr.syeyoung.dungeonsguide.whosonline.api;

import kr.syeyoung.dungeonsguide.features.impl.misc.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.whosonline.api.data.ActiveUser;
import kr.syeyoung.dungeonsguide.whosonline.api.data.CosmeticCacheObject;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WhosOnlineCache {

    @Getter @Setter
    private CosmeticCacheObject cms;

    // 2 minutes
    private static final long TIME_TO_EXPIRE = 120000;

    final ConcurrentMap<UUID, ActiveUser> cache;

    public WhosOnlineCache() {
        cache = new ConcurrentHashMap<>();
    }

    private boolean checkAndClearExpired(String uuid) {
        return checkAndClearExpired(UUID.fromString(uuid));
    }

    /**
     * This function checks if the entry is expired,
     * deletes it if its expired and returns true
     *
     * @param uuid uuid to check
     * @return true is the entry has expired and now deleted
     */
    private boolean checkAndClearExpired(UUID uuid) {
        ActiveUser activeUser = cache.get(uuid);
        if (activeUser != null) {
            long updatedAt = activeUser.updatedAt;
            if (updatedAt + TIME_TO_EXPIRE > System.currentTimeMillis()) {
                cache.remove(uuid);
                return true;
            }
        }
        return false;
    }

    @Nullable
    public ActiveUser getFromCache(UUID uuid) {
        if (checkAndClearExpired(uuid)) return null;
        return cache.get(uuid);
    }

    public void putInCache(String uuid, boolean isonline) {
        putInCache(UUID.fromString(uuid), isonline);
    }

    public void putInCache(UUID uuid, boolean isonline) {
        cache.put(uuid, new ActiveUser(ApiFetcher.fetchNicknameAsync(uuid.toString()), isonline, System.currentTimeMillis()));
    }

    public boolean isCached(String uuid) {
        return cache.containsKey(UUID.fromString(uuid));
    }

    public boolean isOnline(String uuid) {
        if (checkAndClearExpired(uuid)) return false;

        ActiveUser activeUser = cache.get(UUID.fromString(uuid));

        return (activeUser != null) && activeUser.isOnline;

    }
}

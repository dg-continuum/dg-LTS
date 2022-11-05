package kr.syeyoung.dungeonsguide.mod.whosonline.api;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WhosOnlineCache {
    public static final ConcurrentMap<String, Boolean> onlineppl = new ConcurrentHashMap<>();
}

package kr.syeyoung.dungeonsguide.mod.onconfig.huds;

import cc.polyfrost.oneconfig.config.annotations.Switch;

public class epicCountdown {
    @Switch(name = "Clean Dungeon Chat")
    public static boolean cleanChat = true;
    @Switch(name = "Countdown SFX")
    public static boolean sfxenabled = true;
    @Switch(name = "enabled", size = 2)
    public static boolean epicCountdown = true;
}

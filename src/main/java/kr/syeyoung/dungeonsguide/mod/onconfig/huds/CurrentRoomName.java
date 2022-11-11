package kr.syeyoung.dungeonsguide.mod.onconfig.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.FeatureDungeonRoomName;

public class CurrentRoomName {
    @HUD(name = "Display name of the room you are in")
    public static FeatureDungeonRoomName ss = new FeatureDungeonRoomName();
}

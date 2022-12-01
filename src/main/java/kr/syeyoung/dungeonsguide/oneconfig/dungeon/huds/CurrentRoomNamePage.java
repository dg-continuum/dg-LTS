package kr.syeyoung.dungeonsguide.oneconfig.dungeon.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.huds.FeatureDungeonRoomName;

public class CurrentRoomNamePage {
    @HUD(name = "Display name of the room you are in")
    public static FeatureDungeonRoomName ss = new FeatureDungeonRoomName();
}

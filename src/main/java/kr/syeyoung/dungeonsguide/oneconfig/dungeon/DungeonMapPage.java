package kr.syeyoung.dungeonsguide.oneconfig.dungeon;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.huds.FeatureDungeonMap;

public class DungeonMapPage {
    @Switch(
            name = "enable",
            size = 2
    )
    public static boolean dungeonMap = true;
    @HUD(
            name = "Dungeon Map"
    )
    public static FeatureDungeonMap f = new FeatureDungeonMap();
}

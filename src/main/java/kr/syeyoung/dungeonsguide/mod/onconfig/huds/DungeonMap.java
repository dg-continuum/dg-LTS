package kr.syeyoung.dungeonsguide.mod.onconfig.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.FeatureDungeonMap;

public class DungeonMap {
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

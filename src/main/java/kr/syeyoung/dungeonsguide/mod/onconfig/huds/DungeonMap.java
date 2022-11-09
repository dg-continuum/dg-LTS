package kr.syeyoung.dungeonsguide.mod.onconfig.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.FeatureDungeonMap;

public class DungeonMap {
    @HUD(
            name = "Dungeon Map"
    )
    public static FeatureDungeonMap f = new FeatureDungeonMap();
}

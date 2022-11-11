package kr.syeyoung.dungeonsguide.mod.onconfig.huds.dungeon;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.FeatureDungeonDeaths;

public class DeathDisplay {
    @HUD(
            name = "Dungeon Deaths"
    )
    public static FeatureDungeonDeaths aaa = new FeatureDungeonDeaths();
}

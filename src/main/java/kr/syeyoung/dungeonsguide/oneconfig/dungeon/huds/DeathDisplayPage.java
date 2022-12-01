package kr.syeyoung.dungeonsguide.oneconfig.dungeon.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.huds.FeatureDungeonDeaths;

public class DeathDisplayPage {
    @HUD(
            name = "Dungeon Deaths"
    )
    public static FeatureDungeonDeaths aaa = new FeatureDungeonDeaths();
}

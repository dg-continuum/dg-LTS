package kr.syeyoung.dungeonsguide.mod.onconfig.huds.dungeon;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.FeatureDungeonTombs;

public class CryptsDisplay {
    @HUD(
            name = "Display # of Crypts"
    )
    public FeatureDungeonTombs s = new FeatureDungeonTombs();

}

package kr.syeyoung.dungeonsguide.oneconfig.dungeon.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.huds.FeatureDungeonTombs;

public class CryptsDisplayPage {
    @HUD(
            name = "Display # of Crypts"
    )
    public FeatureDungeonTombs s = new FeatureDungeonTombs();

}

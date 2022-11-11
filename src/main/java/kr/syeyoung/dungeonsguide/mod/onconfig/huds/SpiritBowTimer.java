package kr.syeyoung.dungeonsguide.mod.onconfig.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.boss.FeatureThornSpiritBowTimer;

public class SpiritBowTimer {
    @HUD(
            name = "Display Spirit bow timer"
    )
    public static FeatureThornSpiritBowTimer ftsbt = new FeatureThornSpiritBowTimer();
}

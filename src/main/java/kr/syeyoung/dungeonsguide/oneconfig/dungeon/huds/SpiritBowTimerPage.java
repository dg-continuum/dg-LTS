package kr.syeyoung.dungeonsguide.oneconfig.dungeon.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.boss.f4.FeatureThornSpiritBowTimer;

public class SpiritBowTimerPage {
    @HUD(
            name = "Display Spirit bow timer"
    )
    public static FeatureThornSpiritBowTimer ftsbt = new FeatureThornSpiritBowTimer();
}

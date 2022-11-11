package kr.syeyoung.dungeonsguide.oneconfig.dungeon.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.boss.f4.FeatureThornBearPercentage;

public class SpiritBearPrctPage {
    @HUD(name = "Display Spirit Bear Summon Percentage")
    public static FeatureThornBearPercentage ss = new FeatureThornBearPercentage();
}

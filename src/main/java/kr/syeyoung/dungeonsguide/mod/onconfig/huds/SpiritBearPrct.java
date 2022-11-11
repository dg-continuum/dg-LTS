package kr.syeyoung.dungeonsguide.mod.onconfig.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.boss.FeatureThornBearPercentage;

public class SpiritBearPrct {
    @HUD(name = "Display Spirit Bear Summon Percentage")
    public static FeatureThornBearPercentage ss = new FeatureThornBearPercentage();
}

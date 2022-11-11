package kr.syeyoung.dungeonsguide.mod.onconfig.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.boss.FeatureCurrentPhase;

public class CurrentBossPhase {
    @HUD(
            name = "Display Current Phase"
    )
    public static FeatureCurrentPhase a = new FeatureCurrentPhase();
}

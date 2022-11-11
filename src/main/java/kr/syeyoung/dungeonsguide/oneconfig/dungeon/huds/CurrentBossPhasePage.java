package kr.syeyoung.dungeonsguide.oneconfig.dungeon.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.boss.FeatureCurrentPhase;

public class CurrentBossPhasePage {
    @HUD(
            name = "Display Current Phase"
    )
    public static FeatureCurrentPhase a = new FeatureCurrentPhase();
}

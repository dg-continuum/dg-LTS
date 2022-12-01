package kr.syeyoung.dungeonsguide.oneconfig.dungeon.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.huds.FeatureWarnLowHealth;

public class WarnLowHealthPage {
    @HUD(
            name = "Low Health Warning"
    )
    public FeatureWarnLowHealth a = new FeatureWarnLowHealth();
}

package kr.syeyoung.dungeonsguide.mod.onconfig.huds.dungeon;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.FeatureWarnLowHealth;

public class WarnLowHealth {
    @HUD(
            name = "Low Health Warning"
    )
    public FeatureWarnLowHealth a = new FeatureWarnLowHealth();
}

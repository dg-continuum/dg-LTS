package kr.syeyoung.dungeonsguide.mod.onconfig.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCooldownCounter;

public class CooldownCounter {
    @HUD(
            name = "CooldownCounter"
    )
    public static FeatureCooldownCounter f = new FeatureCooldownCounter();
}

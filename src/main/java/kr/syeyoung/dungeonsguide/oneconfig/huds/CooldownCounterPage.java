package kr.syeyoung.dungeonsguide.oneconfig.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.misc.FeatureCooldownCounter;

public class CooldownCounterPage {
    @HUD(
            name = "CooldownCounter"
    )
    public static FeatureCooldownCounter f = new FeatureCooldownCounter();
}

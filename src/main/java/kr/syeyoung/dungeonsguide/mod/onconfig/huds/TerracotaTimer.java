package kr.syeyoung.dungeonsguide.mod.onconfig.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.boss.FeatureTerracotaTimer;

public class TerracotaTimer {
    @HUD(
            name = "TerracotaTimer"
    )
    public static FeatureTerracotaTimer f = new FeatureTerracotaTimer();
}

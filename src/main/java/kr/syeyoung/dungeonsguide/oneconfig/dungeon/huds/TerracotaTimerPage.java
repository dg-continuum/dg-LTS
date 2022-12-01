package kr.syeyoung.dungeonsguide.oneconfig.dungeon.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.boss.f6.FeatureTerracotaTimer;

public class TerracotaTimerPage {
    @HUD(
            name = "TerracotaTimer"
    )
    public static FeatureTerracotaTimer f = new FeatureTerracotaTimer();
}

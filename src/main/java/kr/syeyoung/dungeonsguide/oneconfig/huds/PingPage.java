package kr.syeyoung.dungeonsguide.oneconfig.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.misc.FeaturePing;

public class PingPage {
    @HUD(
            name = "settings"
    )
    public FeaturePing pingHud = new FeaturePing();
}

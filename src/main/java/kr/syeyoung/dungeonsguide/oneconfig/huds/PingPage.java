package kr.syeyoung.dungeonsguide.oneconfig.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import kr.syeyoung.dungeonsguide.features.impl.misc.FeaturePing;

public class PingPage {

    @Switch(
            name = "Warn about server not responding"
    )
    public static boolean warnAboutServerNotresp = true;

    @HUD(
            name = "settings"
    )
    public FeaturePing pingHud = new FeaturePing();
}

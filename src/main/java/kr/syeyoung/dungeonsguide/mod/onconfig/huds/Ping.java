package kr.syeyoung.dungeonsguide.mod.onconfig.huds;

import cc.polyfrost.oneconfig.config.annotations.Checkbox;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeaturePing;

public class Ping {
    @HUD(
            name = "settings"
    )
    public FeaturePing pingHud = new FeaturePing();
}

package kr.syeyoung.dungeonsguide.mod.onconfig.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.FeatureSoulRoomWarning;

public class FairySoulWarning {
    @HUD(
            name = "Fairy Soul Warning"
    )
    public static FeatureSoulRoomWarning ss = new FeatureSoulRoomWarning();
}

package kr.syeyoung.dungeonsguide.oneconfig.secrets;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.huds.FeatureSoulRoomWarning;

public class FairySoulWarningPage {
    @HUD(
            name = "Fairy Soul Warning"
    )
    public static FeatureSoulRoomWarning ss = new FeatureSoulRoomWarning();
}

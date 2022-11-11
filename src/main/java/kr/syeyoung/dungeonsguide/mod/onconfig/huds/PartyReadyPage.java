package kr.syeyoung.dungeonsguide.mod.onconfig.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.FeaturePartyReady;

public class PartyReadyPage {
    @HUD(
            name = "Party Ready List"
    )
    public FeaturePartyReady a = new FeaturePartyReady();
}

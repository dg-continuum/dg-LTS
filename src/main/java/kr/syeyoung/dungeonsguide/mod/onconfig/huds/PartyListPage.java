package kr.syeyoung.dungeonsguide.mod.onconfig.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.FeaturePartyList;

public class PartyListPage {
    @HUD(
            name = "Party List"
    )
    public FeaturePartyList a = new FeaturePartyList();
}

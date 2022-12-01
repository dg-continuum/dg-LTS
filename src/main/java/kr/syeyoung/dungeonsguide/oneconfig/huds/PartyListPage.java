package kr.syeyoung.dungeonsguide.oneconfig.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.party.FeaturePartyList;

public class PartyListPage {
    @HUD(
            name = "Party List"
    )
    public FeaturePartyList a = new FeaturePartyList();
}

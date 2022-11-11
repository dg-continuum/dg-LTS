package kr.syeyoung.dungeonsguide.features.impl.misc.playerpreview.api;

import kr.syeyoung.dungeonsguide.features.impl.misc.playerpreview.api.playerprofile.PlayerProfile;
import lombok.Data;

@Data
public class PlayerSkyblockData {
    PlayerProfile[] playerProfiles;
    int lastestprofileArrayIndex;
}

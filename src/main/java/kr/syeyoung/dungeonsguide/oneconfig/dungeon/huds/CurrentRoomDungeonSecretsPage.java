package kr.syeyoung.dungeonsguide.oneconfig.dungeon.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.huds.FeatureDungeonCurrentRoomSecrets;

public class CurrentRoomDungeonSecretsPage {
    @HUD(
            name="Display # Secrets in current room"
    )
    public FeatureDungeonCurrentRoomSecrets a = new FeatureDungeonCurrentRoomSecrets();
}

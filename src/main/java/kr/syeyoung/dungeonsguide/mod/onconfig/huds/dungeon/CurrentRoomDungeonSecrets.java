package kr.syeyoung.dungeonsguide.mod.onconfig.huds.dungeon;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.FeatureDungeonCurrentRoomSecrets;

public class CurrentRoomDungeonSecrets {
    @HUD(
            name="Display # Secrets in current room"
    )
    public FeatureDungeonCurrentRoomSecrets a = new FeatureDungeonCurrentRoomSecrets();
}

package kr.syeyoung.dungeonsguide.mod.onconfig.huds.dungeon;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.FeatureDungeonSecrets;

public class DungeonSecrets {
    @HUD(
            name = "Display Total # of Secrets"
    )
    public static FeatureDungeonSecrets aa = new FeatureDungeonSecrets();
}

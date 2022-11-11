package kr.syeyoung.dungeonsguide.oneconfig.dungeon.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.huds.FeatureDungeonSecrets;

public class DungeonSecretsPage {
    @HUD(
            name = "Display Total # of Secrets"
    )
    public static FeatureDungeonSecrets aa = new FeatureDungeonSecrets();
}

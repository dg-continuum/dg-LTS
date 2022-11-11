package kr.syeyoung.dungeonsguide.oneconfig.dungeon.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.huds.FeatureDungeonScore;

public class DungeonScorePage {
    @HUD(
            name = "Display Current Score"
    )
    public FeatureDungeonScore a = new FeatureDungeonScore();
}

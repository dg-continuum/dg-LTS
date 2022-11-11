package kr.syeyoung.dungeonsguide.mod.onconfig.huds.dungeon;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.FeatureDungeonScore;

public class DungeonScorePage {
    @HUD(
            name = "Display Current Score"
    )
    public FeatureDungeonScore a = new FeatureDungeonScore();
}

package kr.syeyoung.dungeonsguide.mod.onconfig.huds.dungeon;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.FeatureDungeonMilestone;

public class DungeonMilestone {
    @HUD(
            name = "Display Current Class Milestone"
    )
    public FeatureDungeonMilestone a = new FeatureDungeonMilestone();
}

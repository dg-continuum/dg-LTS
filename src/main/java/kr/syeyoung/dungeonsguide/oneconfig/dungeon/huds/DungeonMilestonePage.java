package kr.syeyoung.dungeonsguide.oneconfig.dungeon.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.huds.FeatureDungeonMilestone;

public class DungeonMilestonePage {
    @HUD(
            name = "Display Current Class Milestone"
    )
    public FeatureDungeonMilestone a = new FeatureDungeonMilestone();
}

package kr.syeyoung.dungeonsguide.oneconfig.dungeon.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.huds.FeatureDungeonRealTime;

public class DungeonRealTimePage {
    @HUD (
            name = "Display Real Time-Dungeon Time"
    )
    public FeatureDungeonRealTime a = new FeatureDungeonRealTime();
}

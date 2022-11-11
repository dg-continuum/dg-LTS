package kr.syeyoung.dungeonsguide.mod.onconfig.huds.dungeon;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.FeatureDungeonRealTime;

public class DungeonRealTime {
    @HUD (
            name = "Display Real Time-Dungeon Time"
    )
    public FeatureDungeonRealTime a = new FeatureDungeonRealTime();
}

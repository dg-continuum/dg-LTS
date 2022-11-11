package kr.syeyoung.dungeonsguide.oneconfig.dungeon.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.huds.FeatureDungeonSBTime;

public class DungeonSBTimePage {
    @HUD(
            name = "Display Ingame Dungeon Time"
    )
    public FeatureDungeonSBTime a = new FeatureDungeonSBTime();
}

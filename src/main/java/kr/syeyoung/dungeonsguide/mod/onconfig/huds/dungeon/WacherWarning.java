package kr.syeyoung.dungeonsguide.mod.onconfig.huds.dungeon;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.FeatureWatcherWarning;

public class WacherWarning {
    @HUD(
            name = "Watcher Spawn Alert"
    )
    public FeatureWatcherWarning a = new FeatureWatcherWarning();
}

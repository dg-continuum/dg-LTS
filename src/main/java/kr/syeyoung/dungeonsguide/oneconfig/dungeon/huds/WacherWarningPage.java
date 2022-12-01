package kr.syeyoung.dungeonsguide.oneconfig.dungeon.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.huds.FeatureWatcherWarning;

public class WacherWarningPage {
    @HUD(
            name = "Watcher Spawn Alert"
    )
    public FeatureWatcherWarning a = new FeatureWatcherWarning();
}

package kr.syeyoung.dungeonsguide.oneconfig.dungeon.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.secret.FeatureActions;

public class ActionsViewerPage {
    @HUD(
            name = "Action Viewer"
    )
    public FeatureActions a = new FeatureActions();
}

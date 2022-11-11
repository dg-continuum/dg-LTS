package kr.syeyoung.dungeonsguide.mod.onconfig.huds.dungeon;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.FeatureActions;

public class ActionsViewerPage {
    @HUD(
            name = "Action Viewer"
    )
    public FeatureActions a = new FeatureActions();
}

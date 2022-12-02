package kr.syeyoung.dungeonsguide.dungeon.actions.tree;

import kr.syeyoung.dungeonsguide.config.types.AColor;
import lombok.Data;

@Data
public class ActionRouteProperties {
    public boolean isPathfind() {
        return pathfind;
    }

    public void setPathfind(boolean pathfind) {
        this.pathfind = pathfind;
    }

    private boolean pathfind;

    public int getLineRefreshRate() {
        return lineRefreshRate;
    }

    public void setLineRefreshRate(int lineRefreshRate) {
        this.lineRefreshRate = lineRefreshRate;
    }

    private int lineRefreshRate;
    private AColor lineColor;
    private float lineWidth;

    private boolean beacon;
    private AColor beaconColor;
    private AColor beaconBeamColor;
}

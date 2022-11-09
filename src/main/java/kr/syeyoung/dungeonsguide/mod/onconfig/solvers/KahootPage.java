package kr.syeyoung.dungeonsguide.mod.onconfig.solvers;

import cc.polyfrost.oneconfig.config.annotations.Color;
import cc.polyfrost.oneconfig.config.core.OneColor;

public class KahootPage {
    @Color(
            name = "Target Color",
            description = "Color of the solution box"
    )
    public static OneColor color = new OneColor(0,255,0,50);
}

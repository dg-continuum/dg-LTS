package kr.syeyoung.dungeonsguide.oneconfig.solvers;

import cc.polyfrost.oneconfig.config.annotations.Color;
import cc.polyfrost.oneconfig.config.core.OneColor;

public class ThreeWeirdosSolverPage {
    @Color(
            name = "Correct chest color",
            description = "Color of the text (others)"
    )
    public static OneColor chestColor = new OneColor(0,255,0,50);
}

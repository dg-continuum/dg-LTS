package kr.syeyoung.dungeonsguide.oneconfig.solvers;

import cc.polyfrost.oneconfig.config.annotations.Color;
import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.core.OneColor;

public class IceFillPage {

    @Color(
            name = "lineColor",
            description = "Color of the solution line"
    )
    public static OneColor ICE_FILL_LINECOLOR = new OneColor(0xFF00FF00);
    @Slider(
            name = "Line Thickness",
            min = 0,
            max = 10,
            description = "Thickness of the solution line"
    )
    public static float ICE_FILL_LINEWIDTH = 1.0F;
}

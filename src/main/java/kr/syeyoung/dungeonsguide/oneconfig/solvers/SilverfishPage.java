package kr.syeyoung.dungeonsguide.oneconfig.solvers;

import cc.polyfrost.oneconfig.config.annotations.Color;
import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.core.OneColor;

public class SilverfishPage {
    @Slider(
            name = "Line Thickness",
            min = 0,
            max = 10
    )
    public static float width = 1.0F;
    @Color(
            name = "Line Color"
    )
    public static OneColor color = new OneColor(0xFF00FF00);
}

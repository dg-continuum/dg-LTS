package kr.syeyoung.dungeonsguide.mod.onconfig.solvers;

import cc.polyfrost.oneconfig.config.annotations.Color;
import cc.polyfrost.oneconfig.config.core.OneColor;

public class Teleport {
    @Color(
            name = "Solution Color",
            description = "Color of the solution teleport pad"
    )
    public static OneColor solutionColor = new OneColor(0,255,0,100);
    @Color(
            name = "Solution Color",
            description = "Color of the solution teleport pads you've been to"
    )
    public static OneColor nonSolutionColor = new OneColor(255,0,0,100);
}

package kr.syeyoung.dungeonsguide.oneconfig.solvers;

import cc.polyfrost.oneconfig.config.annotations.Color;
import cc.polyfrost.oneconfig.config.core.OneColor;

public class TicktackToeSolverPage {
    @Color(
            name = "Player Color",
            description = "Color of the solution box during your turn"
    )
    public static OneColor solutionColor = new OneColor(0,255,255,50);

    @Color(
            name = "Bot move Color",
            description = "Color of the solution box during enemy turn"
    )
    public static OneColor predictColor = new OneColor(255,201,0,50);
}

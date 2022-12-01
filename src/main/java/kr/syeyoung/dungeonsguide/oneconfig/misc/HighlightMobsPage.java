package kr.syeyoung.dungeonsguide.oneconfig.misc;

import cc.polyfrost.oneconfig.config.annotations.Color;
import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.core.OneColor;

public class HighlightMobsPage {
    @Slider(
            name = "Highlighting radius for Skeleton Masters",
            max = 100,
            min = 5
    )
    public static float masterRadius = 20;

    @Color(
            name = "Highlighting color for Skeleton Masters"
    )
    public static OneColor masterColor = new OneColor(255,0,0,50);



    @Slider(
            name = "Highlighting radius for Bats",
            max = 100,
            min = 5
    )
    public static float batRadius = 20;

    @Color(
            name = "Highlighting color for Bats"
    )
    public static OneColor batColor = new OneColor(255,0,0,50);


    @Slider(
            name = "Highlighting radius for Stared Mobs",
            max = 100,
            min = 5
    )
    public static float starRadius = 20;

    @Color(
            name = "Highlighting color for Stared Mobs"
    )
    public static OneColor starColor = new OneColor(0,255,255,50);


}

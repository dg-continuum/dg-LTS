package kr.syeyoung.dungeonsguide.oneconfig.dungeon;

import cc.polyfrost.oneconfig.config.annotations.Checkbox;
import cc.polyfrost.oneconfig.config.annotations.Switch;

public class HideAnimalPage {
    @Switch(
            name = "Enabled",
            size = 2
    )
    public static boolean enabled;


    @Checkbox(
            name = "Hide cows"
    )
    public static boolean cow = false;

    @Checkbox(
            name = "Hide sheep"
    )
    public static boolean sheep = false;
    @Checkbox(
            name = "Hide chickens"
    )
    public static boolean chicken = false;
    @Checkbox(
            name = "Hide wolfs"
    )
    public static boolean wolf = false;
    @Checkbox(
            name = "Hide rabbits"
    )
    public static boolean rabbit = false;
}

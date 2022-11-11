package kr.syeyoung.dungeonsguide.oneconfig.secrets;

import cc.polyfrost.oneconfig.config.annotations.Checkbox;

public class PathfindToALlPage {
    @Checkbox(
            name = "Bats",
            description = "This feature will trigger pathfind to all bats in this room when entering a room"
    )
    public static boolean pfTaBAT = true;
    @Checkbox(
            name = "Chests",
            description = "This feature will trigger pathfind to all chests in this room when entering a room"
    )
    public static boolean pfTaCHEST = true;
    @Checkbox(
            name = "Item Drops",
            description = "This feature will trigger pathfind to all itemdrops in this room when entering a room"
    )
    public static boolean pfTaITEMDROP = true;
    @Checkbox(
            name = "Essance",
            description = "This feature will trigger pathfind to all essences in this room when entering a room"
    )
    public static boolean pfTaESSENCE = true;
}

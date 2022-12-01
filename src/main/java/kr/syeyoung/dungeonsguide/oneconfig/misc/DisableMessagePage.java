package kr.syeyoung.dungeonsguide.oneconfig.misc;

import cc.polyfrost.oneconfig.config.annotations.Checkbox;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.OptionSize;

public class DisableMessagePage {
    @Switch(
            name = "Enabled",
            size = 2
    )
    public static boolean disableMessages = true;
    @Checkbox(
            name = "Aote block message",
            description = "\"There are blocks in the way!\"",
            size = OptionSize.DUAL
    )
    public static boolean aote = true;
    @Checkbox(
            name = "Ability cooldown message",
            description = "\"This ability is currently on cooldown for 3 more seconds.\"",
            size = OptionSize.DUAL
    )
    public static boolean cooldown = true;
    @Checkbox(
            name = "Ability cooldown message2",
            description = "\"This ability is on cooldown for 3s.\"",
            size = OptionSize.DUAL
    )
    public static boolean cooldown2 = true;


    @Checkbox(
            name = "Grappling hook cooldown",
            description = "\"Whow! Slow down there!\"",
            size = OptionSize.DUAL
    )
    public static boolean grappling = true;

    @Checkbox(
            name = "Zombie Sword Charging",
            description = "\"No more charges, next one in 3s!\"",
            size = OptionSize.DUAL
    )
    public static boolean zombie = true;
    @Checkbox(
            name = "Ability Damage",
            description = "\"Your blahblah hit 42 enemy for a lots of damage\"",
            size = OptionSize.DUAL
    )
    public static boolean ability = true;
    @Checkbox(
            name = "Not enough mana",
            description = "\"You do not have enough mana to do this!\"",
            size = OptionSize.DUAL
    )
    public static boolean mana = true;
    @Checkbox(
            name = "Dungeon Ability Usage",
            description = "\"Used Guided Sheep!\" and such",
            size = OptionSize.DUAL
    )
    public static boolean dungeonability = true;
    @Checkbox(
            name = "Ready to use message",
            description = "\"Blah is ready to use! Press F to activate it!",
            size = OptionSize.DUAL
    )
    public static boolean readytouse = true;
    @Checkbox(
            name = "Ability Available",
            description = "\"blah is now available!\"",
            size = OptionSize.DUAL
    )
    public static boolean available = true;
    @Checkbox(
            name = "Stone Message",
            description = "\"The Stone doesn't seem to do anything here\"",
            size = OptionSize.DUAL
    )
    public static boolean stone = true;
    @Checkbox(
            name = "Voodoo Doll No Target",
            description = "\"No target found!\"",
            size = OptionSize.DUAL
    )
    public static boolean voodotarget = true;

}

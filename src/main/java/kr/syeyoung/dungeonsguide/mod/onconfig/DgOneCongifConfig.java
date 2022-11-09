package kr.syeyoung.dungeonsguide.mod.onconfig;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.config.data.OptionSize;
import cc.polyfrost.oneconfig.libs.universal.UKeyboard;
import cc.polyfrost.oneconfig.utils.commands.annotations.Description;

import java.util.function.Supplier;

public class DgOneCongifConfig extends Config {

    @Switch(
            name = "Debug mode",
            category = "Debug"
    )
    public static boolean DEBUG_MODE = false;
    @Switch(
            name = "Debuggable Map",
            category = "Debug"
    )
    public static boolean DEBUGABLE_MAP = false;
    @Switch(
            name = "Block Caching",
            category = "Debug",
            description = "Cache all world.getBlockState calls"
    )
    public static boolean DEBUG_BLOCKCACHING = true;

    @Switch(
            description = "Display Coordinate Relative to the Dungeon Room and room's rotation",
            name = "Enabled",
            size = OptionSize.DUAL,
            category = "Debug",
            subcategory = "Dungeon cords"
    )
    public static boolean DEBUG_DUNGEON_CORDS = false;
    @Color(
            name = "Color",
            category = "Debug",
            subcategory = "Dungeon cords"
    )
    public static OneColor DUNGEON_CORDS_COLOR = new OneColor(255, 191, 0);


    @Switch(
            name = "Enabled",
            description = "Display Coordinate Relative to the Dungeon Room and room's rotation",
            category = "Debug",
            subcategory = "Dungeon Room Info"
    )
    public static boolean DEBUG_ROOM_INFO = false;
    @Color(
            name = "Color",
            category = "Debug",
            subcategory = "Dungeon Room Info"
    )
    public static OneColor DUNGEON_ROOMINFO_COLOR = new OneColor(255, 255, 255);


    @Switch(
            name = "Enabled",
            category = "Debug",
            subcategory = "Test People"
    )
    public static boolean DEBUG_TEST_PEPOLE = false;

    @Slider(
            name = "Scale",
            min = 1F,
            max = 500F,
            category = "Debug",
            subcategory = "Test People"
    )
    public static float DEBUG_TEST_PEPOLE_SCALE = 2F;


    @Switch(
            name = "Enabled",
            category = "Debug",
            description = "Allow editing dungeon rooms\n\nWarning: using this feature can break or freeze your Minecraft\nThis is only for advanced users only",
            subcategory = "Room Edit"
    )
    public static boolean DEBUG_ROOM_EDIT = false;

    @KeyBind(
            category = "Debug",
            name = "Gui keybind",
            subcategory = "Room Edit"
    )
    public static OneKeyBind DEBUG_ROOMEDIT_KEYBIND = new OneKeyBind(UKeyboard.KEY_R);





    @Switch(
            name = "Blaze Solver",
            size = 2,
            description = "Highlights the blaze that needs to be killed in an blaze room",
            category = "Solvers",
            subcategory = "Blaze Solver"
    )
    public static boolean BLAZE_SOLVER = true;

    @Color(
            name = "Normal Blaze Color",
            category = "Solvers",
            subcategory = "Blaze Solver"
    )
    public static OneColor BLAZE_SOLVER_COLOR_NORMAL = new OneColor(255,255,255, 255);

    @Color(
            name = "Next Blaze Color",
            category = "Solvers",
            subcategory = "Blaze Solver"
    )
    public static OneColor BLAZE_SOLVER_COLOR_NEXT = new OneColor(0,255,0, 255);

    @Color(
            name = "Next up Blaze Color",
            category = "Solvers",
            subcategory = "Blaze Solver"
    )
    public static OneColor BLAZE_SOLVER_COLOR_NEXT_UP = new OneColor(255,255,0, 255);

    @Color(
            name = "Blaze Border Color",
            category = "Solvers",
            subcategory = "Blaze Solver"
    )
    public static OneColor BLAZE_SOLVER_BORDER = new OneColor(255,255,255, 0);








    @Switch(
            name = "Bomb Defuse",
            size = 2,
            description = "Communicates with others dg using key 'F' for solutions and displays it",
            category = "Solvers",
            subcategory = "Bomb Defuse Solver"
    )
    public static boolean BOMB_DEFUSE_SOLVER = false;

    @KeyBind(
            category = "Solvers",
            name = "keybind",
            description = "Press to send solution in chat",
            subcategory = "Bomb Defuse Solver"
    )
    public static OneKeyBind BOMB_DEFUSE_SOLVER_KEYBIND = new OneKeyBind(UKeyboard.KEY_NONE);



    @Switch(
            name = "Pathfind to all",
            category = "Secrets", // optional
            subcategory = "Mode" // optional
    )
    public static boolean pathfindToAll = false; // this is the default value.
    @Switch(
            name = "Blood Rush mode",
            category = "Secrets", // optional
            subcategory = "Mode" // optional
    )
    public static boolean bloodRush = false; // this is the default value.


    void addDependences(Supplier<Boolean> condition, String... deps){
        for (String dep : deps) {
            addDependency(dep, condition);
        }
    }
    void hideMultipleIf(Supplier<Boolean> condition, String... deps){
        for (String dep : deps) {
            hideIf(dep, condition);
        }
    }


    public DgOneCongifConfig() {
        super(new Mod("DG-LTS", ModType.SKYBLOCK, "/gdlogox512.png"), "dgconfig.json");
        initialize();

        hideMultipleIf(() -> !DEBUG_MODE,  "DEBUGABLE_MAP", "DEBUG_BLOCKCACHING", "DEBUG_DUNGEON_CORDS", "DUNGEON_CORDS_COLOR", "DEBUG_ROOM_INFO", "DUNGEON_ROOMINFO_COLOR", "DEBUG_TEST_PEPOLE", "DEBUG_TEST_PEPOLE_SCALE", "DEBUG_ROOMEDIT_KEYBIND", "DEBUG_ROOM_EDIT");

//        addDependency("fieldname", () -> bollthatdetemins);
    }



}

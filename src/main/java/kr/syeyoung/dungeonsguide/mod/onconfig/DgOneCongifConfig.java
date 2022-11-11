package kr.syeyoung.dungeonsguide.mod.onconfig;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.*;
import cc.polyfrost.oneconfig.libs.universal.UKeyboard;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.FeatureDungeonMap;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.TestMap;
import kr.syeyoung.dungeonsguide.mod.onconfig.huds.*;
import kr.syeyoung.dungeonsguide.mod.onconfig.misc.DisableMessage;
import kr.syeyoung.dungeonsguide.mod.onconfig.misc.HighlightMobs;
import kr.syeyoung.dungeonsguide.mod.onconfig.secrets.AutoPathfindPage;
import kr.syeyoung.dungeonsguide.mod.onconfig.secrets.BloodRushPage;
import kr.syeyoung.dungeonsguide.mod.onconfig.secrets.PathfindToALlPage;
import kr.syeyoung.dungeonsguide.mod.onconfig.solvers.*;

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
            name = "Creeper",
            description = "Draws line between prismarine lamps in creeper room",
            category = "Solvers"
    )
    public static boolean creeperSolver = true;

    @Switch(
            name = "Waterboard (Advanced)",
            description = "Calculates solution for waterboard puzzle and displays it to user",
            category = "Solvers"
    )
    public static boolean waterboardSolver = true;
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
            name = "enabled",
            size = 2,
            description = "Calculates solution for box puzzle room, and displays it to user",
            category = "Solvers",
            subcategory = "Box (Advanced)"
    )
    public static boolean SOLVER_BOX = true;


    @Switch(
            name = "disableText",
            description = "Box Puzzle Solver Disable text\", \"Disable 'Type recalc to recalculate solution' showing up on top left.\\nYou can still type recalc to recalc solution after disabling this feature",
            category = "Solvers",
            subcategory = "Box (Advanced)"
    )
    public static boolean SOLVER_BOX_DISABLE_TEXT = false;
    @Slider(
            name = "Line Thickness",
            min = 0,
            max = 10,
            description = "Thickness of the solution line",
            category = "Solvers",
            subcategory = "Box (Advanced)"
    )
    public static float SOLVER_BOX_LINEWIDTH = 1.0F;
    @Color(
            name = "Line Color",
            description = "Color of the solution line",
            category = "Solvers",
            subcategory = "Box (Advanced)"
    )
    public static OneColor SOLVER_BOX_LINE_COLOR = new OneColor(0xFF00FF00);
    @Color(
            name = "Target Color",
            description = "Color of the target button",
            category = "Solvers",
            subcategory = "Box (Advanced)"
    )
    public static OneColor SOLVER_BOX_TARGET_COLOR = new OneColor(0x5500FFFF);
    @Color(
            name = "Text Color Next Step",
            description = "Color of the text (next step)",
            category = "Solvers",
            subcategory = "Box (Advanced)"
    )
    public static OneColor SOLVER_BOX_TEXT_COLOR_NEXT_STEP = new OneColor(0xFF00FF00);
    @Color(
            name = "Text Color Next Step",
            description = "Color of the text (others)",
            category = "Solvers",
            subcategory = "Box (Advanced)"
    )
    public static OneColor SOLVER_BOX_TEXT_COLOR = new OneColor(0xFF000000);





    @Switch(
            name = "enabled",
            size = 2,
            description = "Calculates solution for icepath puzzle and displays it to user",
            category = "Solvers",
            subcategory = "Icepath (Advanced)"
    )
    public static boolean ICE_FILL = true;

    @Page(
            name = "options",
            location = PageLocation.BOTTOM,
            category = "Solvers",
            subcategory = "Icepath (Advanced)"
    )
    public IceFillPage testPage2 = new IceFillPage();


    @Switch(
            name = "enabled",
            size = 2,
            description = "Highlights the correct solution for trivia puzzle",
            category = "Solvers",
            subcategory = "Quiz"
    )
    public static boolean KAHOOT_SOLVER = true;

    @Page(
            name = "options",
            location = PageLocation.BOTTOM,
            category = "Solvers",
            subcategory = "Quiz"
    )
    public KahootPage a = new KahootPage();

    @Switch(
            name = "enabled",
            size = 2,
            description = "Highlights the correct box after clicking on all 3 weirdos",
            category = "Solvers",
            subcategory = "3 weirdos Solver"
    )
    public static boolean riddleSolver = true;
    @Page(
            name = "options",
            location = PageLocation.BOTTOM,
            category = "Solvers",
            subcategory = "3 weirdos Solver"
    )
    public Weirdos b = new Weirdos();


    @Switch(
            name = "enabled",
            size = 2,
            description = "Actively calculates solution for silverfish puzzle and displays it to user",
            category = "Solvers",
            subcategory = "Silverfish (Advanced)"
    )
    public static boolean silverFishSolver = true;
    @Page(
            name = "options",
            location = PageLocation.BOTTOM,
            category = "Solvers",
            subcategory = "Silverfish (Advanced)"
    )
    public Silverfish c = new Silverfish();


    @Switch(
            name = "enabled",
            size = 2,
            description = "Shows teleport pads you've visited in a teleport maze room",
            category = "Solvers",
            subcategory = "Teleport"
    )
    public static boolean teleportSolver = true;
    @Page(
            name = "options",
            location = PageLocation.BOTTOM,
            category = "Solvers",
            subcategory = "Teleport"
    )
    public Teleport d = new Teleport();

    @Switch(
            name = "enabled",
            size = 2,
            description = "Shows the best move that could be taken by player in the tictactoe room",
            category = "Solvers",
            subcategory = "Tictactoe"
    )
    public static boolean ticktaktoeSolver = true;
    @Page(
            name = "options",
            location = PageLocation.BOTTOM,
            category = "Solvers",
            subcategory = "Tictactoe"
    )
    public TicktackToe e = new TicktackToe();


    @Switch(
            name = "enabled",
            size = 2,
            description = "See players through walls",
            category = "dungeon",
            subcategory = "Player ESP"
    )
    public static boolean playerEps = true;

    @Checkbox(
            name = "Hide Mob nametags",
            size = 2,
            description = "Hide mob nametags in dungeon",
            category = "dungeon",
            subcategory = "Mobs"
    )
    public static boolean hideMobNametags = true;

    @Checkbox(
            name = "Highlight bats",
            category = "dungeon",
            subcategory = "Mobs"
    )
    public static boolean highlightBats = true;

    @Checkbox(
            name = "Highlight Starred mobs",
            category = "dungeon",
            subcategory = "Mobs"
    )
    public static boolean highlightStaredMobs = true;

    @Checkbox(
            name = "Highlight Skeleton Masters",
            category = "dungeon",
            subcategory = "Mobs"
    )
    public static boolean highlightSkeletonMasters = true;

    @Page(
            name = "prefrences",
            location = PageLocation.BOTTOM,
            category = "dungeon",
            subcategory = "Mobs"
    )
    static HighlightMobs hb = new HighlightMobs();


    @Page(
            name = "Ping",
            location = PageLocation.BOTTOM,
            category = "HUD"
    )
    public Ping ss = new Ping();

    @Page(
            name = "Boss Health",
            location = PageLocation.BOTTOM,
            category = "HUD"
    )
    public BossHealth aa = new BossHealth();




    @Switch(
            name = "enabled",
            size = 2,
            description = "Automatically accept reparty",
            category = "Misc",
            subcategory = "Auto accept reparty"
    )
    public static boolean autoRp = true;

    @Switch(
            name = "Spirit Boots Fixer",
            description = "Fix Spirit boots messing up with inventory",
            category = "Misc"
    )
    public static boolean spiritBootsFix = true;

    @Switch(
            name = "Collect Speed Score",
            description = "Collect Speed score, run time, and floor and send that to developer's server for speed formula. This data is completely anonymous, opt out of the feature by disabling this feature",
            category = "Misc"
    )
    public static boolean collectSpeedScore = true;

    @Switch(
            name = "Custom Party Finder",
            description = "Custom Party Finder",
            category = "Misc"
    )
    public static boolean customPartyfinder = true;
    @Switch(
            name = "Copy Chat Messages",
            description = "Click on copy to copy",
            category = "Misc"
    )
    public static boolean copyChatMesseges = false;

    @Text(
            name = "Hypixel API key",
            secure = true,
            category = "Misc"
    )
    public static String apikey = "";





    @Switch(
            name = "enabled",
            size = 2,
            description = "Decreases volume of explosions while on skyblock",
            category = "Misc",
            subcategory = "Decrease Explosion sound effect"
    )
    public static boolean decreseExplosionSound = true;
    @Slider(
            name = "Sound Multiplier %",
            min = 1F,
            max = 100F,
            description = "The volume of explosion effect will be multiplied by this value. 0~100",
            category = "Misc",
            subcategory = "Decrease Explosion sound effect"
    )
    public static float explosionDecreseMultiplyer = 10F;



    @Switch(
            name = "enabled",
            size = 2,
            description = "Do not let ability messages show up in chatbox\nclick on Edit for more precise settings",
            category = "Misc",
            subcategory = "Disable ability messages"
    )
    public static boolean disableMessages = true;
    @Page(
            name = "Settings",
            location = PageLocation.BOTTOM,
            category = "Misc",
            subcategory = "Disable ability messages"
    )
    public DisableMessage aaa = new DisableMessage();


    @Page(
            name = "Epic Dungeon Start Countdown",
            location = PageLocation.BOTTOM,
            description = "Shows a cool dungeon start instead of the chat messages",
            category = "HUD"
    )
    private static DungeonMap mp = new DungeonMap();

    @Switch(
            name = "enabled",
            size = 2,
            description = "Awwww",
            category = "Misc",
            subcategory = "Penguins"
    )
    public static boolean penguins = false;


    @Switch(
            name = "enabled",
            category = "Misc",
            subcategory = "Reparty Command From DG"
    )
    public static boolean repartyCommand = false;
    @Info(text = "if you disable, /dg reparty will still work, Auto reparty will still work\nRequires Restart to get applied", type = InfoType.INFO, category = "Misc", subcategory = "Reparty Command From DG")
    public static boolean ignored2; // Useless. Java limitations with @annotation.
    @Text(
            name = "The Command",
            placeholder = "reparty",
            category = "Misc",
            subcategory = "Reparty Command From DG"
    )
    public static String reparty = "reparty";



    @Switch(
            name = "enabled",
            description = "Shows quality of dungeon items (floor, percentage)",
            size = 2,
            category = "Misc",
            subcategory = "Dungeon Item Stats"
    )
    public static boolean dungeonStat = false;

    @Switch(
            name = "Require Shift",
            description = "If shift needs to be pressed in order for this feature to be activated",
            category = "Misc",
            subcategory = "Dungeon Item Stats"
    )
    public static boolean tooltipPrice = false;



    @Info(
            text = "Disables usage of jna for discord rpc support. Breaks any discord related feature in the mod. Requires mod restart to get affected. This feature is only for those whose minecraft crashes due to discord gamesdk crash.",
            type = InfoType.INFO,
            category = "Discord",
            subcategory = "Disable Native Library"
    )
    public static boolean useless;
    @Switch(
            name = "enable",
            size = 2,
            category = "Discord",
            subcategory = "Disable Native Library"
    )
    public static boolean disableDiscd  = true;


    @Page(
            name = "Dungeon Map",
            location = PageLocation.BOTTOM,
            category = "HUD"
    )
    private static DungeonMap map = new DungeonMap();

    @Page(
            name = "CooldownCounter",
            location = PageLocation.BOTTOM,
            category = "HUD"
    )
    private static CooldownCounter cc = new CooldownCounter();

    @Page(
            name = "Terracota timer",
            location = PageLocation.BOTTOM,
            category = "HUD"
    )
    private static TerracotaTimer tt = new TerracotaTimer();

    @Page(
            name = "Display Spirit Bear Summon Percentage",
            description = "Displays spirit bear summon percentage in hud",
            location = PageLocation.BOTTOM,
            category = "HUD"
    )
    private static SpiritBearPrct sbp = new SpiritBearPrct();

    @Page(
            name = "Display name of the room you are in",
            description = "Display name of the room you are in",
            location = PageLocation.BOTTOM,
            category = "HUD"
    )
    private static CurrentRoomName crn = new CurrentRoomName();


    @Page(
            name = "Secret Soul Alert",
            description = "Alert if there is an fairy soul in the room",
            location = PageLocation.BOTTOM,
            category = "HUD"
    )
    private static FairySoulWarning fsrw = new FairySoulWarning();

    @Page(
            name = "Display Spirit bow timer",
            description = "Displays how long until spirit bow gets destroyed",
            location = PageLocation.BOTTOM,
            category = "HUD"
    )
    private static SpiritBowTimer sbt = new SpiritBowTimer();

    @Page(
            name = "Display Current Phase",
            description = "Displays the current phase of bossfight",
            location = PageLocation.BOTTOM,
            category = "HUD"
    )
    private static CurrentBossPhase cbp = new CurrentBossPhase();

    @HUD(
            name = "Debug Map",
            category = "Debug"
    )
    public static TestMap f = new TestMap();



    @Checkbox(
            name = "Render beacons",
            category = "secrets",
            subcategory = "Preferences"
    )
    public static boolean renderSecretBeacons = true;

    @Checkbox(
            name = "Render Destination text",
            category = "secrets",
            subcategory = "Preferences"
    )
    public static boolean renderSecretDestText = true;


//    THETA_STAR("The default pathfinding algorithm. It will generate sub-optimal path quickly."),
//    A_STAR_DIAGONAL("New pathfinding algorithm. It will generate path that looks like the one JPS generates"),
//    A_STAR_FINE_GRID("New pathfinding algorithm. It will generate path that kind of looks like stair"),
//    JPS_LEGACY("The improved pathfinding algorithm. Not suggested for usage. It will have problems on diagonal movements, thus giving wrong routes"),
//    A_STAR_LEGACY("The first pathfinding algorithm. It may have problem on navigating through stairs. This is the one used by Minecraft for mob pathfind.")
    @Dropdown(
            description = "Select pathfind algorithm used by paths",
            name = "Pathfind Algorithm",
            options = {"THETA_STAR", "A_STAR_DIAGONAL", "A_STAR_FINE_GRID", "JPS_LEGACY", "A_STAR_LEGACY"},
            category = "secrets",
            subcategory = "Preferences"
    )
    public static int secretPathfindStrategy = 0;


    @KeyBind(
            name = "Toggle Pathfind Lines",
            description = "A key for toggling pathfound line visibility.\nPress settings to edit the key",
            category = "secrets",
            subcategory = "Preferences"
    )
    public static OneKeyBind togglePathfindKeybind = new OneKeyBind(UKeyboard.KEY_NONE);

    public static boolean togglePathfindStatus = false;

    @KeyBind(
            name = "Freeze Pathfind",
            description = "Freeze Pathfind, meaning the pathfind lines won't change when you move.\nPress settings to edit the key",
            category = "secrets",
            subcategory = "Preferences"
    )
    public static OneKeyBind freezePathfindingKeybinding = new OneKeyBind(UKeyboard.KEY_NONE);
    public static boolean freezePathfindingStatus = false;


    @Dropdown(
            name = "Mode",
            options = {"PathFind to All", "Blood Rush", "Auto pathfind"},
            category = "secrets"
    )
    public static int secretFindMode = 2;



    @Page(
            name = "PathFind to All Mode Settings",
            location = PageLocation.BOTTOM,
            category = "secrets"
    )
    public PathfindToALlPage pftap = new PathfindToALlPage();

    /**
     * This field is in addition to the selector, the user might want to disable the lines temporally and
     * changing the mode would not solve that
     */
    public static boolean bloodRush = false;

    @Page(
            name = "Blood Rush Mode Settings",
            location = PageLocation.BOTTOM,
            category = "secrets"
    )
    public BloodRushPage brp = new BloodRushPage();


    @Page(
            name = "Auto pathfind Mode Settings",
            location = PageLocation.BOTTOM,
            category = "secrets"
    )
    public AutoPathfindPage app = new AutoPathfindPage();




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

    public static AColor oneconftodgcolor(OneColor c){
        return new AColor(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
    }


    public DgOneCongifConfig() {
        super(new Mod("DG-LTS", ModType.SKYBLOCK, "/gdlogox512.png"), "dgconfig.json");
        initialize();

        hideMultipleIf(() -> !DEBUG_MODE,  "DEBUGABLE_MAP", "DEBUG_BLOCKCACHING", "DEBUG_DUNGEON_CORDS", "DUNGEON_CORDS_COLOR", "DEBUG_ROOM_INFO", "DUNGEON_ROOMINFO_COLOR", "DEBUG_TEST_PEPOLE", "DEBUG_TEST_PEPOLE_SCALE", "DEBUG_ROOMEDIT_KEYBIND", "DEBUG_ROOM_EDIT");

        hideMultipleIf(() -> !FeatureDungeonMap.centerMapOnPlayer, "shouldRotateWithPlayer");

        registerKeyBind(BloodRushPage.keybind, () -> {
            DgOneCongifConfig.bloodRush = !DgOneCongifConfig.bloodRush;
            ChatTransmitter.addToQueue( ChatTransmitter.PREFIX + "§fToggled Blood Rush to §e "+(DgOneCongifConfig.bloodRush ? "on":"off"));
        });

        registerKeyBind(togglePathfindKeybind, () -> {
            togglePathfindStatus = !togglePathfindStatus;
            try {
                ChatTransmitter.addToQueue( ChatTransmitter.PREFIX + "§fToggled Pathfind Line visibility to §e"+(togglePathfindStatus ? "on":"off"));
            } catch (Exception ignored) {

            }
        });

        registerKeyBind(freezePathfindingKeybinding, () -> {
            freezePathfindingStatus = !freezePathfindingStatus;
            ChatTransmitter.addToQueue(ChatTransmitter.PREFIX + "§fToggled Pathfind Freeze to §e"+(DgOneCongifConfig.freezePathfindingStatus ? "on":"off"));
        });

    }



}

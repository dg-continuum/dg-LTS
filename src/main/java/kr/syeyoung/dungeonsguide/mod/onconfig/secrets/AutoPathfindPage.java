package kr.syeyoung.dungeonsguide.mod.onconfig.secrets;

import cc.polyfrost.oneconfig.config.annotations.DualOption;
import cc.polyfrost.oneconfig.config.annotations.KeyBind;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.libs.universal.UKeyboard;

public class AutoPathfindPage {

    @DualOption(
            name = "Auto Pathfind to next secret",
            size = 2,
            left = "Auto",
            right = "On keypress"
    )
    public static boolean autoBrowseToNext = true;

    @KeyBind(
            name = "Next Secret Keybind",
            description = "Press to navigate to next best secret"
    )
    public static OneKeyBind keybind = new OneKeyBind(UKeyboard.KEY_NONE);


}

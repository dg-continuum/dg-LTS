package kr.syeyoung.dungeonsguide.oneconfig.secrets;

import cc.polyfrost.oneconfig.config.annotations.KeyBind;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.libs.universal.UKeyboard;

public class BloodRushPage {
    @KeyBind(
            name = "Start Keybind",
            description = "Auto pathfind to witherdoors. \nCan be toggled with key set in settings"
    )
    public static OneKeyBind keybind = new OneKeyBind(UKeyboard.KEY_NONE);
}

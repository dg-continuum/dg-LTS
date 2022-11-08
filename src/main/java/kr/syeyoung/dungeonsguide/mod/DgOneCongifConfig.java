package kr.syeyoung.dungeonsguide.mod;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import kr.syeyoung.dungeonsguide.Main;

public class DgOneCongifConfig extends Config {
    public DgOneCongifConfig() {
        super(new Mod("DG-LTS", ModType.SKYBLOCK, "/gdlogox512.png"), "dgconfig.json");
        initialize();
    }



    @Switch(
            name = "Eat ASS"
    )
    public static boolean eatAss = false;


}

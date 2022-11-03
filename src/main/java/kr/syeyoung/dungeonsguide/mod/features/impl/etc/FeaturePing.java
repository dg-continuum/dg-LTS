package kr.syeyoung.dungeonsguide.mod.features.impl.etc;

import kr.syeyoung.dungeonsguide.mod.features.text.StyledText;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;

import java.util.List;

public class FeaturePing extends TextHUDFeature {

    public FeaturePing() {
        super("Misc", "Epic ping warner", "Shows ping and displays timeout warnings", "misc.epicping", true, getFontRenderer().getStringWidth("Ping: 100"), getFontRenderer().FONT_HEIGHT + 2);
//        "Misc", "Dungeon Item Stats", "Shows quality of dungeon items (floor, percentage)", "tooltip.dungeonitem"
//        "Dungeon.HUDs", "Display Deaths", "Display names of player and death count in dungeon run", "dungeon.stats.deaths", false, getFontRenderer().getStringWidth("longestplayernamepos: 100"), getFontRenderer().FONT_HEIGHT * 6

    }



    @Override
    public boolean isHUDViewable() {
        return true;
    }

    @Override
    public List<String> getUsedTextStyle() {
        return null;
    }

    @Override
    public List<StyledText> getText() {
        return null;
    }
}

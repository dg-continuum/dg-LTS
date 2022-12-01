/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.features.impl.dungeon.huds;

import cc.polyfrost.oneconfig.hud.SingleTextHud;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FeatureDungeonCurrentRoomSecrets extends SingleTextHud {
    transient private int latestCurrSecrets = 0;
    transient private int latestTotalSecrets = 0;

    public FeatureDungeonCurrentRoomSecrets() {
//        super("Dungeon.HUDs",
//                "Display # Secrets in current room",
//                "",
//                "dungeon.stats.secretsroom",
//                true,
//                getFontRenderer().getStringWidth("Secrets In Room: 8/8"),
//                getFontRenderer().FONT_HEIGHT);
        super("Secrets In Room", true);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent events) {
        if (!SkyblockStatus.isOnSkyblock()) return;
        if (events.type != 2) return;
        String text = events.message.getFormattedText();
        if (!text.contains("/")) return;

        int secretsIndex = text.indexOf("Secrets");
        if (secretsIndex != -1) {
            int theindex = 0;
            for (int i = secretsIndex; i >= 0; i--) {
                if (text.startsWith("§7", i)) {
                    theindex = i;
                }
            }
            String it = text.substring(theindex + 2, secretsIndex - 1);

            latestCurrSecrets = Integer.parseInt(it.split("/")[0]);
            latestTotalSecrets = Integer.parseInt(it.split("/")[1]);
        }
    }

    @Override
    protected boolean shouldShow() {
        return SkyblockStatus.isOnDungeon();
    }

    @Override
    protected String getText(boolean example) {
        if (example) {
            return "8/8";
        }
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null) return "";
        return latestCurrSecrets + "/" + latestTotalSecrets;
    }
}

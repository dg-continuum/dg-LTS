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

import cc.polyfrost.oneconfig.hud.TextHud;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.utils.DungeonUtil;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import lombok.val;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeatureDungeonDeaths extends TextHud {
    transient final Pattern deathPattern = Pattern.compile("§r§c ☠ (.+?)§r§7 .+and became a ghost.+");
    transient final Pattern meDeathPattern = Pattern.compile("§r§c ☠ §r§7You .+and became a ghost.+");

    public FeatureDungeonDeaths() {
        super(true);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (event.type == 2) return;
        if (!SkyblockStatus.isOnDungeon()) return;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return;

        String txt = event.message.getFormattedText();
        Matcher m = deathPattern.matcher(txt);
        if (m.matches()) {
            String nickname = TextUtils.stripColor(m.group(1));
            int deaths = context.getDeaths().getOrDefault(nickname, 0);
            context.getDeaths().put(nickname, deaths + 1);
            ChatTransmitter.sendDebugChat(new ChatComponentText("Death verified :: " + nickname + " / " + (deaths + 1)));
        }
        Matcher m2 = meDeathPattern.matcher(txt);
        if (m2.matches()) {
            String nickname = "me";
            int deaths = context.getDeaths().getOrDefault(nickname, 0);
            context.getDeaths().put(nickname, deaths + 1);
            ChatTransmitter.sendDebugChat(new ChatComponentText("Death verified :: me / " + (deaths + 1)));
        }
    }

    @Override
    protected boolean shouldShow() {
        if (!SkyblockStatus.isOnDungeon()) return false;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        return context != null;
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {

        if (example) {

            lines.add("syeyoung: 1");
            lines.add("rioho: 0");
            lines.add("dungeonsguide: 2");
            lines.add("penguinman: 0");
            lines.add("Total Deaths: 3");
            return;
        }


        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if(context == null) return;
        if(context.getDeaths().isEmpty()) return;
        for (val death : context.getDeaths().entrySet()) {
            lines.add(death.getKey() + ": " + death.getValue());
        }
        lines.add("Total Deaths: " + DungeonUtil.getTotalDeaths());
    }


}

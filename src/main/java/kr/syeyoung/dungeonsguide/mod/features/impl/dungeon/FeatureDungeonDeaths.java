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

package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon;

import cc.polyfrost.oneconfig.hud.TextHud;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.events.impl.DungeonDeathEvent;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeatureDungeonDeaths extends TextHud {
    final Pattern deathPattern = Pattern.compile("§r§c ☠ (.+?)§r§7 .+and became a ghost.+");
    final Pattern meDeathPattern = Pattern.compile("§r§c ☠ §r§7You .+and became a ghost.+");

    public FeatureDungeonDeaths() {
        super(true);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static int getTotalDeaths() {
        if (!SkyblockStatus.isOnDungeon()) return 0;
        for (NetworkPlayerInfo networkPlayerInfoIn : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
            String name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
            if (name.contains("Deaths")) {
                String whatever = TextUtils.keepIntegerCharactersOnly(TextUtils.keepScoreboardCharacters(TextUtils.stripColor(name)));
                if (whatever.isEmpty()) break;
                return Integer.parseInt(whatever);
            }
        }
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return 0;
        int d = 0;
        for (Integer value : context.getDeaths().values()) {
            d += value;
        }
        return d;
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
            context.createEvent(new DungeonDeathEvent(nickname, txt, deaths));
            ChatTransmitter.sendDebugChat(new ChatComponentText("Death verified :: " + nickname + " / " + (deaths + 1)));
        }
        Matcher m2 = meDeathPattern.matcher(txt);
        if (m2.matches()) {
            String nickname = "me";
            int deaths = context.getDeaths().getOrDefault(nickname, 0);
            context.getDeaths().put(nickname, deaths + 1);
            context.createEvent(new DungeonDeathEvent(Minecraft.getMinecraft().thePlayer.getName(), txt, deaths));
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
        lines.add("Total Deaths: " + getTotalDeaths());
    }


}

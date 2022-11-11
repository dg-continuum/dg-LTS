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

import cc.polyfrost.oneconfig.hud.SingleTextHud;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.utils.DungeonUtil;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.regex.Pattern;

public class FeatureDungeonMilestone extends SingleTextHud {
    public static final Pattern milestone_pattern = Pattern.compile("§r§e§l(.+) Milestone §r§e(.)§r§7: .+ §r§a(.+)§r");

    public FeatureDungeonMilestone() {
        super("Milestone", true);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (event.type == 2) return;
        if (!SkyblockStatus.isOnDungeon()) return;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return;
        String txt = event.message.getFormattedText();
        if (milestone_pattern.matcher(txt).matches()) {
            context.getMilestoneReached().add(new String[]{
                    TextUtils.formatTime(DungeonContext.getTimeElapsed()),
                    TextUtils.formatTime(DungeonUtil.getTimeElapsed())
            });
            ChatTransmitter.sendDebugChat(new ChatComponentText("Reached Milestone At " + TextUtils.formatTime(DungeonContext.getTimeElapsed()) + " / " + TextUtils.formatTime(DungeonUtil.getTimeElapsed())));
        }
    }

    @Override
    protected String getText(boolean example) {
        if (example) {
            return "9";
        }
        if(Minecraft.getMinecraft().thePlayer == null) return "";
        for (val player : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
            String name = player.getDisplayName() != null ? player.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(player.getPlayerTeam(), player.getGameProfile().getName());
            if (name.startsWith("§r Milestone: §r")) {
                return TextUtils.stripColor(name).substring(13);
            }
        }
        return "";
    }

    @Override
    protected boolean shouldShow() {
        return SkyblockStatus.isOnDungeon();
    }

}

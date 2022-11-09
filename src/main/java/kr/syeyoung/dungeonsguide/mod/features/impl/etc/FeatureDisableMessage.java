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

package kr.syeyoung.dungeonsguide.mod.features.impl.etc;

import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.mod.onconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.mod.onconfig.misc.DisableMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class FeatureDisableMessage extends SimpleFeature implements ChatListener {
    @Data
    @AllArgsConstructor
    public static class MessageData {
        private Pattern pattern;
        private String name;
        private String description;
        private String key;
    }


    public static final Map<String, Pattern> messeges = new HashMap<>();


    static {
        messeges.put("aote", Pattern.compile("§r§cThere are blocks in the way!§r"));
        messeges.put("cooldown", Pattern.compile("§r§cThis ability is currently on cooldown for .+ more seconds?\\.§r"));
        messeges.put("cooldown2", Pattern.compile("§r§cThis ability is on cooldown for .+s\\.§r"));
        messeges.put("grappling", Pattern.compile("§r§cWhow! Slow down there!§r"));
        messeges.put("zombie", Pattern.compile("§r§cNo more charges, next one in §r§e.+§r§cs!§r"));
        messeges.put("ability",Pattern.compile("§r§7Your .+ hit §r§c.+ §r§7enem(?:y|ies) for §r§c.+ §r§7damage\\.§r"));
        messeges.put("mana", Pattern.compile("§r§cYou do not have enough mana to do this!§r"));
        messeges.put("dungeonability", Pattern.compile("§r§aUsed §r.+§r§a!§r"));
        messeges.put("readytouse", Pattern.compile("§r.+§r§a is ready to use! Press §r.+§r§a to activate it!§r"));
        messeges.put("available", Pattern.compile("§r.+ §r§ais now available!§r"));
        messeges.put("stone", Pattern.compile("§r§cThe Stone doesn't seem to do anything here\\.§r"));
        messeges.put( "voodotarget", Pattern.compile("§r§cNo target found!§r"));
    }


    public FeatureDisableMessage() {
        super("Misc.Chat", "", "", "fixes.messagedisable", true);
    }

    @Override
    public boolean isEnabled() {
        return DgOneCongifConfig.disableMessages;
    }

    @Override
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        if (clientChatReceivedEvent.type == 2) return;
        if (!isEnabled()) return;
        if (!SkyblockStatus.isOnSkyblock()) return;
        String msg = clientChatReceivedEvent.message.getFormattedText();

        if(DisableMessage.aote && messeges.get("aote").matcher(msg).matches()){
            clientChatReceivedEvent.setCanceled(true);
            return;
        }
        if(DisableMessage.cooldown && messeges.get("cooldown").matcher(msg).matches()){
            clientChatReceivedEvent.setCanceled(true);
            return;
        }
        if(DisableMessage.cooldown2 && messeges.get("cooldown2").matcher(msg).matches()){
            clientChatReceivedEvent.setCanceled(true);
            return;
        }
        if(DisableMessage.grappling && messeges.get("grappling").matcher(msg).matches()){
            clientChatReceivedEvent.setCanceled(true);
            return;
        }
        if(DisableMessage.zombie && messeges.get("zombie").matcher(msg).matches()){
            clientChatReceivedEvent.setCanceled(true);
            return;
        }
        if(DisableMessage.ability && messeges.get("ability").matcher(msg).matches()){
            clientChatReceivedEvent.setCanceled(true);
            return;
        }
        if(DisableMessage.mana && messeges.get("mana").matcher(msg).matches()){
            clientChatReceivedEvent.setCanceled(true);
            return;
        }
        if(DisableMessage.dungeonability && messeges.get("dungeonability").matcher(msg).matches()){
            clientChatReceivedEvent.setCanceled(true);
            return;
        }
        if(DisableMessage.readytouse && messeges.get("readytouse").matcher(msg).matches()){
            clientChatReceivedEvent.setCanceled(true);
            return;
        }
        if(DisableMessage.stone && messeges.get("stone").matcher(msg).matches()){
            clientChatReceivedEvent.setCanceled(true);
            return;
        }
        if(DisableMessage.voodotarget && messeges.get("voodotarget").matcher(msg).matches()){
            clientChatReceivedEvent.setCanceled(true);
        }

    }
}

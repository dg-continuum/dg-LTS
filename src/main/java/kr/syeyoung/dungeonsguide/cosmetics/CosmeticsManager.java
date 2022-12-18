/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.cosmetics;

import kr.syeyoung.dungeonsguide.cosmetics.data.ActiveCosmetic;
import kr.syeyoung.dungeonsguide.cosmetics.data.CosmeticData;
import kr.syeyoung.dungeonsguide.cosmetics.replacers.chat.ChatReplacer;
import kr.syeyoung.dungeonsguide.cosmetics.replacers.playername.PlayerNameReplacer;
import kr.syeyoung.dungeonsguide.cosmetics.replacers.tab.TabReplacer;
import kr.syeyoung.dungeonsguide.events.impl.PlayerListItemPacketEvent;
import lombok.Getter;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class CosmeticsManager {
    public Map<UUID, CosmeticData> getCosmeticDataMap() {
        return cosmeticDataMap;
    }

    public Map<UUID, ActiveCosmetic> getActiveCosmeticMap() {
        return activeCosmeticMap;
    }

    public Map<String, List<ActiveCosmetic>> getActiveCosmeticByType() {
        return activeCosmeticByType;
    }

    public Map<UUID, List<ActiveCosmetic>> getActiveCosmeticByPlayer() {
        return activeCosmeticByPlayer;
    }

    public Map<String, List<ActiveCosmetic>> getActiveCosmeticByPlayerNameLowerCase() {
        return activeCosmeticByPlayerNameLowerCase;
    }


    private Map<UUID, CosmeticData> cosmeticDataMap = new ConcurrentHashMap<>();

    private Map<UUID, ActiveCosmetic> activeCosmeticMap = new ConcurrentHashMap<>();

    private Map<String, List<ActiveCosmetic>> activeCosmeticByType = new ConcurrentHashMap<>();

    private Map<UUID, List<ActiveCosmetic>> activeCosmeticByPlayer = new ConcurrentHashMap<>();

    private Map<String, List<ActiveCosmetic>> activeCosmeticByPlayerNameLowerCase = new ConcurrentHashMap<>();
    @Getter
    private Set<String> perms = new CopyOnWriteArraySet<>();


    private final ChatReplacer chatReplacer;
    private final TabReplacer tabReplacer;
    private final PlayerNameReplacer playerNameReplacer;

    public CosmeticsManager() {
        this.playerNameReplacer = new PlayerNameReplacer(this);

        this.tabReplacer = new TabReplacer(this);

        this.chatReplacer = new ChatReplacer(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        chatReplacer.consumeEvent(clientChatReceivedEvent);
    }

    @SubscribeEvent
    public void onTabList(PlayerListItemPacketEvent packetPlayerListItem) {
        tabReplacer.consumeEvent(packetPlayerListItem);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void nameFormat(PlayerEvent.NameFormat nameFormat) {
        playerNameReplacer.consumeEvent(nameFormat);
    }

    public void requestActiveCosmetics() {

    }
    public void requestCosmeticsList() {
    }
    public void requestPerms() {
    }
    public void setCosmetic(CosmeticData cosmetic) {
    }

    public void removeCosmetic(ActiveCosmetic activeCosmetic) {
    }

    private void rebuildCaches() {
        activeCosmeticByType = new HashMap<>();
        activeCosmeticByPlayer = new HashMap<>();
        Map<String, List<ActiveCosmetic>> activeCosmeticByPlayerName = new HashMap<>();
        for (ActiveCosmetic value : activeCosmeticMap.values()) {
            CosmeticData cosmeticData = cosmeticDataMap.get(value.getCosmeticData());
            if (cosmeticData != null) {
                List<ActiveCosmetic> cosmeticsByTypeList = activeCosmeticByType.computeIfAbsent(cosmeticData.getCosmeticType(), a-> new CopyOnWriteArrayList<>());
                cosmeticsByTypeList.add(value);
            }
            List<ActiveCosmetic> activeCosmetics = activeCosmeticByPlayer.computeIfAbsent(value.getPlayerUID(), a-> new CopyOnWriteArrayList<>());
            activeCosmetics.add(value);
            activeCosmetics = activeCosmeticByPlayerName.computeIfAbsent(value.getUsername().toLowerCase(), a-> new CopyOnWriteArrayList<>());
            activeCosmetics.add(value);
        }

        this.activeCosmeticByPlayerNameLowerCase = activeCosmeticByPlayerName;
    }

}

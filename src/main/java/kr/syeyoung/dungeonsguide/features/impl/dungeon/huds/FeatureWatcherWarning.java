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
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom;
import kr.syeyoung.dungeonsguide.events.impl.DungeonEndedEvent;
import kr.syeyoung.dungeonsguide.events.impl.DungeonLeftEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class FeatureWatcherWarning extends TextHud {

    private long warning = 0;

    public FeatureWatcherWarning() {
        super(true);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected boolean shouldShow() {
        return warning > System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onDungeonLeft(DungeonLeftEvent event) {
        warning = 0;
    }

    @SubscribeEvent
    public void onDungeonEnd(DungeonEndedEvent event) {
        warning = 0;
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!SkyblockStatus.isOnSkyblock()) return;
        if (event.message.getFormattedText().equals("§r§c[BOSS] The Watcher§r§f: That will be enough for now.§r")) {
            warning = System.currentTimeMillis() + 2500;
            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (context == null) return;
            for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
                if (dungeonRoom != null && dungeonRoom.getColor() == 18)
                    dungeonRoom.setCurrentState(DungeonRoom.RoomState.DISCOVERED);
            }
        }
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {
        lines.add("Watcher finished spawning all mobs!");
    }
}

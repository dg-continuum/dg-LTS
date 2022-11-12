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
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonFairySoul;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.general.GeneralRoomProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.UUID;

public class FeatureSoulRoomWarning extends SingleTextHud {

    public FeatureSoulRoomWarning() {
        super("There is a fairy soul in this room!", true);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick) {
        if (tick.phase == TickEvent.Phase.END && tick.type == TickEvent.Type.CLIENT ) {
            if (!SkyblockStatus.isOnDungeon()) return;
            if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null || !DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getMapProcessor().isInitialized()) return;
            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            if (thePlayer == null) return;
            Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
            DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
            if (dungeonRoom == null) return;
            if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;

            if (!dungeonRoom.getDungeonRoomInfo().getUuid().equals(lastRoomUID)) {
                for (DungeonMechanic value : dungeonRoom.getMechanics().values()) {
                    if (value instanceof DungeonFairySoul)
                        warning = System.currentTimeMillis() + 2500;
                }
                lastRoomUID = dungeonRoom.getDungeonRoomInfo().getUuid();
            }
        }
    }

    @Override
    protected boolean shouldShow() {
        return warning > System.currentTimeMillis();
    }

    private UUID lastRoomUID = UUID.randomUUID();
    private long warning = 0;

    @Override
    protected String getText(boolean example) {
        return "";
    }
}

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

package kr.syeyoung.dungeonsguide.features.impl.dungeon.secret;

import cc.polyfrost.oneconfig.hud.TextHud;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.GeneralRoomProcessor;
import lombok.val;
import lombok.var;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import java.awt.*;
import java.util.List;

public class FeatureActions extends TextHud {
    public FeatureActions() {
        super(false);
    }


    @Override
    protected boolean shouldShow() {
        if (!SkyblockStatus.isOnDungeon()) return false;
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null || !DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getMapProcessor().isInitialized()) return false;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        if (dungeonRoom == null) return false;
        return dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor;
    }


    @Override
    protected void getLines(List<String> lines, boolean example) {
        if(example){
            lines.add("Pathfinding Secret -> Found");
            lines.add("> 1. Move OffsetPoint{x=1,y=42,z=1}");
            lines.add("> 2. Click OffsetPoint{x=1,y=42,z=1}");
            lines.add("> 3. Profit");

            return;
        }


        val thePlayer = Minecraft.getMinecraft().thePlayer;

        val context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if(context == null) return;

        val roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

        val dungeonRoom = context.getRoomMapper().get(roomPt);

        for (val path : ((GeneralRoomProcessor) dungeonRoom.getRoomProcessor()).getPath().values()) {
            lines.add("Pathfinding " + path.getMechanic() + " -> " + path.getState());

            for (var i = Math.max(0,path.getCurrent()-2); i < path.getActions().size(); i++) {
                val lineBuilder = new StringBuilder();
                lineBuilder.append(i == path.getCurrent() ? ">" : " ");
                lineBuilder.append(" ");
                lineBuilder.append(i);
                lineBuilder.append(". ");

                val action = path.getActions().get(i);
                val str = action.toString().split("\n");
                lineBuilder.append(str[0]);
                lineBuilder.append(" (");
                for (var j = 1; j < str.length; j++) {
                    var base = str[j].trim();
                    if (base.startsWith("-")) {
                        base = base.substring(1);
                    }
                    lineBuilder.append(base.trim()).append(" ");
                }

                lineBuilder.append(")");
                lines.add(lineBuilder.toString());
            }
        }
    }
}

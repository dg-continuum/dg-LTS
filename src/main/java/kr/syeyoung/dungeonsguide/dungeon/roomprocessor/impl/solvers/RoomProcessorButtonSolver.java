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

package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.solvers;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.VectorUtils;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.joml.Vector3i;

import java.awt.*;
import java.util.Arrays;

public class RoomProcessorButtonSolver extends GeneralRoomProcessor {
    private final int[] result = new int[12];
    private boolean bugged;

    private Vector3i[] buttons;
    private Vector3i[] woods;

    private long clicked;
    private int clickedButton = -1;

    public RoomProcessorButtonSolver(DungeonRoom dungeonRoom) {
        super(dungeonRoom);

        OffsetPointSet ops = (OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("buttons");
        if (ops == null) {
            bugged = true;
            return;
        }

        buttons = new Vector3i[12];
        woods = new Vector3i[12];
        for (int i = 0; i < ops.getOffsetPointList().size(); i++) {
            buttons[i] = ops.getOffsetPointList().get(i).getVector3i(dungeonRoom);
            woods[i] = buttons[i].add(0, -1, 0);
        }
    }

    @Override
    public void onInteractBlock(PlayerInteractEvent playerInteractEvent) {
        super.onInteractBlock(playerInteractEvent);
        if (bugged) return;

        if (playerInteractEvent.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        for (int i = 0; i < buttons.length; i++) {
            if (playerInteractEvent.pos.equals(buttons[i])) {
                clicked = System.currentTimeMillis();
                clickedButton = i;
                return;
            }
        }
    }

    @Override
    public void chatReceived(IChatComponent chat) {
        super.chatReceived(chat);
        if (bugged) return;

        if (clickedButton == -1) return;
        if (clicked + 500 < System.currentTimeMillis()) return;

        String msg = chat.getFormattedText();
        if (msg.equals("§r§cThis button doesn't seem to do anything...§r")) {
            result[clickedButton] = -1;
            clickedButton = -1;
        } else if (msg.equals("§r§aThis button seems connected to something§r")) {
            Arrays.fill(result, -1);
            if (clickedButton % 4 != 0) result[clickedButton - 1] = 1;
            if (clickedButton % 4 != 3) result[clickedButton + 1] = 1;
            clickedButton = -1;
        } else if (msg.equals("§r§aClick! you Hear the sound of a door opening§r")) {
            Arrays.fill(result, -1);
            result[clickedButton] = 2;
            clickedButton = -1;
        } else if (msg.equals("§r§aWrong button, looks like the system reset!§r")) {
            Arrays.fill(result, 0);
            clickedButton = -1;
        }
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (bugged) return;
        if (VectorUtils.getPlayerVector3i().distance(woods[6]) > 100) return;


        for (int i = 0; i < woods.length; i++) {
            int data = result[i];
            Vector3i pos = woods[i];

            if (data == 0) {
                RenderUtils.highlightBlock(pos, new Color(0, 255, 255, 50), partialTicks, false);
            } else if (data == -1) {
                RenderUtils.highlightBlock(pos, new Color(255, 0, 0, 50), partialTicks, false);
            } else if (data == 1) {
                RenderUtils.highlightBlock(pos, new Color(0, 255, 0, 50), partialTicks, false);
            } else if (data == 2) {
                RenderUtils.highlightBlock(pos, new Color(0, 255, 0, 100), partialTicks, false);
            }
        }
    }
}

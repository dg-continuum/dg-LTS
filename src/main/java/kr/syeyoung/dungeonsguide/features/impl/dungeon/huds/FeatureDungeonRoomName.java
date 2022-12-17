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
import kr.syeyoung.dungeonsguide.dungeon.DungeonFacade;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import lombok.val;

public class FeatureDungeonRoomName extends SingleTextHud {
    public FeatureDungeonRoomName() {
        super("You're in", true);
    }

    @Override
    protected boolean shouldShow() {
        return SkyblockStatus.isOnDungeon() && DungeonFacade.context != null;
    }

    @Override
    protected String getText(boolean example) {
        if (example) {
            return "puzzle-tictactoe";
        }

        val context = DungeonFacade.context;
        if(context == null) return "";

        val dungeonRoom = context.getCurrentRoom();
        if(dungeonRoom == null) return "";
        return dungeonRoom.getDungeonRoomInfo().getName();
    }
}

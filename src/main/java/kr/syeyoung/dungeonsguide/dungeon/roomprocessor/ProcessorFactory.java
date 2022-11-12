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

package kr.syeyoung.dungeonsguide.dungeon.roomprocessor;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.*;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.boxpuzzle.RoomProcessorBoxSolver;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.general.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.icefill.RoomProcessorIcePath2;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.waterpuzzle.RoomProcessorWaterPuzzle;

public class ProcessorFactory {

    public static RoomProcessor createRoomProcessor(String id, DungeonRoom room) {
        switch (id) {
            case "default":
                return new GeneralRoomProcessor(room);

            case "button_5":
                return new RoomProcessorButtonSolver(room);
            case "bossroom":
                return new RoomProcessorRedRoom(room);

            case "puzzle_water_solver":
                return new RoomProcessorWaterPuzzle(room);
            case "puzzle_teleport_solver":
                return new RoomProcessorTeleportMazeSolver(room);
            case "puzzle_riddle_solver":
                return new RoomProcessorRiddle(room);
            case "puzzle_creeper_solver":
                return new RoomProcessorCreeperSolver(room);
            case "puzzle_tictactoe_solver":
                return new RoomProcessorTicTacToeSolver(room);
            case "puzzle_blaze_solver":
                return new RoomProcessorBlazeSolver(room);
            case "puzzle_silverfish":
                return new RoomProcessorIcePath(room);
            case "puzzle_icefill":
                return new RoomProcessorIcePath2(room);
            case "puzzle_box":
                return new RoomProcessorBoxSolver(room);
            case "puzzle_trivia":
                return new RoomProcessorTrivia(room);
            case "puzzle_bombdefuse":
                return new RoomProcessorBombDefuseSolver(room);
            default:
                return null;
        }
    }

}

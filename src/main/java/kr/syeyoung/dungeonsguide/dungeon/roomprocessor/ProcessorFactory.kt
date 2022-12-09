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
package kr.syeyoung.dungeonsguide.dungeon.roomprocessor

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.solvers.*
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.solvers.bombdefuse.RoomProcessorBombDefuseSolver
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.solvers.boxpuzzle.RoomProcessorBoxSolver
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.solvers.icefill.RoomProcessorIcePath2
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.solvers.waterpuzzle.RoomProcessorWaterPuzzle

object ProcessorFactory {
    @JvmStatic
    fun createRoomProcessor(id: String, room: DungeonRoom): RoomProcessor {
        return when (id) {
            "default" -> GeneralRoomProcessor(room)
            "button_5" -> RoomProcessorButtonSolver(room)
            "bossroom" -> RoomProcessorRedRoom(room)
            "puzzle_water_solver" -> RoomProcessorWaterPuzzle(room)
            "puzzle_teleport_solver" -> RoomProcessorTeleportMazeSolver(room)
            "puzzle_riddle_solver" -> RoomProcessorRiddle(room)
            "puzzle_creeper_solver" -> RoomProcessorCreeperSolver(room)
            "puzzle_tictactoe_solver" -> RoomProcessorTicTacToeSolver(room)
            "puzzle_blaze_solver" -> RoomProcessorBlazeSolver(room)
            "puzzle_silverfish" -> RoomProcessorIcePath(room)
            "puzzle_icefill" -> RoomProcessorIcePath2(room)
            "puzzle_box" -> RoomProcessorBoxSolver(room)
            "puzzle_trivia" -> RoomProcessorTrivia(room)
            "puzzle_bombdefuse" -> RoomProcessorBombDefuseSolver(room)
            else -> throw IllegalArgumentException("Tried to create room processor with invalid name $id")
        }
    }
}
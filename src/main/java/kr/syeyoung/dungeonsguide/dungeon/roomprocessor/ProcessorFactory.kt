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
package kr.syeyoung.dungeonsguide.dungeon.pathfinding

import kr.syeyoung.dungeonsguide.dungeon.pathfinding.impl.AStarCornerCut
import kr.syeyoung.dungeonsguide.dungeon.pathfinding.impl.AStarFineGrid
import kr.syeyoung.dungeonsguide.dungeon.pathfinding.impl.JPSPathfinder
import kr.syeyoung.dungeonsguide.dungeon.pathfinding.impl.ThetaStar
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig

fun buildPfStrategy(roomAcessor: DungeonRoomAccessor, mode: Int): PathfinderStrategy {
    return when (mode) {
        0 -> ThetaStar(roomAcessor)
        1 -> AStarCornerCut(roomAcessor)
        2 -> AStarFineGrid(roomAcessor)
        3 -> JPSPathfinder(roomAcessor)
        else -> TODO("implement me")
    }
}

fun buildPfStrategy(roomAcessor: DungeonRoomAccessor): PathfinderStrategy {
    return buildPfStrategy(roomAcessor, DgOneCongifConfig.secretPathfindStrategy)
}
package kr.syeyoung.dungeonsguide.dungeon.pathfinding

import kr.syeyoung.dungeonsguide.dungeon.pathfinding.impl.AStarCornerCut
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig

fun buildPfStrategy(roomAcessor: DungeonRoomAccessor, mode: Int): IPathfinderStrategy {
    return when (mode) {
        1 -> {
            AStarCornerCut(roomAcessor)
        }
        else -> TODO("impmenet me")
    }
}

fun buildPfStrategy(roomAcessor: DungeonRoomAccessor): IPathfinderStrategy {
    return buildPfStrategy(roomAcessor, DgOneCongifConfig.secretPathfindStrategy)
}
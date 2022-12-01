package kr.syeyoung.dungeonsguide.dungeon.pathfinding

import kr.syeyoung.dungeonsguide.dungeon.pathfinding.impl.AStarCornerCut
import kr.syeyoung.dungeonsguide.dungeon.pathfinding.impl.AStarFineGrid
import kr.syeyoung.dungeonsguide.dungeon.pathfinding.impl.JPSPathfinder
import kr.syeyoung.dungeonsguide.dungeon.pathfinding.impl.ThetaStar
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig
import org.joml.Vector3d
import java.util.*

fun buildPfStrategy(roomAcessor: DungeonRoomAccessor, mode: Int): PathfinderStrategy {
    return when (mode) {
        0 -> ThetaStar(roomAcessor)
        1 -> AStarCornerCut(roomAcessor)
        2 -> AStarFineGrid(roomAcessor)
        3 -> JPSPathfinder(roomAcessor)
        else -> ThetaStar(roomAcessor)
    }
}

fun buildPfStrategy(roomAcessor: DungeonRoomAccessor): PathfinderStrategy {
    return buildPfStrategy(roomAcessor, DgOneCongifConfig.secretPathfindStrategy)
}

data class PfJob(val from: Vector3d, val to: Vector3d, val room: DungeonRoomAccessor)
data class PfPath(val path: LinkedList<Vector3d>)
class AStarUtil {
    class Node(val coordinate: Coordinate) {
        var f = Float.MAX_VALUE
        var g = Float.MAX_VALUE
        var lastVisited = 0

        var parent: Node? = null
    }

    data class Coordinate(val x: Int = 0, val y: Int = 0, val z: Int = 0)
}
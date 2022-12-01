package kr.syeyoung.dungeonsguide.dungeon.pathfinding

import org.joml.Vector3d
import java.util.*


abstract class PathfinderStrategy(open val roomAccessor: DungeonRoomAccessor) {
    var route = LinkedList<Vector3d>()

    abstract fun pathfind(from: Vector3d, to:Vector3d, timeout: Float): Boolean
    fun pathfind(job: PfJob): Boolean {
        return pathfind(job.from, job.to, 2000f)
    }
}
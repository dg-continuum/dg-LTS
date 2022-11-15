package kr.syeyoung.dungeonsguide.dungeon.pathfinding

import net.minecraft.util.Vec3
import org.joml.Vector3d
import java.util.*


abstract class IPathfinderStrategy(open val accessor: DungeonRoomAccessor) {
    var route = LinkedList<Vec3>()

    abstract fun pathfind(from: Vector3d, to:Vector3d, timeout: Float): Boolean
}
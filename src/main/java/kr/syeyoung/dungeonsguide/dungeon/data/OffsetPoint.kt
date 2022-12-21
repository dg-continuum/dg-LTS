
package kr.syeyoung.dungeonsguide.dungeon.data

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.room.NewDungeonRoom
import kr.syeyoung.dungeonsguide.utils.VectorUtils
import net.minecraft.block.Block
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import org.joml.Vector3d
import org.joml.Vector3i
import java.io.Serializable
import javax.vecmath.Vector2d

class OffsetPoint : Cloneable, Serializable {
    var x = 0
    var y = 0
    var z = 0

    constructor(x: Int, y: Int, z: Int) {
        this.x = x
        this.y = y
        this.z = z
    }

    constructor(dungeonRoom: DungeonRoom, pos: Vector3i) {
        setPosInWorld(dungeonRoom, pos)
    }

    constructor(dungeonRoom: DungeonRoom, pos: Vec3) {
        setPosInWorld(dungeonRoom, Vector3i(pos.xCoord.toInt(), pos.yCoord.toInt(), pos.zCoord.toInt()))
    }

    fun setPosInWorld(dungeonRoom: DungeonRoom, pos: Vector3i) {
        var vector2d = Vector2d((pos.x - dungeonRoom.min.x).toDouble(), (pos.z - dungeonRoom.min.z).toDouble())
        for (i in 0 until dungeonRoom.roomMatcher.rotation) {
            vector2d = VectorUtils.rotateClockwise(vector2d)
            if (i % 2 == 0) {
                vector2d.x += (dungeonRoom.dungeonRoomInfo.blocks[0].size - 1).toDouble() // + Z
            } else {
                vector2d.x += (dungeonRoom.dungeonRoomInfo.blocks.size - 1).toDouble() // + X
            }
        }
        x = vector2d.x.toInt()
        z = vector2d.y.toInt()
        y = pos.y - dungeonRoom.min.y
    }

    fun toRotatedRelBlockPos(dungeonRoom: DungeonRoom): Vector3i {
        var rot = Vector2d(x.toDouble(), z.toDouble())
        for (i in 0 until dungeonRoom.roomMatcher.rotation) {
            rot = VectorUtils.rotateCounterClockwise(rot)
            if (i % 2 == 0) {
                rot.y += (dungeonRoom.max.z - dungeonRoom.min.z + 1).toDouble() // + Z
            } else {
                rot.y += (dungeonRoom.max.x - dungeonRoom.min.x + 1).toDouble() // + X
            }
        }
        return Vector3i(rot.x.toInt(), y, rot.y.toInt())
    }

    fun toRotatedRelBlockPos(dungeonRoom: NewDungeonRoom): Vector3i {
        var rot = Vector2d(x.toDouble(), z.toDouble())

        rot = VectorUtils.rotateCounterClockwise(rot)
        rot.y += (dungeonRoom.roomShape.max.z - dungeonRoom.roomShape.min.z + 1).toDouble() // + Z


        return Vector3i(rot.x.toInt(), y, rot.y.toInt())
    }

    fun getBlock(dungeonRoom: DungeonRoom): Block {
        val relBp = toRotatedRelBlockPos(dungeonRoom)
        return dungeonRoom.getRelativeBlockAt(relBp.x, relBp.y, relBp.z)
    }

    fun getVector3i(dungeonRoom: DungeonRoom): Vector3i {
        val relBp = toRotatedRelBlockPos(dungeonRoom)
        return dungeonRoom.getRelativeBlockPosAt(relBp.x, relBp.y, relBp.z)
    }

    fun getVector3i(dungeonRoom: NewDungeonRoom): Vector3i {
        val relBp = toRotatedRelBlockPos(dungeonRoom)
        return dungeonRoom.getRelativeBlockPosAt(relBp.x, relBp.y, relBp.z)
    }

    fun getVector3d(dungeonRoom: DungeonRoom): Vector3d {
        val relBp = toRotatedRelBlockPos(dungeonRoom)
        val relativeBlockPosAt = dungeonRoom.getRelativeBlockPosAt(relBp.x, relBp.y, relBp.z)
        return Vector3d(
            relativeBlockPosAt.x.toDouble(),
            relativeBlockPosAt.y.toDouble(),
            relativeBlockPosAt.z.toDouble()
        )
    }

    fun getBlockPos(dungeonRoom: DungeonRoom): BlockPos {
        val relBp = toRotatedRelBlockPos(dungeonRoom)
        val relativeBlockPosAt = dungeonRoom.getRelativeBlockPosAt(relBp.x, relBp.y, relBp.z)
        return BlockPos(relativeBlockPosAt.x, relativeBlockPosAt.y, relativeBlockPosAt.z)
    }

    fun getData(dungeonRoom: DungeonRoom): Int {
        val relBp = toRotatedRelBlockPos(dungeonRoom)
        return dungeonRoom.getRelativeBlockDataAt(relBp.x, relBp.y, relBp.z)
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        return OffsetPoint(x, y, z)
    }

    override fun toString(): String {
        return "OffsetPoint{x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}'
    }

}
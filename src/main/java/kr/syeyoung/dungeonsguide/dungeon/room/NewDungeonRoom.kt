package kr.syeyoung.dungeonsguide.dungeon.room

import kr.syeyoung.dungeonsguide.dungeon.DungeonContext
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic
import kr.syeyoung.dungeonsguide.dungeon.pathfinding.DungeonRoomAccessor
import kr.syeyoung.dungeonsguide.dungeon.room.data.RoomColor
import kr.syeyoung.dungeonsguide.dungeon.room.data.RoomDataBundle
import kr.syeyoung.dungeonsguide.dungeon.room.data.RoomShape
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.RoomProcessor
import kr.syeyoung.dungeonsguide.utils.BlockCache
import net.minecraft.block.Block
import net.minecraft.util.BlockPos
import org.joml.Vector2i
import org.joml.Vector3d
import org.joml.Vector3i


class NewDungeonRoom(val contextHandle: DungeonContext): DungeonRoomAccessor {
    lateinit var roomColor: RoomColor
    lateinit var roomInfo: DungeonRoomInfo
    lateinit var roomShape: RoomDataBundle
    var lShapeCorners: List<Vector3i> = ArrayList()

    private val cachedMechanics: Map<String, DungeonMechanic> = HashMap()
    val mechanics: Map<String, DungeonMechanic>
        get() = roomInfo.mechanics
    lateinit var roomProcessor: RoomProcessor
        private set

    fun updateRoomProcessor() {
//        roomProcessor = createRoomProcessor(roomInfo.processorId, this);
    }

    val discoveredEssence: Map<Vector3i, Boolean> = HashMap()
    val discoveredItemDrops: Map<Vector3d, Boolean> = HashMap()
    val discoveredChests: Map<Vector3i, Int> = HashMap()
    override fun isBlocked(x: Int, y: Int, z: Int): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * MADE BY CHATGPT, IF IT VIOLATES COPYRIGHT PLEASE CONTACT ME
     */
    fun isVectorWithinShape(vector: Vector2i, shape: List<Vector3i>): Boolean {

        // Check if the vector is within the shape by testing if it is inside all the
        // lines formed by the shape's vertices
        var inside = false
        var i = 0
        var j = shape.size - 1
        while (i < shape.size) {
            if (shape[i].y > vector.y != shape[j].y > vector.y && vector.x < (shape[j].x - shape[i].x) * (vector.y - shape[i].y) / (shape[j].y - shape[i].y) + shape[i].x) {
                inside = !inside
            }
            j = i++
        }
        return inside
    }


    fun isWithinBounds(x: Int, z: Int): Boolean {
        return if(roomShape.roomshape != RoomShape.LSHAPE){
            x >= roomShape.min.x && x <= roomShape.max.x && z >= roomShape.min.z && z <= roomShape.max.z
        } else {
            return isVectorWithinShape(Vector2i(x, z), lShapeCorners)
        }
    }

    fun isWithinBoundsAbsolute(pos: Vector3i?): Boolean {
        TODO()
    }

    fun getRelativeBlockAt(x: Int, y: Int, z: Int): Block? {
        // validate x y z's
        return if (isWithinBounds(x, z)) {
            BlockCache.getBlock(Vector3i(x, y, z).add(roomShape.min.x, roomShape.min.y, roomShape.min.z))
        } else {
            null
        }
    }

    fun getRelativeBlockPosAt(x: Int, y: Int, z: Int): Vector3i {
        return Vector3i(x, y, z).add(roomShape.min.x, roomShape.min.y, roomShape.min.z)
    }

    fun getRelativeBlockDataAt(x: Int, y: Int, z: Int): Int {
        // validate x y z's
        if (isWithinBounds(x, z)) {
            val pos = BlockPos(x, y, z).add(roomShape.min.x, roomShape.min.y, roomShape.min.z)
            val blockState = BlockCache.getBlockState(pos)
            return blockState.block.getMetaFromState(blockState)
        }
        return -1
    }


    fun isInRoom(pos: Vector3i): Boolean {
        if (roomShape.roomshape != RoomShape.LSHAPE){
            return pos.x >= roomShape.min.x && pos.y >= roomShape.min.y && pos.z >= roomShape.min.z && pos.x <= roomShape.max.x && pos.y <= roomShape.max.y && pos.z <= roomShape.max.z
        } else {
            TODO("no so easy copilot huh, we have L shape to worry about")
        }
    }

}
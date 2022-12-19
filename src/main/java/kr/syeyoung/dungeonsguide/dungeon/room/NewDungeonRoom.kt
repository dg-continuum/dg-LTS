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
import org.joml.Vector3d
import org.joml.Vector3i

class NewDungeonRoom(val contextHandle: DungeonContext): DungeonRoomAccessor {
    lateinit var min: Vector3i
    lateinit var max: Vector3i
    lateinit var roomColor: RoomColor
    lateinit var roomInfo: DungeonRoomInfo
    lateinit var roomShape: RoomDataBundle

    private val cachedMechanics: Map<String, DungeonMechanic> = HashMap()
    val mechanics: Map<String, DungeonMechanic>
        get() = TODO()
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

    fun isWithinBounds(x: Int, z: Int): Boolean {
        TODO()
    }

    fun isWithinBoundsAbsolute(pos: Vector3i?): Boolean {
        TODO()
    }

    fun getRelativeBlockAt(x: Int, y: Int, z: Int): Block? {
        // validate x y z's
        return if (isWithinBounds(x, z)) {
            BlockCache.getBlock(Vector3i(x, y, z).add(min.x, min.y, min.z))
        } else {
            null
        }
    }

    fun getRelativeBlockPosAt(x: Int, y: Int, z: Int): Vector3i {
        return Vector3i(x, y, z).add(min.x, min.y, min.z)
    }

    fun getRelativeBlockDataAt(x: Int, y: Int, z: Int): Int {
        // validate x y z's
        if (isWithinBounds(x, z)) {
            val pos = BlockPos(x, y, z).add(min.x, min.y, min.z)
            val blockState = BlockCache.getBlockState(pos)
            return blockState.block.getMetaFromState(blockState)
        }
        return -1
    }


    fun isInRoom(pos: Vector3i): Boolean {
        if (roomShape.roomshape != RoomShape.LSHAPE){
            return pos.x >= min.x && pos.y >= min.y && pos.z >= min.z && pos.x <= max.x && pos.y <= max.y && pos.z <= max.z
        } else {
            TODO("no so easy copilot huh, we have L shape to worry about")
        }
    }

}
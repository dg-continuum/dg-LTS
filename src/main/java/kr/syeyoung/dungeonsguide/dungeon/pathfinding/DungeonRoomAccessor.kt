package kr.syeyoung.dungeonsguide.dungeon.pathfinding

import kr.syeyoung.dungeonsguide.utils.BlockCache
import kr.syeyoung.dungeonsguide.utils.VectorUtils
import net.minecraft.block.state.IBlockState
import org.joml.Vector3d

interface DungeonRoomAccessor {
    fun isBlocked(x: Int,y: Int, z:Int ): Boolean

    fun getBlockState(location: Vector3d): IBlockState? {
        return BlockCache.getBlockState(VectorUtils.Vec3ToBlockPos(location));
    }
}
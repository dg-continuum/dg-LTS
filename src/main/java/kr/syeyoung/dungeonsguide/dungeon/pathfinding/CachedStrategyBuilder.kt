package kr.syeyoung.dungeonsguide.dungeon.pathfinding

import kr.syeyoung.dungeonsguide.dungeon.pathfinding.impl.AStarCornerCut
import org.joml.Vector3d

class CachedStrategyBuilder {
    val cache: MutableMap<Pair<Int, Vector3d>, IPathfinderStrategy> = HashMap()

    fun build(roomAcessor: DungeonRoomAccessor, dest: Vector3d, mode: Int): IPathfinderStrategy {
        return when (mode) {
            1 -> {
                val key = Pair(mode, dest)
                if(cache[key] == null){
                    cache[key] = AStarCornerCut(roomAcessor)
                }
                cache[key]!!
            }
            else -> TODO("impmenet me")
        }
    }


    companion object{
        val INSTANCE: CachedStrategyBuilder = CachedStrategyBuilder()
    }


}
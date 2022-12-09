package kr.syeyoung.dungeonsguide.dungeon.pathfinding

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import kr.syeyoung.dungeonsguide.dungeon.DungeonFacade
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig
import kr.syeyoung.dungeonsguide.utils.VectorUtils
import net.minecraft.entity.Entity
import org.joml.Vector3d
import org.joml.Vector3i
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit


class CachedPathFinder {

    @Suppress("UnstableApiUsage")
    val cache: Cache<PfJob, PfPath> = CacheBuilder.newBuilder()
        .maximumSize(500)
        .expireAfterWrite(20, TimeUnit.SECONDS)
        .build()


    @Suppress("UnstableApiUsage")
    fun pathFind(job: PfJob): PfPath {
        if (DgOneCongifConfig.debugMode) {
            println("Requested pathfinding: from ${job.from} to: ${job.to}")
        }
        return cache.get(job) {
            buildPfStrategy(job.room).let {
                val now = System.nanoTime()
                it.pathfind(job)
                if (DgOneCongifConfig.debugMode) {
                    println("Finished pathfinding in ${System.nanoTime() - now}")
                }
                return@get PfPath(it.route)

            }

        }
    }


    fun CreatePath(
        entityIn: Entity,
        targetPos: Vector3i,
        room: DungeonRoomAccessor,
    ): Future<List<Vector3d>> {
        return DungeonFacade.INSTANCE.ex.submit<List<Vector3d>> {
            return@submit pathFind(
                PfJob(
                    VectorUtils.vec3ToVec3d(entityIn.positionVector),
                    Vector3d(targetPos).add(.5, .5, .5),
                    room
                )
            ).path
        }
    }


}
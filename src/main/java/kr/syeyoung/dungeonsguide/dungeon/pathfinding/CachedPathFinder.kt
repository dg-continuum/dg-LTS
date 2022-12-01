package kr.syeyoung.dungeonsguide.dungeon.pathfinding

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig
import java.util.*
import java.util.concurrent.TimeUnit


class CachedPathFinder {

    @Suppress("UnstableApiUsage")
    val cache: Cache<PfJob, PfPath> = CacheBuilder.newBuilder()
        .maximumSize(500)
        .expireAfterWrite(20, TimeUnit.SECONDS)
        .build()


    @Suppress("UnstableApiUsage")
    fun pathFind(job: PfJob): PfPath {

        return cache.get(job) {
            buildPfStrategy(job.room).let {
                val now = System.nanoTime()
                it.pathfind(job)
                if (DgOneCongifConfig.debugMode){
                    println("Finished pathfinding in ${System.nanoTime() - now}")
                }
                return@get PfPath(it.route)

            }

        }
    }

}
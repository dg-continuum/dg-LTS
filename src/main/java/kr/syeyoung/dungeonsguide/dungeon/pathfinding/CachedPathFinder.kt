package kr.syeyoung.dungeonsguide.dungeon.pathfinding

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.joml.Vector3d
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class CachedPathFinder {
    var ex: ExecutorService = Executors.newCachedThreadPool(
        ThreadFactoryBuilder().setNameFormat("Dg-NewAsyncPathFinder-%d").build()
    )

    val timeout: Float = 2000f

    @Suppress("UnstableApiUsage")
    val cache: Cache<Pair<Vector3d, Vector3d>, PfPath> = CacheBuilder.newBuilder()
        .maximumSize(500)
        .expireAfterWrite(10, TimeUnit.SECONDS)
        .build()


    @Suppress("UnstableApiUsage")
    fun pathFind(from: Vector3d, to: Vector3d, room: DungeonRoomAccessor): CompletableFuture<PfPath> {

        val path: CompletableFuture<PfPath> = CompletableFuture()

        ex.submit {
            path.complete(
                cache.get(Pair(from, to)) {

                    buildPfStrategy(room).let {
                        it.pathfind(from, to, timeout)
                        return@get PfPath(it.route)
                    }

                }
            )
        }
        return path
    }

}
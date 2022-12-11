package kr.syeyoung.dungeonsguide.dungeon

import com.google.common.util.concurrent.ThreadFactoryBuilder
import kr.syeyoung.dungeonsguide.Main
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoomInfoRegistry.loadAll
import kr.syeyoung.dungeonsguide.dungeon.pathfinding.CachedPathFinder
import kr.syeyoung.dungeonsguide.dungeon.pathfinding.impl.ThetaStar
import net.minecraftforge.common.MinecraftForge
import org.joml.Vector3d
import org.joml.Vector3i
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class DungeonFacade {
    lateinit var cachedPathFinder: CachedPathFinder
    lateinit var ex: ExecutorService


    init {
        INSTANCE = this
    }

    fun init() {
        val dgEventListener = DungeonListener()
        MinecraftForge.EVENT_BUS.register(dgEventListener)
        loadAll(Main.getConfigDir())
        ex = Executors.newCachedThreadPool(
            ThreadFactoryBuilder().setNameFormat("Dg-AsyncPathFinder-%d").build()
        )
        cachedPathFinder = CachedPathFinder()
    }

    fun calculatePathLenght(from: Vector3i, to: Vector3i, r: DungeonRoom?): Float {
        var distance = -1f
        val fromv3 = Vector3d(from.x.toDouble(), from.y.toDouble(), from.z.toDouble())
        val tov3 = Vector3d(to.x.toDouble(), to.y.toDouble(), to.z.toDouble())
        val a = genPathfind(fromv3, tov3, r)
        val b: List<Vector3d>
        b = try {
            a.get()
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } catch (e: ExecutionException) {
            throw RuntimeException(e)
        }
        var last: Vector3d? = null
        val iterator = b.iterator()
        while (iterator.hasNext()) {
            val vec3 = iterator.next()
            if (last != null) {
                distance += vec3.distance(last).toFloat()
            }
            last = vec3
        }
        return distance
    }

    fun genPathfind(from: Vector3d?, to: Vector3d?, room: DungeonRoom?): Future<List<Vector3d>> {
        return ex.submit<List<Vector3d>> {
            val pathFinder = ThetaStar(room!!)
            pathFinder.pathfind(from!!, to!!, 100f)
            pathFinder.route
        }
    }

    companion object {
        @JvmField
        var context: DungeonContext? = null
        lateinit var INSTANCE: DungeonFacade
    }
}
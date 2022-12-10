package kr.syeyoung.dungeonsguide.dungeon.actions.impl

import kr.syeyoung.dungeonsguide.DungeonsGuide
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRouteProperties
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import net.minecraft.entity.Entity
import net.minecraftforge.event.entity.living.LivingDeathEvent
import java.awt.Color
import java.util.function.Predicate

class ActionKill(private val target: OffsetPoint) : AbstractAction() {
    var predicate = Predicate { _: Entity? -> false }
    var radius = 0
    private var killed = false

    override fun isComplete(dungeonRoom: DungeonRoom?): Boolean {
        val spawn = target.getVector3i(dungeonRoom)
        for (el in DungeonsGuide.getDungeonsGuide().dungeonFacade.context.killedBats) {
            DungeonsGuide.getDungeonsGuide().dungeonFacade.context.batSpawnedLocations[el]?.let {
                if (it.distance(spawn) < 100){
                    return true
                }
            }
        }
        return killed
    }

    override fun onLivingDeath(
        dungeonRoom: DungeonRoom?,
        event: LivingDeathEvent?,
        actionRouteProperties: ActionRouteProperties?
    ) {
        if (killed) {
            return
        } else {
            val spawnLoc =
                DungeonsGuide.getDungeonsGuide().dungeonFacade.context.batSpawnedLocations[event!!.entity.entityId]
                    ?: return
            if (target.getVector3i(dungeonRoom)
                    .distanceSquared(spawnLoc.x, spawnLoc.y, spawnLoc.z) > radius * radius
            ) return
            if (!predicate.test(event.entity)) return
            killed = true
        }
    }

    override fun onRenderWorld(
        dungeonRoom: DungeonRoom?,
        partialTicks: Float,
        actionRouteProperties: ActionRouteProperties?,
        flag: Boolean
    ) {
        val pos = target.getVector3i(dungeonRoom)
        RenderUtils.highlightBlock(pos, Color(0, 255, 255, 50), partialTicks, true)
        RenderUtils.drawTextAtWorld(
            "Spawn",
            pos.x + 0.5f,
            pos.y + 0.3f,
            pos.z + 0.5f,
            -0x100,
            0.02f,
            false,
            false,
            partialTicks
        )
    }

    override fun toString(): String {
        return """
            KillEntity
            - target: $target
            - radius: $radius
            - predicate: ${if (predicate.test(null)) "null" else predicate.javaClass.simpleName}
            """.trimIndent()
    }
}
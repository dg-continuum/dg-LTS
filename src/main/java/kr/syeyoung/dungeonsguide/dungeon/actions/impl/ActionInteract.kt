package kr.syeyoung.dungeonsguide.dungeon.actions.impl

import kr.syeyoung.dungeonsguide.dungeon.DungeonFacade
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionPlanProperties
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.events.impl.PlayerInteractEntityEvent
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import net.minecraft.entity.Entity
import java.awt.Color
import java.util.function.Predicate

class ActionInteract(private val target: OffsetPoint) : AbstractAction() {
    var predicate = Predicate { _: Entity? -> false }
    var radius = 0
    private var interacted = false

    override fun isComplete(dungeonRoom: DungeonRoom): Boolean {
        return interacted
    }

    override fun onLivingInteract(
        dungeonRoom: DungeonRoom,
        event: PlayerInteractEntityEvent?,
        actionPlanProperties: ActionPlanProperties?,
    ) {
        if (interacted) {
            return
        }

        val entity = event?.entity ?: return

        val spawnLoc = DungeonFacade.context?.batSpawnedLocations?.get(entity.entityId) ?: return

        if (target.getVector3i(dungeonRoom).distanceSquared(spawnLoc.x, spawnLoc.y, spawnLoc.z) > radius.toLong() * radius) {
            return
        }
        if (!predicate.test(entity)) {
            return
        }
        interacted = true

    }

    override fun onRenderWorld(
        dungeonRoom: DungeonRoom,
        partialTicks: Float,
        actionPlanProperties: ActionPlanProperties?,
        flag: Boolean,
    ) {
        val pos = target.getVector3i(dungeonRoom)
        RenderUtils.highlightBlock(pos, Color(0, 255, 255, 50), partialTicks, true)
        RenderUtils.drawTextAtWorld(
            "Interact",
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
            InteractEntity
            - target: $target
            - radius: $radius
            - predicate: ${if (predicate.test(null)) "null" else predicate.javaClass.simpleName}
            """.trimIndent()
    }
}
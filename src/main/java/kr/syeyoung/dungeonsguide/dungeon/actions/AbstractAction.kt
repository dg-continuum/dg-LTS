package kr.syeyoung.dungeonsguide.dungeon.actions

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.events.impl.PlayerInteractEntityEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent

/**
 * THIS IS A ABSTRACT CLASS CUZ I DONT WANT SO MANY EMPTY OVERRIDES
 */
abstract class AbstractAction {
    var preRequisite: Set<AbstractAction> = HashSet()
    open fun getPreRequisites(dungeonRoom: DungeonRoom): MutableSet<AbstractAction> {
        return preRequisite.toMutableSet()
    }

    open fun onPlayerInteract(
        dungeonRoom: DungeonRoom,
        event: PlayerInteractEvent?,
        actionPlanProperties: ActionPlanProperties?
    ) {
    }

    open fun onRenderWorld(
        dungeonRoom: DungeonRoom,
        partialTicks: Float,
        actionPlanProperties: ActionPlanProperties?,
        flag: Boolean
    ) {
    }

    open fun onLivingDeath(
        dungeonRoom: DungeonRoom,
        event: LivingDeathEvent?,
        actionPlanProperties: ActionPlanProperties?
    ) {
    }

    fun onRenderScreen(dungeonRoom: DungeonRoom, partialTicks: Float, actionPlanProperties: ActionPlanProperties?) {}
    open fun onLivingInteract(
        dungeonRoom: DungeonRoom,
        event: PlayerInteractEntityEvent?,
        actionPlanProperties: ActionPlanProperties?
    ) {
    }

    open fun onTick(dungeonRoom: DungeonRoom, actionPlanProperties: ActionPlanProperties?) {}
    abstract fun isComplete(dungeonRoom: DungeonRoom): Boolean
}
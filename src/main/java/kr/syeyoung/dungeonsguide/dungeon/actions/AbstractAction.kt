package kr.syeyoung.dungeonsguide.dungeon.actions

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRouteProperties
import kr.syeyoung.dungeonsguide.events.impl.PlayerInteractEntityEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent

/**
 * THIS IS A ABSTRACT CLASS CUZ I DONT WANT SO MANY EMPTY OVERRIDES
 */
abstract class AbstractAction {
    var preRequisite: Set<AbstractAction> = HashSet()
    open fun getPreRequisites(dungeonRoom: DungeonRoom?): MutableSet<AbstractAction> {
        return preRequisite.toMutableSet()
    }

    open fun onPlayerInteract(
        dungeonRoom: DungeonRoom?,
        event: PlayerInteractEvent?,
        actionRouteProperties: ActionRouteProperties?
    ) {
    }

    open fun onRenderWorld(
        dungeonRoom: DungeonRoom?,
        partialTicks: Float,
        actionRouteProperties: ActionRouteProperties?,
        flag: Boolean
    ) {
    }

    open fun onLivingDeath(
        dungeonRoom: DungeonRoom?,
        event: LivingDeathEvent?,
        actionRouteProperties: ActionRouteProperties?
    ) {
    }

    fun onRenderScreen(dungeonRoom: DungeonRoom?, partialTicks: Float, actionRouteProperties: ActionRouteProperties?) {}
    open fun onLivingInteract(
        dungeonRoom: DungeonRoom?,
        event: PlayerInteractEntityEvent?,
        actionRouteProperties: ActionRouteProperties?
    ) {
    }

    open fun onTick(dungeonRoom: DungeonRoom?, actionRouteProperties: ActionRouteProperties?) {}
    abstract fun isComplete(dungeonRoom: DungeonRoom?): Boolean
}
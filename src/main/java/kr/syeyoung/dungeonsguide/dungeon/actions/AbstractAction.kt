/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package kr.syeyoung.dungeonsguide.dungeon.actions

import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRouteProperties
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom
import kr.syeyoung.dungeonsguide.events.impl.PlayerInteractEntityEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent

/**
 * THIS IS A ABSTRACT CLASS CUZ I DONT WANT SO MANY EMPTY OVERRIDES
 */
abstract class AbstractAction {
    var preRequisite: Set<AbstractAction?> = HashSet()
    open fun getPreRequisites(dungeonRoom: DungeonRoom?): Set<AbstractAction?> {
        return preRequisite
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
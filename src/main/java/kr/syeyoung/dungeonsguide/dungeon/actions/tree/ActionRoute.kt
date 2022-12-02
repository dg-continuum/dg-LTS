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
package kr.syeyoung.dungeonsguide.dungeon.actions.tree

import kr.syeyoung.dungeonsguide.chat.ChatTransmitter
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionChangeState
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionComplete
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionMove
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionMoveNearestAir
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom
import kr.syeyoung.dungeonsguide.events.impl.PlayerInteractEntityEvent
import kr.syeyoung.dungeonsguide.utils.VectorUtils
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent

class ActionRoute(
    dungeonRoom: DungeonRoom,
    val mechanic: String,
    val state: String,
    val actionRouteProperties: ActionRouteProperties
) {


    val actions: MutableList<AbstractAction>
    private val dungeonRoom: DungeonRoom
    var current: Int

    init {
        println("Creating Action Route with mechanic:$mechanic State:$state")
        val actionChangeState = ActionChangeState(mechanic, state)
        val tree = ActionTree.buildActionTree(actionChangeState, dungeonRoom)
        actions = ActionTreeUtil.linearifyActionTree(tree)
        actions.add(ActionComplete())
        ChatTransmitter.sendDebugChat("Created ActionRoute with " + actions.size + " steps")
        ChatTransmitter.sendDebugChat("========== STEPS ==========")
        for (action in actions) {
            ChatTransmitter.sendDebugChat(action.toString())
        }
        ChatTransmitter.sendDebugChat("=========== END ===========")
        current = 0
        this.dungeonRoom = dungeonRoom
    }

    operator fun next(): AbstractAction {
        current++
        if (current >= actions.size) {
            current = actions.size - 1
        }
        return currentAction
    }

    fun prev(): AbstractAction {
        current--
        if (current < 0) {
            current = 0
        }
        return currentAction
    }

    val currentAction: AbstractAction
        get() = actions[current]

    fun onPlayerInteract(event: PlayerInteractEvent?) {
        currentAction.onPlayerInteract(dungeonRoom, event, actionRouteProperties)
    }

    fun onLivingDeath(event: LivingDeathEvent?) {
        currentAction.onLivingDeath(dungeonRoom, event, actionRouteProperties)
    }

    fun onRenderWorld(partialTicks: Float, flag: Boolean) {
        if (current - 1 >= 0) {
            val abstractAction = actions[current - 1]
            if (abstractAction is ActionMove && abstractAction.target.getVector3i(dungeonRoom)
                    .distance(VectorUtils.getPlayerVector3i()) >= 5 || abstractAction is ActionMoveNearestAir && abstractAction.target.getVector3i(
                    dungeonRoom
                ).distance(VectorUtils.getPlayerVector3i()) >= 5
            ) {
                abstractAction.onRenderWorld(dungeonRoom, partialTicks, actionRouteProperties, flag)
            }
        }
        currentAction.onRenderWorld(dungeonRoom, partialTicks, actionRouteProperties, flag)
    }

    fun onRenderScreen(partialTicks: Float) {
        currentAction.onRenderScreen(dungeonRoom, partialTicks, actionRouteProperties)
    }

    fun onTick() {
        val currentAction = currentAction
        currentAction.onTick(dungeonRoom, actionRouteProperties)
        if (current - 1 >= 0 && (actions[current - 1] is ActionMove || actions[current - 1] is ActionMoveNearestAir)) actions[current - 1].onTick(
            dungeonRoom,
            actionRouteProperties
        )
        if (dungeonRoom.mechanics[mechanic]!!.getCurrentState(dungeonRoom) == state) {
            current = actions.size - 1
        }
        if (currentAction.isComplete(dungeonRoom)) {
            next()
        }
    }

    fun onLivingInteract(event: PlayerInteractEntityEvent?) {
        currentAction.onLivingInteract(dungeonRoom, event, actionRouteProperties)
    }
}
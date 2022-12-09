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
package kr.syeyoung.dungeonsguide.dungeon.actions.impl

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionState
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonDummy
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonSecret


class ActionChangeState(private val mechanicName: String, private val state: ActionState) : AbstractAction() {

    private val preRequisite2: Set<AbstractAction> = HashSet()
    override fun getPreRequisites(dungeonRoom: DungeonRoom?): MutableSet<AbstractAction> {
        val set: MutableSet<AbstractAction> = HashSet(preRequisite2)
        val mechanic = dungeonRoom!!.mechanics[mechanicName]
        if (mechanic != null) set.addAll(mechanic.getAction(state.name, dungeonRoom))
        return set
    }

    override fun toString(): String {
        return "ChangeState\n- target: $mechanicName\n- state: $state"
    }

    override fun isComplete(dungeonRoom: DungeonRoom?): Boolean {
        val mechanic = dungeonRoom!!.mechanics[mechanicName]
        if (state == ActionState.navigate) {
            return true
        }
        if (mechanic == null) {
            return false
        }
        if (mechanic is DungeonSecret && mechanic.secretType != DungeonSecret.SecretType.CHEST) {
            return true
        }
        return if (mechanic is DungeonDummy) {
            true
        } else mechanic.getCurrentState(dungeonRoom).equals(state.state, ignoreCase = true)
    }
}
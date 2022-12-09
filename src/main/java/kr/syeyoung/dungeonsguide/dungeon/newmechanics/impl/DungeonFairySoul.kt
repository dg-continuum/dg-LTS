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
package kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl

import com.google.common.collect.Sets
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionState
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionChangeState
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionInteract
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionMove
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.dungeon.mechanics.predicates.PredicateArmorStand
import kr.syeyoung.dungeonsguide.dungeon.newmechanics.MechanicType
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import net.minecraft.entity.Entity
import org.joml.Vector3i
import java.awt.Color
import java.util.function.Predicate

class DungeonFairySoul : kr.syeyoung.dungeonsguide.dungeon.newmechanics.DungeonMechanic() {
    var secretPoint = OffsetPoint(0, 0, 0)
    var preRequisite: List<String> = ArrayList()
    override val mechType: MechanicType = MechanicType.Fairysoul
    override fun getAction(state: String, dungeonRoom: DungeonRoom): Set<AbstractAction> {
        return getAbstractActions(state, secretPoint, preRequisite)
    }

    override fun highlight(color: Color, name: String, dungeonRoom: DungeonRoom, partialTicks: Float) {
        val pos: Vector3i = secretPoint.getVector3i(dungeonRoom)
        RenderUtils.highlightBlock(pos, color, partialTicks)
        RenderUtils.drawTextAtWorld(
            "F-$name",
            pos.x + 0.5f,
            pos.y + 0.375f,
            pos.z + 0.5f,
            -0x1,
            0.03f,
            false,
            true,
            partialTicks
        )
        RenderUtils.drawTextAtWorld(
            getCurrentState(dungeonRoom),
            pos.x + 0.5f,
            pos.y + 0f,
            pos.z + 0.5f,
            -0x1,
            0.03f,
            false,
            true,
            partialTicks
        )
    }

    override fun getCurrentState(dungeonRoom: DungeonRoom): String {
        return "no-state"
    }

    override fun getPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        return Sets.newHashSet("navigate")
    }

    override fun getTotalPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        return Sets.newHashSet("no-state", "navigate")
    }

    override fun getRepresentingPoint(dungeonRoom: DungeonRoom): OffsetPoint {
        return secretPoint
    }

    companion object {
        private const val serialVersionUID = 156412742320519783L
        fun getAbstractActions(
            state: String,
            secretPoint: OffsetPoint?,
            preRequisite: List<String>
        ): Set<AbstractAction> {
            require("navigate".equals(state, ignoreCase = true)) { "$state is not valid state for secret" }
            var base: MutableSet<AbstractAction> = HashSet()
            val actionClick = ActionInteract(secretPoint!!)
            actionClick.predicate = (PredicateArmorStand.INSTANCE as Predicate<Entity?>)
            actionClick.radius = 3
            base.add(actionClick)
            base = actionClick.getPreRequisites(null).toMutableSet()
            val actionMove = ActionMove(secretPoint)
            base.add(actionMove)
            base = actionMove.getPreRequisites(null).toMutableSet()
            for (str in preRequisite) {
                if (str.isNotEmpty()) {
                    val toTypedArray = str.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val actionChangeState = ActionChangeState(toTypedArray[0], ActionState.valueOf(toTypedArray[1]))
                    base.add(actionChangeState)
                }
            }
            return base
        }
    }
}
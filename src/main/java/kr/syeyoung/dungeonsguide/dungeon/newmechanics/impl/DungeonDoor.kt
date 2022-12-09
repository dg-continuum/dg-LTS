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
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet
import kr.syeyoung.dungeonsguide.dungeon.newmechanics.MechanicType
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import net.minecraft.init.Blocks
import java.awt.Color

class DungeonDoor : kr.syeyoung.dungeonsguide.dungeon.newmechanics.DungeonMechanic(), kr.syeyoung.dungeonsguide.dungeon.newmechanics.RouteBlocker {
    var secretPoint = OffsetPointSet()
    var closePreRequisite: List<String> = ArrayList()
    var openPreRequisite: List<String> = ArrayList()
    override val mechType: MechanicType = MechanicType.Door
    override fun getAction(state: String, dungeonRoom: DungeonRoom): Set<AbstractAction> {
        require(
            "open".equals(state, ignoreCase = true) || "closed".equals(
                state,
                ignoreCase = true
            )
        ) { "$state is not valid state for door" }
        if (state.equals(getCurrentState(dungeonRoom), ignoreCase = true)) return emptySet()
        val base: MutableSet<AbstractAction> = HashSet()
        if (state.equals("open", ignoreCase = true)) {
            for (str in openPreRequisite) {
                if (str.isEmpty()) continue


                val toTypedArray = str.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()

                val element = ActionChangeState(toTypedArray[0], ActionState.valueOf(toTypedArray[1]))

                base.add(element)
            }
        } else {
            for (str in closePreRequisite) {
                if (str.isEmpty()) continue

                val toTypedArray = str.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val actionChangeState = ActionChangeState(toTypedArray[0], ActionState.valueOf(toTypedArray[1]))

                base.add(actionChangeState)
            }

        }
        return base
    }

    override fun highlight(color: Color, name: String, dungeonRoom: DungeonRoom, partialTicks: Float) {
        if (secretPoint.offsetPointList.isEmpty()) return
        val firstpt = secretPoint.offsetPointList[0]
        val pos = firstpt.getVector3i(dungeonRoom)
        RenderUtils.drawTextAtWorld(
            name,
            pos.x + 0.5f,
            pos.y + 0.75f,
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
            pos.y + 0.25f,
            pos.z + 0.5f,
            -0x1,
            0.03f,
            false,
            true,
            partialTicks
        )
        for (offsetPoint in secretPoint.offsetPointList) {
            RenderUtils.highlightBlock(offsetPoint.getVector3i(dungeonRoom), color, partialTicks)
        }
    }

    override fun isBlocking(dungeonRoom: DungeonRoom): Boolean {
        for (offsetPoint in secretPoint.offsetPointList) {
            if (offsetPoint.getBlock(dungeonRoom) !== Blocks.air) return true
        }
        return false
    }


    override fun getCurrentState(dungeonRoom: DungeonRoom): String {
        return if (isBlocking(dungeonRoom)) "closed" else "open"
    }

    override fun getPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        val currentStatus = getCurrentState(dungeonRoom)
        if (currentStatus.equals(
                "closed",
                ignoreCase = true
            )
        ) return setOf("open") else if (currentStatus.equals("open", ignoreCase = true)) return setOf("closed")
        return emptySet()
    }

    override fun getTotalPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        return Sets.newHashSet("open", "closed")
    }

    override fun getRepresentingPoint(dungeonRoom: DungeonRoom): OffsetPoint {
        var leastY = Int.MAX_VALUE
        var thatPt: OffsetPoint? = null
        for (offsetPoint in secretPoint.offsetPointList) {
            if (offsetPoint.y < leastY) {
                thatPt = offsetPoint
                leastY = offsetPoint.y
            }
        }
        return thatPt!!
    }

    companion object {
        private const val serialVersionUID = -1011605722415475761L
    }
}
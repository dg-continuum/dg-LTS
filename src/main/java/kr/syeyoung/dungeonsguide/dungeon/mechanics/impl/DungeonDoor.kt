package kr.syeyoung.dungeonsguide.dungeon.mechanics.impl

import com.google.common.collect.Sets
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionChangeState
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic
import kr.syeyoung.dungeonsguide.dungeon.mechanics.MechanicType
import kr.syeyoung.dungeonsguide.dungeon.mechanics.RouteBlocker
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import net.minecraft.init.Blocks
import java.awt.Color

class DungeonDoor : DungeonMechanic(), RouteBlocker, Cloneable {
    var secretPoint = OffsetPointSet()
    var closePreRequisite: List<String> = ArrayList()
    var openPreRequisite: List<String> = ArrayList()
    override val mechType: MechanicType = MechanicType.Door

    public override fun clone(): Any {
        return super.clone()
    }

    override fun getAction(state: String, dungeonRoom: DungeonRoom): Set<AbstractAction> {
        require(
            "open".equals(state, ignoreCase = true) || "closed".equals(state, ignoreCase = true)
        ) { "$state is not valid state for door" }


        return HashSet<AbstractAction>().also {
            if (state.equals(getCurrentState(dungeonRoom), ignoreCase = true)) return emptySet()

            (if (state == "open") openPreRequisite else closePreRequisite).forEach { str ->
                DungeonMechanic.disassemblePreRequisite(str)?.let { (name, state) ->
                    it.add(ActionChangeState(name, state))
                }
            }

        }
    }

    override fun highlight(color: Color, name: String, dungeonRoom: DungeonRoom, partialTicks: Float) {
        if (secretPoint.offsetPointList.isEmpty()) return
        val firstpt = secretPoint.offsetPointList[0]
        val pos = firstpt.getVector3i(dungeonRoom)
        RenderUtils.drawTextAtWorld(
            name, pos.x + 0.5f, pos.y + 0.75f, pos.z + 0.5f, -0x1, 0.03f, false, true, partialTicks
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
                "closed", ignoreCase = true
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

}
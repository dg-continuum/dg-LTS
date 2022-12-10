package kr.syeyoung.dungeonsguide.dungeon.mechanics.impl

import com.google.common.collect.Sets
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionState
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionChangeState
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionClick
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionMove
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic
import kr.syeyoung.dungeonsguide.dungeon.mechanics.MechanicType
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import org.joml.Vector3i
import java.awt.Color

class DungeonDummy : DungeonMechanic(), Cloneable {
    var secretPoint = OffsetPoint(0, 0, 0)
    var preRequisite: List<String> = ArrayList()
    override val mechType: MechanicType = MechanicType.Dummy

    public override fun clone(): Any {
        return super.clone()
    }

    override fun getAction(state: String, dungeonRoom: DungeonRoom): Set<AbstractAction> {
//        if (!"navigate".equalsIgnoreCase(state)) throw new IllegalArgumentException(state+" is not valid state for secret");
        val base: MutableSet<AbstractAction>
        base = HashSet()
        var preRequisites = base
        if (state.equals("navigate", ignoreCase = true)) {
            val actionMove = ActionMove(secretPoint)
            preRequisites.add(actionMove)
            preRequisites = actionMove.getPreRequisites(dungeonRoom)
        } else if (state.equals("click", ignoreCase = true)) {
            val actionClick = ActionClick(secretPoint)
            preRequisites.add(actionClick)
            preRequisites = actionClick.getPreRequisites(dungeonRoom)
            val actionMove = ActionMove(secretPoint)
            preRequisites.add(actionMove)
            preRequisites = actionMove.getPreRequisites(dungeonRoom)
        }
        for (str in preRequisite) {
            if (str.isEmpty()) continue
            val toTypedArray = str.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val actionChangeState = ActionChangeState(toTypedArray[0], ActionState.valueOf(toTypedArray[1]))
            preRequisites.add(actionChangeState)
        }

        return base
    }

    override fun highlight(color: Color, name: String, dungeonRoom: DungeonRoom, partialTicks: Float) {
        val pos: Vector3i = secretPoint.getVector3i(dungeonRoom)
        RenderUtils.highlightBlock(pos, color, partialTicks)
        RenderUtils.drawTextAtWorld(
            "D-$name", pos.x + 0.5f, pos.y + 0.375f, pos.z + 0.5f, -0x1, 0.03f, false, true, partialTicks
        )
        RenderUtils.drawTextAtWorld(
            getCurrentState(dungeonRoom), pos.x + 0.5f, pos.y + 0f, pos.z + 0.5f, -0x1, 0.03f, false, true, partialTicks
        )
    }

    override fun getCurrentState(dungeonRoom: DungeonRoom): String {
        return "no-state"
    }

    override fun getPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        return Sets.newHashSet("navigate", "click")
    }

    override fun getTotalPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        return Sets.newHashSet("no-state", "navigate,click")
    }

    override fun getRepresentingPoint(dungeonRoom: DungeonRoom): OffsetPoint {
        return secretPoint
    }

    companion object {
        private const val serialVersionUID = -8449664812034435765L
    }
}
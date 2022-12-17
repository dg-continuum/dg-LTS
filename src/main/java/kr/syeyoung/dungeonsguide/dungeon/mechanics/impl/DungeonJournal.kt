package kr.syeyoung.dungeonsguide.dungeon.mechanics.impl

import com.google.common.collect.Sets
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionChangeState
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionMove
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic
import kr.syeyoung.dungeonsguide.dungeon.mechanics.MechanicType
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import org.joml.Vector3i
import java.awt.Color

class DungeonJournal : DungeonMechanic(), Cloneable {
    var secretPoint = OffsetPoint(0, 0, 0)
    var preRequisite: List<String> = ArrayList()
    override val mechType: MechanicType = MechanicType.Journal

    public override fun clone(): DungeonJournal {
        return DungeonJournal().also {
            it.secretPoint = secretPoint
            it.preRequisite = preRequisite
        }
    }

    override fun getAction(state: String, dungeonRoom: DungeonRoom): Set<AbstractAction> {
        require("navigate".equals(state, ignoreCase = true)) { "$state is not valid state for DungeonJournal" }

        return HashSet<AbstractAction>().also {
            it.add(ActionMove(secretPoint))
            preRequisite.forEach { str ->
                DungeonMechanic.disassemblePreRequisite(str)?.let { (name, state) ->
                    it.add(ActionChangeState(name, state))
                }
            }
        }

    }

    override fun highlight(color: Color, name: String, dungeonRoom: DungeonRoom, partialTicks: Float) {
        val pos: Vector3i = secretPoint.getVector3i(dungeonRoom)
        RenderUtils.highlightBlock(pos, color, partialTicks)
        RenderUtils.drawTextAtWorld(
            "J-$name", pos.x + 0.5f, pos.y + 0.375f, pos.z + 0.5f, -0x1, 0.03f, false, true, partialTicks
        )
        RenderUtils.drawTextAtWorld(
            getCurrentState(dungeonRoom), pos.x + 0.5f, pos.y + 0f, pos.z + 0.5f, -0x1, 0.03f, false, true, partialTicks
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

}
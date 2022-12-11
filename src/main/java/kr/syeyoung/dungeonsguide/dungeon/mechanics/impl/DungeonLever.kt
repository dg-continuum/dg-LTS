package kr.syeyoung.dungeonsguide.dungeon.mechanics.impl

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionChangeState
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionClick
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionMove
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionMoveNearestAir
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic
import kr.syeyoung.dungeonsguide.dungeon.mechanics.MechanicType
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import org.joml.Vector3i
import java.awt.Color

class DungeonLever : DungeonMechanic(), Cloneable {
    var leverPoint = OffsetPoint(0, 0, 0)
    var preRequisite: List<String> = ArrayList()
    var triggering: String? = ""
    override val mechType: MechanicType = MechanicType.Lever

    public override fun clone(): DungeonLever {
        return DungeonLever().also {
            it.leverPoint = leverPoint
            it.preRequisite = ArrayList(preRequisite)
            it.triggering = triggering
        }
    }

    override fun getAction(state: String, dungeonRoom: DungeonRoom): Set<AbstractAction> {
        if (state == getCurrentState(dungeonRoom)) return emptySet()


        return HashSet<AbstractAction>().also {

            when (state.lowercase()) {
                "navigate" -> {
                    it.add(ActionMoveNearestAir(getRepresentingPoint(dungeonRoom)))
                }

                "triggered", "untriggered" -> {
                    it.add(ActionMove(leverPoint))
                    if (!state.equals(getCurrentState(dungeonRoom), ignoreCase = true)) {
                        it.add(ActionClick(leverPoint))
                    }
                }

                else -> throw IllegalArgumentException("Unknown state $state")
            }


            preRequisite.forEach { str ->
                Companion.disassemblePreRequisite(str)?.let { (name, state) ->
                    it.add(ActionChangeState(name, state))
                }
            }
        }
    }

    override fun highlight(color: Color, name: String, dungeonRoom: DungeonRoom, partialTicks: Float) {
        val pos: Vector3i = leverPoint.getVector3i(dungeonRoom)
        RenderUtils.highlightBlock(pos, color, partialTicks)
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
    }


    override fun getCurrentState(dungeonRoom: DungeonRoom): String {
        if (triggering == null) {
            triggering = "null"
        }
        val mechanic = dungeonRoom.mechanics[triggering]
        return if (mechanic == null) {
            "undeterminable"
        } else {
            val state = mechanic.getCurrentState(dungeonRoom)
            if ("open".equals(state, ignoreCase = true)) {
                "triggered"
            } else {
                "untriggered"
            }
        }
    }

    override fun getPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        val currentStatus = getCurrentState(dungeonRoom)
        if (currentStatus.equals("untriggered", ignoreCase = true)) return setOf(
            "navigate", "triggered"
        ) else if (currentStatus.equals("triggered", ignoreCase = true)) return setOf(
            "navigate", "untriggered"
        )
        return setOf("navigate")
    }

    override fun getTotalPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        return setOf("triggered", "untriggered")
    }

    override fun getRepresentingPoint(dungeonRoom: DungeonRoom): OffsetPoint {
        return leverPoint
    }
}
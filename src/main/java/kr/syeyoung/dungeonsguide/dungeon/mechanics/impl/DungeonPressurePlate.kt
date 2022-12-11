package kr.syeyoung.dungeonsguide.dungeon.mechanics.impl

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionChangeState
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionDropItem
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionMove
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionMoveNearestAir
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic
import kr.syeyoung.dungeonsguide.dungeon.mechanics.MechanicType
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import org.joml.Vector3i
import java.awt.Color

class DungeonPressurePlate : DungeonMechanic(), Cloneable {
    override val mechType: MechanicType =
        MechanicType.PressurePlate
    var platePoint = OffsetPoint(0, 0, 0)
    var preRequisite: List<String> = ArrayList()
    var triggering: String? = ""

    public override fun clone(): DungeonPressurePlate {
        return DungeonPressurePlate().apply {
            platePoint = platePoint
            preRequisite = ArrayList(preRequisite)
            triggering = triggering
        }
    }

    override fun getAction(state: String, dungeonRoom: DungeonRoom): Set<AbstractAction> {
        if (state == getCurrentState(dungeonRoom)) {
            return emptySet()
        }


        return HashSet<AbstractAction>().also {

            when (state.lowercase()) {
                "triggered"  -> {
                    it.add(ActionMove(platePoint))
                    it.add(ActionDropItem(platePoint))
                }
                "untriggered" -> {
                    it.add(ActionMove(platePoint))
                }

                "navigate" -> {
                    it.add(ActionMoveNearestAir(getRepresentingPoint(dungeonRoom)))
                }

                else -> throw IllegalArgumentException("$state is not valid state for DungeonPressurePlate")
            }

            preRequisite.forEach { str ->
                Companion.disassemblePreRequisite(str)?.let { (name, state) ->
                    it.add(ActionChangeState(name, state))
                }
            }
        }
    }

    override fun highlight(color: Color, name: String, dungeonRoom: DungeonRoom, partialTicks: Float) {
        val pos: Vector3i = platePoint.getVector3i(dungeonRoom)
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
        if (triggering == null) triggering = "null"
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
        if (currentStatus.equals("triggered", ignoreCase = true)) {
            return setOf("navigate", "untriggered")
        } else if (currentStatus.equals("untriggered", ignoreCase = true)) {
            return setOf("navigate", "triggered")
        }
        return setOf("navigate")
    }

    override fun getTotalPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        return setOf("triggered", "untriggered")
    }

    override fun getRepresentingPoint(dungeonRoom: DungeonRoom): OffsetPoint {
        return platePoint
    }

}
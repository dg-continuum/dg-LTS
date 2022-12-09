package kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl

import com.google.common.collect.Sets
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionState
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionChangeState
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionDropItem
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionMove
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionMoveNearestAir
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.dungeon.newmechanics.MechanicType
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import org.joml.Vector3i
import java.awt.Color

class DungeonPressurePlate : kr.syeyoung.dungeonsguide.dungeon.newmechanics.DungeonMechanic() {
    override val mechType: MechanicType =
        MechanicType.PressurePlate
    var platePoint = OffsetPoint(0, 0, 0)
    var preRequisite: List<String> = ArrayList()
    var triggering: String? = ""
    override fun getAction(state: String, dungeonRoom: DungeonRoom): Set<AbstractAction> {
        if (state == getCurrentState(dungeonRoom)) {
            return emptySet()
        }
        if (state.equals("navigate", ignoreCase = true)) {
            val base: MutableSet<AbstractAction>
            base = HashSet()
            var preRequisites = base
            val actionMove = ActionMoveNearestAir(getRepresentingPoint(dungeonRoom))
            preRequisites.add(actionMove)
            preRequisites = actionMove.getPreRequisites(dungeonRoom).toMutableSet()
            for (str in preRequisite) {
                if (str.isNotEmpty()) {

                    val toTypedArray = str.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    val actionChangeState = ActionChangeState(toTypedArray[0], ActionState.valueOf(toTypedArray[1]))

                    preRequisites.add(actionChangeState)

                }
            }
            return base
        }


        require(
            "triggered".equals(state, ignoreCase = true) || "untriggered".equals(
                state, ignoreCase = true
            )
        ) { "$state is not valid state for secret" }
        if (state.equals(getCurrentState(dungeonRoom), ignoreCase = true)) {
            return emptySet()
        }
        val base: MutableSet<AbstractAction> = HashSet()
        var preRequisites: MutableSet<AbstractAction?> = HashSet()
        if ("triggered".equals(state, ignoreCase = true)) {
            val actionClick = ActionDropItem(platePoint)
            preRequisites.add(actionClick)
            preRequisites = actionClick.getPreRequisites(dungeonRoom).toMutableSet()
        }
        val actionMove = ActionMove(platePoint)
        preRequisites.add(actionMove)
        preRequisites = actionMove.getPreRequisites(dungeonRoom).toMutableSet()
        for (str in preRequisite) {
            if (str.isEmpty()) continue
            val toTypedArray = str.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val actionChangeState = ActionChangeState(toTypedArray[0], ActionState.valueOf(toTypedArray[1]))

            preRequisites.add(actionChangeState)
        }
        return base
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
            return Sets.newHashSet("navigate", "untriggered")
        } else if (currentStatus.equals("untriggered", ignoreCase = true)) {
            return Sets.newHashSet("navigate", "triggered")
        }
        return Sets.newHashSet("navigate")
    }

    override fun getTotalPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        return Sets.newHashSet("triggered", "untriggered")
    }

    override fun getRepresentingPoint(dungeonRoom: DungeonRoom): OffsetPoint {
        return platePoint
    }

    companion object {
        private const val serialVersionUID = 7450034718355390645L
    }
}
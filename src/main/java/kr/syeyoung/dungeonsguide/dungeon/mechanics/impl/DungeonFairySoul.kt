package kr.syeyoung.dungeonsguide.dungeon.mechanics.impl

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionChangeState
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionInteract
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionMove
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic
import kr.syeyoung.dungeonsguide.dungeon.mechanics.MechanicType
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import org.joml.Vector3i
import java.awt.Color
import java.util.function.Predicate

class DungeonFairySoul : DungeonMechanic(), Cloneable {
    var secretPoint = OffsetPoint(0, 0, 0)
    var preRequisite: List<String> = ArrayList()
    override val mechType: MechanicType = MechanicType.Fairysoul
    override fun getAction(state: String, dungeonRoom: DungeonRoom): Set<AbstractAction> {
        return getAbstractActions(state, secretPoint, preRequisite, dungeonRoom)
    }

    public override fun clone(): DungeonFairySoul {
        return DungeonFairySoul().also {
            it.secretPoint = secretPoint
            it.preRequisite = preRequisite
        }
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
        return setOf("navigate")
    }

    override fun getTotalPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        return setOf("no-state", "navigate")
    }

    override fun getRepresentingPoint(dungeonRoom: DungeonRoom): OffsetPoint {
        return secretPoint
    }

    companion object {
        fun getAbstractActions(
            state: String,
            secretPoint: OffsetPoint?,
            preRequisite: List<String>,
            dungeonRoom: DungeonRoom
        ): Set<AbstractAction> {
            require("navigate".equals(state, ignoreCase = true)) { "$state is not valid state for secret" }

            return HashSet<AbstractAction>().also {
                it.add(ActionInteract(secretPoint!!).apply {
                    predicate = Predicate { entity: Entity? -> entity is EntityArmorStand }
                    radius = 3
                })
                it.add(ActionMove(secretPoint))

                preRequisite.forEach { str ->
                    disassemblePreRequisite(str)?.let { (name, state) ->
                        it.add(ActionChangeState(name, state))
                    }
                }
            }
        }
    }
}
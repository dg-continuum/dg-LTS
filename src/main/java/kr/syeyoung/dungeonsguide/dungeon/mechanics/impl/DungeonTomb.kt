package kr.syeyoung.dungeonsguide.dungeon.mechanics.impl

import com.google.common.collect.Sets
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionState
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionBreakWithSuperBoom
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionChangeState
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionMoveNearestAir
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic
import kr.syeyoung.dungeonsguide.dungeon.mechanics.MechanicType
import kr.syeyoung.dungeonsguide.dungeon.mechanics.RouteBlocker
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import net.minecraft.init.Blocks
import java.awt.Color


class DungeonTomb : DungeonMechanic(), RouteBlocker, Cloneable {
    var secretPoint = OffsetPointSet()
    var preRequisite: List<String> = ArrayList()
    override val mechType: MechanicType = MechanicType.Tomb


    public override fun clone(): Any {
        return super.clone()
    }
    override fun getAction(state: String, dungeonRoom: DungeonRoom): Set<AbstractAction> {
        if (state.equals("navigate", ignoreCase = true)) {
            getRepresentingPoint(dungeonRoom)?.let {
                var base: MutableSet<AbstractAction> = HashSet()

                val actionMove = ActionMoveNearestAir(it)
                base.add(actionMove)
                base = actionMove.getPreRequisites(dungeonRoom)

                preRequisite.forEach { str ->
                    if(str.isNotEmpty()){
                        val toTypedArray = str.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()

                        base.add(ActionChangeState(toTypedArray[0], ActionState.turnIntoForm(toTypedArray[1])))
                    }
                }
                return base
            }
        }
        require("open".equals(state, ignoreCase = true)) { "$state is not valid state for tomb" }
        if (!isBlocking(dungeonRoom)) {
            return emptySet()
        }
        val base: MutableSet<AbstractAction>
        base = HashSet()
        var preRequisites = base
        val actionClick = ActionBreakWithSuperBoom(secretPoint.offsetPointList[0])
        preRequisites.add(actionClick)
        preRequisites = actionClick.getPreRequisites(dungeonRoom)
        val actionMove = ActionMoveNearestAir(secretPoint.offsetPointList[0])
        preRequisites.add(actionMove)
        preRequisites = actionMove.getPreRequisites(dungeonRoom)
        for (str in preRequisite) {
            if (str.isEmpty()) continue
            val toTypedArray = str.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val actionChangeState = ActionChangeState(toTypedArray[0], ActionState.turnIntoForm(toTypedArray[1]))

            preRequisites.add(actionChangeState)
        }
        return base
    }

    override fun highlight(color: Color, name: String, dungeonRoom: DungeonRoom, partialTicks: Float) {
        if (secretPoint.offsetPointList.isNotEmpty()) {
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
            secretPoint.offsetPointList.forEach {
                RenderUtils.highlightBlock(it.getVector3i(dungeonRoom), color, partialTicks)
            }
        }
    }

    override fun isBlocking(dungeonRoom: DungeonRoom): Boolean {
        for (offsetPoint in secretPoint.offsetPointList) {
            if (offsetPoint.getBlock(dungeonRoom) !== Blocks.air) return true
        }
        return false
    }

    override fun getCurrentState(dungeonRoom: DungeonRoom): String {
        var b = Blocks.air
        if (!secretPoint.offsetPointList.isEmpty()) b = secretPoint.offsetPointList[0].getBlock(dungeonRoom)
        return if (b === Blocks.air) "open" else "closed"
    }

    override fun getPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        return if (isBlocking(dungeonRoom)) Sets.newHashSet("open", "navigate") else Sets.newHashSet("navigate")
    }

    override fun getTotalPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        return Sets.newHashSet("open", "closed")
    }

    override fun getRepresentingPoint(dungeonRoom: DungeonRoom): OffsetPoint? {
        return if (secretPoint.offsetPointList.size == 0) null else secretPoint.offsetPointList[0]
    }

    companion object {
        private const val serialVersionUID = -7347076019472222115L
    }
}
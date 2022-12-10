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


class DungeonBreakableWall : DungeonMechanic(), RouteBlocker, Cloneable {
    var secretPoint = OffsetPointSet()
    var preRequisite: List<String> = ArrayList()

    public override fun clone(): Any {
        return super.clone()
    }

    override val mechType: MechanicType = MechanicType.BreakableWall

    override fun getAction(state: String, dungeonRoom: DungeonRoom): Set<AbstractAction> {
        var base: MutableSet<AbstractAction> = HashSet()

        var leastY = Int.MAX_VALUE
        var thatPt: OffsetPoint? = null
        for (offsetPoint in secretPoint.offsetPointList) {
            if (offsetPoint.y < leastY) {
                thatPt = offsetPoint
                leastY = offsetPoint.y
            }
        }

        when(state.lowercase()){
            "navigate" -> {
                val actionMove = ActionMoveNearestAir(thatPt!!)
                base.add(actionMove)
                base = actionMove.getPreRequisites(dungeonRoom).toMutableSet()
            }

            "open" -> {
                if (!isBlocking(dungeonRoom)) {
                    return emptySet()
                }
                val actionClick = ActionBreakWithSuperBoom(getRepresentingPoint(dungeonRoom)!!)
                base.add(actionClick)
                base = actionClick.getPreRequisites(dungeonRoom).toMutableSet()

                val actionMove = ActionMoveNearestAir(thatPt!!)
                base.add(actionMove)
                base = actionMove.getPreRequisites(dungeonRoom).toMutableSet()

            }

            else -> throw IllegalStateException("$state is not a valid state for breakable wall")
        }

        preRequisite.forEach { str ->
            if(str.isNotEmpty()){
                val toTypedArray = str.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()

                val element = ActionChangeState(toTypedArray[0], ActionState.turnIntoForm(toTypedArray[1]))
                base.add(element)
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
        var b = Blocks.air
        if (secretPoint.offsetPointList.isNotEmpty()) b = secretPoint.offsetPointList[0].getBlock(dungeonRoom)
        return if (b === Blocks.air) "open" else "closed"
    }

    override fun getPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        return if (isBlocking(dungeonRoom)) Sets.newHashSet("navigate", "open") else Sets.newHashSet("navigate")
    }

    override fun getTotalPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        return Sets.newHashSet("open", "closed")
    }

    override fun getRepresentingPoint(dungeonRoom: DungeonRoom): OffsetPoint? {
        return if (secretPoint.offsetPointList.size == 0) null else secretPoint.offsetPointList[secretPoint.offsetPointList.size / 2]
    }

    companion object {
        private const val serialVersionUID = 1161593374765852217L
    }
}
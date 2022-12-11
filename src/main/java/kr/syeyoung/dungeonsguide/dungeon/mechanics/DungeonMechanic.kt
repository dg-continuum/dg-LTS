package kr.syeyoung.dungeonsguide.dungeon.mechanics

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionState
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import java.awt.Color
import java.io.Serializable

abstract class DungeonMechanic : Serializable {
    abstract val mechType: MechanicType?

    abstract fun getAction(state: String, dungeonRoom: DungeonRoom): Set<AbstractAction>?
    abstract fun highlight(color: Color, name: String, dungeonRoom: DungeonRoom, partialTicks: Float)
    abstract fun getCurrentState(dungeonRoom: DungeonRoom): String?
    abstract fun getPossibleStates(dungeonRoom: DungeonRoom): Set<String?>?
    abstract fun getTotalPossibleStates(dungeonRoom: DungeonRoom): Set<String?>?
    abstract fun getRepresentingPoint(dungeonRoom: DungeonRoom): OffsetPoint?


    fun disassemblePreRequisite(input: String): Pair<String, ActionState>? {
        if(input.isEmpty()){
            return null
        }
        val arr = input.split(":").toTypedArray()

        return Pair(arr[0], ActionState.turnIntoForm(arr[1]))
    }

}
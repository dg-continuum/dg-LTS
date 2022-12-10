package kr.syeyoung.dungeonsguide.dungeon.actions.impl

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionState
import kr.syeyoung.dungeonsguide.dungeon.mechanics.impl.DungeonDummy
import kr.syeyoung.dungeonsguide.dungeon.mechanics.impl.DungeonSecret


class ActionChangeState(private val mechanicName: String, private val state: ActionState) : AbstractAction() {

    private val preRequisite2: Set<AbstractAction> = HashSet()
    override fun getPreRequisites(dungeonRoom: DungeonRoom?): MutableSet<AbstractAction> {
        val set: MutableSet<AbstractAction> = HashSet(preRequisite2)
        val mechanic = dungeonRoom!!.mechanics[mechanicName] ?: return set
        mechanic.getAction(state.name, dungeonRoom)?.let { set.addAll(it) }
        return set
    }

    override fun toString(): String {
        return "ChangeState\n- target: $mechanicName\n- state: $state"
    }

    override fun isComplete(dungeonRoom: DungeonRoom?): Boolean {
        if (state == ActionState.navigate) {
            return true
        }
        dungeonRoom ?: return false

        val mechanic = dungeonRoom.mechanics[mechanicName] ?: return false
        if (mechanic is DungeonSecret && mechanic.secretType != DungeonSecret.SecretType.CHEST) {
            return true
        }
        if (mechanic is DungeonDummy) {
            return true
        }


        return mechanic.getCurrentState(dungeonRoom).equals(state.state, ignoreCase = true)
    }
}
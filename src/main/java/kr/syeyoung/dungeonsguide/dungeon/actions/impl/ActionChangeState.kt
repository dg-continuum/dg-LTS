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
        val mechanic = dungeonRoom!!.mechanics[mechanicName]
        if (mechanic != null) mechanic.getAction(state.name, dungeonRoom)?.let { set.addAll(it) }
        return set
    }

    override fun toString(): String {
        return "ChangeState\n- target: $mechanicName\n- state: $state"
    }

    override fun isComplete(dungeonRoom: DungeonRoom?): Boolean {
        val mechanic = dungeonRoom!!.mechanics[mechanicName]
        if (state == ActionState.navigate) {
            return true
        }
        if (mechanic == null) {
            return false
        }
        if (mechanic is DungeonSecret && mechanic.secretType != DungeonSecret.SecretType.CHEST) {
            return true
        }
        return if (mechanic is DungeonDummy) {
            true
        } else mechanic.getCurrentState(dungeonRoom).equals(state.state, ignoreCase = true)
    }
}
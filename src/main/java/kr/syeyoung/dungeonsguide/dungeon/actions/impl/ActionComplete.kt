package kr.syeyoung.dungeonsguide.dungeon.actions.impl

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction

class ActionComplete : AbstractAction() {
    override fun isComplete(dungeonRoom: DungeonRoom): Boolean {
        return false
    }

    override fun toString(): String {
        return "Completed"
    }
}
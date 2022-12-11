package kr.syeyoung.dungeonsguide.dungeon.actions.impl

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction


class ActionRoot : AbstractAction() {

    override fun isComplete(dungeonRoom: DungeonRoom): Boolean {
        return true
    }

    override fun toString(): String {
        return "Action Root"
    }
}
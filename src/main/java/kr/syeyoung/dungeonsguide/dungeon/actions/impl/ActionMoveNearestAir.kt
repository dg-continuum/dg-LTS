package kr.syeyoung.dungeonsguide.dungeon.actions.impl

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint

class ActionMoveNearestAir(tt: OffsetPoint) : ActionMove(tt) {
    override fun toString(): String {
        return "MoveNearestAir\n- target: $target"
    }
}
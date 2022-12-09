package kr.syeyoung.dungeonsguide.dungeon.newmechanics

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom

interface RouteBlocker {
    fun isBlocking(dungeonRoom: DungeonRoom): Boolean
}
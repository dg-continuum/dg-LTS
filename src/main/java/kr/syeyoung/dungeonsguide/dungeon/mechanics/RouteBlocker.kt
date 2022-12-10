package kr.syeyoung.dungeonsguide.dungeon.mechanics

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom

interface RouteBlocker {
    fun isBlocking(dungeonRoom: DungeonRoom): Boolean
}
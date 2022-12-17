package kr.syeyoung.dungeonsguide.dungeon.pathfinding

interface DungeonRoomAccessor {
    fun isBlocked(x: Int,y: Int, z:Int ): Boolean
}
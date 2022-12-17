package kr.syeyoung.dungeonsguide.dungeon.roomdetection

import org.joml.Vector3i

interface IDungeonRoomDetector {
    fun generateRoomDataBundle(playerPos: Vector3i): RoomDataBundle
}
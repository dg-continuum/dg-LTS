package kr.syeyoung.dungeonsguide.dungeon.pathfinding

import org.joml.Vector3d

data class PfJob(val from: Vector3d, val to: Vector3d, val room: DungeonRoomAccessor)

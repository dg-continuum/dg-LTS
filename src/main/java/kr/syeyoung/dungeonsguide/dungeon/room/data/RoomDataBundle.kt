package kr.syeyoung.dungeonsguide.dungeon.room.data

import org.joml.Vector3i

data class RoomDataBundle(val roomshape: RoomShape, val width: Int, val heigh: Int, val depth: Int, var roomRotation: RoomRotation, val min: Vector3i, val max: Vector3i, val corners: List<Vector3i>)
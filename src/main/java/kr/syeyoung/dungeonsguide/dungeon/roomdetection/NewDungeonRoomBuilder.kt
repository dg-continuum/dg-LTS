package kr.syeyoung.dungeonsguide.dungeon.roomdetection

import kr.syeyoung.dungeonsguide.dungeon.DungeonContext
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoomInfoRegistry.getRoomInfosByShape
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo
import kr.syeyoung.dungeonsguide.dungeon.room.NewDungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.room.data.RoomColor
import kr.syeyoung.dungeonsguide.dungeon.room.data.RoomDataBundle
import kr.syeyoung.dungeonsguide.utils.VectorUtils


object NewDungeonRoomBuilder {
    fun build(contextHandle: DungeonContext): NewDungeonRoom {
        return NewDungeonRoom(contextHandle).apply {
            roomShape = SizeBundleBuilder().generateRoomDataBundle(VectorUtils.getPlayerVector3i())
            roomColor = getRoomColor()
            roomInfo = matchRoom(roomShape, this)
            updateRoomProcessor()
        }
    }

    private fun getRoomColor(): RoomColor {
        TODO("Somehow get room color without using map?? if possible")
    }

    private fun matchRoom(roomSizeBundle: RoomDataBundle, room: NewDungeonRoom): DungeonRoomInfo {
        return getRoomInfosByShape(roomSizeBundle.roomshape)
            .minByOrNull { roomInfo -> calcRoomSimilarityScore(roomInfo, room) }
            ?: throw IllegalStateException("Could not find matching room")
    }

    private fun calcRoomSimilarityScore(roomInfo: DungeonRoomInfo, room: NewDungeonRoom): Int {
        TODO("Calc the cost basing of color and shape and maybe few other signals")
    }
}
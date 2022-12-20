package kr.syeyoung.dungeonsguide.dungeon.roomdetection

import kr.syeyoung.dungeonsguide.dungeon.DungeonContext
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoomInfoRegistry.getRoomInfosByShape
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo
import kr.syeyoung.dungeonsguide.dungeon.room.NewDungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.room.data.RoomColor
import kr.syeyoung.dungeonsguide.dungeon.room.data.RoomDataBundle
import kr.syeyoung.dungeonsguide.dungeon.room.data.RoomShape
import kr.syeyoung.dungeonsguide.utils.VectorUtils
import net.minecraft.block.Block


object NewDungeonRoomBuilder {

    val shapeMap = mapOf(
        1 to RoomShape.ONEBYONE,
        50 to RoomShape.LSHAPE,
        51 to RoomShape.TWOBYTWO,
        273 to RoomShape.ONEBYTHREE,
        3 to RoomShape.ONEBYTWO,
        17 to RoomShape.ONEBYTWO,
        49 to RoomShape.LSHAPE,
        50 to RoomShape.LSHAPE,
        19 to RoomShape.LSHAPE,
        35 to RoomShape.LSHAPE,
        7 to RoomShape.ONEBYTHREE,
        15 to RoomShape.ONEBYFOUR,
        4369 to RoomShape.ONEBYFOUR,
    )

    fun build(contextHandle: DungeonContext): NewDungeonRoom {
        return NewDungeonRoom(contextHandle).apply {
            roomShape = SizeBundleBuilder().generateRoomDataBundle(VectorUtils.getPlayerVector3i())
//            roomColor = getRoomColor()
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

        val res: Array<IntArray> = roomInfo.blocks

        var wrongs = 0
        for (z in res.indices) {
            for (x in res[0].indices) {
                val data = res[z][x]
                if (data != -1) {
                    val b = room.getRelativeBlockAt(x, 0, z)
                    if (b == null || Block.getIdFromBlock(b) != data) {
                        wrongs++
                        if (wrongs > 10) {
                            return wrongs
                        }
                    }
                }
            }
        }
        return wrongs
    }

}
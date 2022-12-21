package kr.syeyoung.dungeonsguide.dungeon.roomdetection

import kr.syeyoung.dungeonsguide.dungeon.DungeonContext
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoomInfoRegistry.getRoomInfosByShape
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo
import kr.syeyoung.dungeonsguide.dungeon.mechanics.impl.DungeonSecret
import kr.syeyoung.dungeonsguide.dungeon.room.NewDungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.room.data.RoomColor
import kr.syeyoung.dungeonsguide.dungeon.room.data.RoomDataBundle
import kr.syeyoung.dungeonsguide.dungeon.room.data.RoomShape
import kr.syeyoung.dungeonsguide.utils.ArrayUtils
import kr.syeyoung.dungeonsguide.utils.VectorUtils
import net.minecraft.block.Block


object NewDungeonRoomBuilder {

    val shapeMap = mapOf(
        1 to RoomShape.ONEBYONE,
        3 to RoomShape.ONEBYTWO,
        17 to RoomShape.ONEBYTWO,
        273 to RoomShape.ONEBYTHREE,
        7 to RoomShape.ONEBYTHREE,
        15 to RoomShape.ONEBYFOUR,
        4369 to RoomShape.ONEBYFOUR,
        50 to RoomShape.LSHAPE,
        49 to RoomShape.LSHAPE,
        50 to RoomShape.LSHAPE,
        19 to RoomShape.LSHAPE,
        35 to RoomShape.LSHAPE,
        51 to RoomShape.TWOBYTWO,
    )

    fun build(contextHandle: DungeonContext, amountOfSecrets:Int=-1): NewDungeonRoom {
        println("building room with $amountOfSecrets secrets")
        return NewDungeonRoom(contextHandle).apply {
            roomShape = SizeBundleBuilder().generateRoomDataBundle(VectorUtils.getPlayerVector3i())
//            roomColor = getRoomColor()
            roomInfo = matchRoom(roomShape, this, amountOfSecrets)
            updateRoomProcessor()
        }
    }

    private fun getRoomColor(): RoomColor {
        TODO("Somehow get room color without using map?? if possible")
    }

    private fun matchRoom(roomSizeBundle: RoomDataBundle, room: NewDungeonRoom, amountOfSecrets: Int = -1): DungeonRoomInfo {
        return getRoomInfosByShape(roomSizeBundle.roomshape)
            .filter { roominfo ->
                if(amountOfSecrets == -1){
                    true
                } else {
                    val roomInfoSecretCount = roominfo.mechanics.filter { (_, mech) -> mech is DungeonSecret }.count()
                    roomInfoSecretCount == amountOfSecrets
                }
            }
            .also {
                println("Found ${it.size} rooms that are ${roomSizeBundle.roomshape} ${if (amountOfSecrets != -1) "and have $amountOfSecrets secrets" else ""}")
            }
            .minByOrNull { roomInfo -> calcRoomSimilarityScore(roomInfo, room) }
            ?: throw IllegalStateException("Could not find matching room")
    }

    private fun calcRoomSimilarityScore(roomInfo: DungeonRoomInfo, room: NewDungeonRoom): Int {
        println("Trying to match ${roomInfo.name}")
        var lowestcost = Int.MAX_VALUE
        for (rotation in 0..3) {
            var res: Array<IntArray> = roomInfo.blocks
            (1..rotation).forEach { _ ->
                res = ArrayUtils.rotateClockwise(res)
            }
            var wrongs = 0
            for (z in res.indices) {
                for (x in res[0].indices) {
                    val data = res[z][x]
                    if (data != -1) {
                        val b = room.getRelativeBlockAt(x, 0, z)
                        if(Block.getIdFromBlock(b) != data){
                            wrongs++
                        } else {
                            println("${Block.getIdFromBlock(b)} is the same as $data")
                        }
                        if (b == null) {
                            wrongs++
                        }
                    }
                }
            }
            println("Rotation $rotation has $wrongs wrongs, lowestcost is $lowestcost")
            if(wrongs < lowestcost){
                lowestcost = wrongs
            }
        }

        return lowestcost
    }

}
package kr.syeyoung.dungeonsguide.dungeon.roomdetection

import kr.syeyoung.dungeonsguide.chat.ChatTransmitter
import kr.syeyoung.dungeonsguide.dungeon.room.data.RoomDataBundle
import kr.syeyoung.dungeonsguide.dungeon.room.data.RoomRotation
import kr.syeyoung.dungeonsguide.dungeon.room.data.RoomShape
import kr.syeyoung.dungeonsguide.utils.BlockCache
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import org.joml.Vector3i
import java.text.DecimalFormat

class SizeBundleBuilder {


    /**
     * this function treats pos as immutable, only use the output of the function
     * pos: the position of the block we want to drive to an edge
     * modifier: a vector that will be added to pos until it hits an edge
     */
    private fun getToEdge(pos: Vector3i, modifier: Vector3i): Vector3i {
        val hit = Vector3i(pos)
        // add modifier until we hit air
        while (!isBlockAir(BlockCache.getBlockState(hit))) {
            hit.add(modifier)
        }
        // back-off one block
        hit.sub(modifier)
        return hit
    }


    private fun getWidthHeight(l1: Int, l2: Int, l3: Int, l4: Int): Pair<Int, Int> {
        val edges = arrayOf(l1, l2, l3, l4)
        edges.sort()
        var width = edges[0]
        var height = edges[2]
        if (height > width) {
            val temp = width
            width = height
            height = temp
        }
        return Pair(width, height)
    }


    fun generateRoomDataBundle(playerPos: Vector3i): RoomDataBundle {
        val topOfRoom = getTopOfRoom(playerPos)


        var shape: RoomShape? = null

        // we shoot 2 lines to left and right of a player
        val leftShot = getToEdge(topOfRoom, Vector3i(1, 0, 0))
        val rightShot = getToEdge(topOfRoom, Vector3i(-1, 0, 0))

        // we shoot two bottom and top lines from the left/right shoots and get their those walls length

        val leftTopShot = getToEdge(leftShot, Vector3i(0, 0, 1))
        val leftBottomShot = getToEdge(leftShot, Vector3i(0, 0, -1))
        val leftEdgeLength = leftTopShot.distance(leftBottomShot)

        val rightTopShot = getToEdge(rightShot, Vector3i(0, 0, 1))
        val rightBottomShot = getToEdge(rightShot, Vector3i(0, 0, -1))
        val rightEdgeLength = rightBottomShot.distance(rightTopShot)

        println("leftEdgeLength: $leftEdgeLength, rightEdgeLength: $rightEdgeLength")
        val topShot = getToEdge(topOfRoom, Vector3i(0, 0, -1))
        val bottomShot = getToEdge(topOfRoom, Vector3i(0, 0, 1))

        val topTopShot = getToEdge(topShot, Vector3i(1, 0, 0))
        val topBottomShot = getToEdge(topShot, Vector3i(-1, 0, 0))
        val topEdgeLength = topBottomShot.distance(topTopShot)

        val bottomTopShot = getToEdge(bottomShot, Vector3i(1, 0, 0))
        val bottomBottomShot = getToEdge(bottomShot, Vector3i(-1, 0, 0))
        val bottomEdgeLength = bottomBottomShot.distance(bottomTopShot)
        println("topEdgeLength: $topEdgeLength, bottomEdgeLength: $bottomEdgeLength")

        val (width, height) = getWidthHeight(
            leftEdgeLength.toInt(), rightEdgeLength.toInt(), topEdgeLength.toInt(), bottomEdgeLength.toInt()
        )
        println("width: $width, height: $height")
        if (leftEdgeLength != rightEdgeLength) {
            shape = RoomShape.LSHAPE
        } else {
            // if width and height are equal, we have a square
            if (width == height) {
                // square rooms are 30 blocks but wtf do I care lmao
                shape = if (width < 40) {
                    RoomShape.ONEBYONE
                } else {
                    RoomShape.TWOBYTWO
                }
            }

            if (width in 61..89) {
                shape = RoomShape.ONEBYTWO
            }

            if (width in 91..119) {
                shape = RoomShape.ONEBYTHREE
            }

            if (width > 120) {
                shape = RoomShape.ONEBYFOUR
            }

        }

        if (shape == null) {
            throw IllegalStateException("RoomShape not found")
        }



//        val roomRotation = TODO("calc the rotation you dhummy")
        val roomRotation = RoomRotation.UP

        val bottomOfRoom = getBottomOfRoom(playerPos)
        ChatTransmitter.addToQueue("Room bottom: ${bottomOfRoom.toString(DecimalFormat("#"))}")
        ChatTransmitter.addToQueue("Room top: ${topOfRoom.toString(DecimalFormat("#"))}")
        val depth = bottomOfRoom.distance(topOfRoom).toInt()
        val min = Vector3i(rightTopShot)
        val max = Vector3i(leftBottomShot)
        min.y = bottomOfRoom.y
        max.y = topOfRoom.y
        ChatTransmitter.addToQueue("Room min: ${rightTopShot.toString(DecimalFormat("#"))}")
        ChatTransmitter.addToQueue("Room max: ${leftBottomShot.toString(DecimalFormat("#"))}")
        return RoomDataBundle(shape, width, height, depth, roomRotation, min = min, max = max, listOf())
    }

    fun getTopOfRoom(userpos: Vector3i): Vector3i {
        val topOfRoom = Vector3i(userpos)
        topOfRoom.y = 255

        // if previous is not air, and current is air stop
        while (isBlockAir(BlockCache.getBlockState(topOfRoom))) {
            topOfRoom.y -= 1
        }

//        topOfRoom.y += 1

        return topOfRoom
    }

    private fun getBottomOfRoom(userpos: Vector3i): Vector3i {
        val posCLone = Vector3i(userpos)
        posCLone.y = 0

        // if previous is not air, and current is air stop
        while (isBlockAir(BlockCache.getBlockState(posCLone))) {
            posCLone.y += 1
        }

        posCLone.y -= 1

        return posCLone
    }


    private fun isBlockAir(blockState: IBlockState): Boolean {
        return blockState.block == Blocks.air
    }


}
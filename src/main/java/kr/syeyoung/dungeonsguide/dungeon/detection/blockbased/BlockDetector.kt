package kr.syeyoung.dungeonsguide.dungeon.detection.blockbased

import kr.syeyoung.dungeonsguide.chat.ChatTransmitter
import kr.syeyoung.dungeonsguide.utils.BlockCache
import kr.syeyoung.dungeonsguide.utils.VectorUtils
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import org.joml.Vector3i
import java.text.DecimalFormat

class BlockDetector {


    /**
     * this function treats pos as immutable, only use the output of the function
     * pos: the position of the block we want to drive to an edge
     * modifier: a vector that will be added to pos until it hits an edge
     */
    fun getToEdge(pos: Vector3i, modifier: Vector3i): Vector3i {
        val hit = pos.clone() as Vector3i
        // add modifier until we hit air
        while (!isBlockAir(BlockCache.getBlockState(hit))) {
            hit.add(modifier)
        }
        // back-off one block
        hit.sub(modifier)
        return hit
    }

    fun getRoomSizeBundle(playerPos: Vector3i): roomDataBundle {
        var roomShape:RoomShape = RoomShape.ONEBYONE
        // we shoot 2 lines to left and right of a player
        val leftShot = getToEdge(playerPos, Vector3i(1, 0, 0))
        val rightShot = getToEdge(playerPos, Vector3i(-1, 0, 0))

        // we shoot two bottom and top lines from the left/right shoots and get thier those walls lenght

        val leftTopShot = getToEdge(leftShot, Vector3i(0, 0, 1))
        val leftBottomShot = getToEdge(leftShot, Vector3i(0, 0, -1))

        val leftEdgeLenght = leftTopShot.distance(leftBottomShot)

        val rightTopShot = getToEdge(rightShot, Vector3i(0, 0, 1))
        val rightBottomShot = getToEdge(rightShot, Vector3i(0, 0, -1))

        val rightEdgeLenght = rightBottomShot.distance(rightTopShot)


        if(leftEdgeLenght != rightEdgeLenght){
            roomShape = RoomShape.LSHAPE
            return roomDataBundle(roomShape, 0,0,0)
        }

        return roomDataBundle(roomShape, 0,0,0)
    }

    fun getTopOfRoom(): Vector3i {
        return getTopOfRoom(VectorUtils.BlockPosToVec3i(Minecraft.getMinecraft().thePlayer.position))
    }

    /**
     * This function appears to determine the top of a room, given a starting position (userpos) in the room.
     * The function first initializes a boolean variable wasAir to true, which indicates whether the previous block was air or not.
     * It then checks the block above the starting position, and sets a variable isAir to true if the block is air, or false otherwise.
     * The function then enters a loop that continues until the current block is air and the previous block was not air.
     * Inside the loop, the function updates the userpos variable by adding 1 to the y-coordinate, which moves the position up by one block.
     * It then updates the wasAir variable with the value of the isAir variable, and checks the block at the new position to update the isAir variable.
     * Once the loop exits, the function returns the position of the block below the current position by subtracting 1 from the y-coordinate of userpos.
     * This should be the top of the room, as the function only stops when it reaches a block of air that is immediately above a non-air block.
     */
    fun getTopOfRoom(userpos: Vector3i): Vector3i {
        var wasAir = true

        var isAir = isBlockAir(BlockCache.getBlockState(userpos.add(0, 1, 1)))

        // if previous is not air, and current is air stop
        while (!(isAir && !wasAir)) {
            wasAir = isAir
            userpos.add(0, 1, 0)
            isAir = isBlockAir(BlockCache.getBlockState(userpos))
        }


        return userpos.sub(0, 1, 0)
    }

    /**
     * Gets 4 corners of a block square surrounded by air blocks
     */
    fun getCornersSquare(startingBlock: Vector3i): List<Vector3i> {

        ChatTransmitter.addToQueue("Y level: ${startingBlock.y}")

        val righttopCorner = Vector3i(startingBlock)

        // get to the top edge
        while (!isBlockAir(BlockCache.getBlockState(righttopCorner))) {
            righttopCorner.add(0, 0, 1)
        }
        // backoff one block
        righttopCorner.add(0, 0, -1)

        // get to right edge
        while (!isBlockAir(BlockCache.getBlockState(righttopCorner))) {
            righttopCorner.add(-1, 0, 0)
        }
        righttopCorner.add(1, 0, 0)

        val bottomRightCorner = Vector3i(righttopCorner)
        // get to bottom edge
        while (!isBlockAir(BlockCache.getBlockState(bottomRightCorner))) {
            bottomRightCorner.add(0, 0, -1)
        }
        bottomRightCorner.add(0, 0, 1)

        val bottomLeftCorner = Vector3i(bottomRightCorner)
        // get to left edge
        while (!isBlockAir(BlockCache.getBlockState(bottomLeftCorner))) {
            bottomLeftCorner.add(1, 0, 0)
        }
        bottomLeftCorner.add(-1, 0, 0)

        // GIRL YES YOU ARE INVITED TO THE FLOP HOUSE

        val topLeftCorner = Vector3i(bottomLeftCorner)
        // get to left edge
        while (!isBlockAir(BlockCache.getBlockState(topLeftCorner))) {
            topLeftCorner.add(0, 0, 1)
        }
        topLeftCorner.add(0, 0, -1)

        ChatTransmitter.addToQueue("right top: ${righttopCorner.toString(DecimalFormat(".###"))} ")
        ChatTransmitter.addToQueue("left top: ${topLeftCorner.toString(DecimalFormat(".###"))} ")
        ChatTransmitter.addToQueue("right bottom: ${bottomRightCorner.toString(DecimalFormat(".###"))} ")
        ChatTransmitter.addToQueue("left bottom: ${bottomLeftCorner.toString(DecimalFormat(".###"))} ")

        return listOf(righttopCorner, bottomRightCorner, bottomLeftCorner, topLeftCorner)

    }

    fun getCorners(startingBlock: Vector3i): List<Vector3i> {

        val edge = Vector3i(startingBlock)
        val lastEdge = Vector3i()
        val corners = mutableListOf<Vector3i>()


        // get an edge of 2d shape
        while (!isBlockAir(BlockCache.getBlockState(edge))) {
            edge.add(0, 0, 1)
        }
        edge.add(0, 0, -1)

        val startingpos = edge.clone() as Vector3i

        // go right untill we hit a corner
        // then add said corner, backoff and go forward
        while (!isBlockAir(BlockCache.getBlockState(edge))) {
            lastEdge.set(lastEdge)
            edge.add(-1, 0, 0)
        }
        edge.add(1, 0, 0)

        corners.add(edge.clone() as Vector3i)


        while (true) {
            // this says which way we should we iterate next
            val moveMultiplier: Vector3i = getMovementMultiplier(edge, lastEdge)


            while (!isBlockAir(BlockCache.getBlockState(edge))) {
                if (edge == startingpos) {
                    // we check if we have done a full loop
                    // *could be done with recursion but who cares*
                    return corners
                }
                // we use last edge to not go backwards
                lastEdge.set(edge)
                // add the multiplier, aka move one block
                edge.add(moveMultiplier)
            }
            // backoff one iteration since the loop stops on air, and we want the block before the air
            edge.sub(moveMultiplier)

            corners.add(edge.clone() as Vector3i)

        }
    }

    /**
     * An function that gets nearby blocks, and gives tells us how to move clockwise along the edge
     */
    private fun getMovementMultiplier(currentPoint: Vector3i, lastPoint: Vector3i): Vector3i {
        val left = (currentPoint.clone() as Vector3i).add(1, 0, 0)
        val right = (currentPoint.clone() as Vector3i).add(-1, 0, 0)
        val bottom = (currentPoint.clone() as Vector3i).add(0, 0, -1)
        val top = (currentPoint.clone() as Vector3i).add(0, 0, 1)

        val isLeftAir = isBlockAir(BlockCache.getBlockState(left))
        val isRightAir = isBlockAir(BlockCache.getBlockState(right))
        val isBottomAir = isBlockAir(BlockCache.getBlockState(bottom))
        val isTopAir = isBlockAir(BlockCache.getBlockState(top))

        if (!isLeftAir && !isRightAir && !isBottomAir && !isTopAir) {
            TODO("Handle crease")
        }

        if (left == lastPoint) {
            if (!isRightAir) {
                throw IllegalStateException("edge: $currentPoint lastEdge: $lastPoint; right is not air; last edge is left; we didnt iterate enough?")
            }
            if (isLeftAir) {
                throw IllegalStateException("edge: $currentPoint lastEdge: $lastPoint; left is air; last edge is left; how did we iterate from air?")
            }
            return if (isTopAir) {
                Vector3i(0, 0, -1)
            } else {
                if (isBottomAir) {
                    Vector3i(0, 0, 1)
                } else {
                    throw IllegalStateException("edge: $currentPoint lastEdge: $lastPoint; last edge is left; right,top,bottom,left is solid; did the if statement not work?")
                }
            }
        }

        if (right == lastPoint) {
            if (!isLeftAir) {
                throw IllegalStateException("edge: $currentPoint lastEdge: $lastPoint; left is not air; last edge is right; we didnt iterate enough?")
            }
            if (isRightAir) {
                throw IllegalStateException("edge: $currentPoint lastEdge: $lastPoint; right is air; last edge is right; how did we iterate from air?")
            }
            if (isTopAir) {
                return Vector3i(0, 0, -1)
            } else {
                throw IllegalStateException("edge: $currentPoint lastEdge: $lastPoint; last point is right; top, right are solid; left is air; we should be going clockwise")
            }
        }

        if (bottom == lastPoint) {

        }

        if (top == lastPoint) {

        }


        return TODO()
    }


    private fun isBlockAir(blockState: IBlockState): Boolean {
        return blockState.block == Blocks.air
    }


    data class roomDataBundle(val roomshape: RoomShape, val width: Int, val heigh: Int, val depth: Int)

    enum class RoomShape {
        ONEBYONE, ONEBYTWO, ONEBYTHREE, TWOBYTWO, LSHAPE
    }

}
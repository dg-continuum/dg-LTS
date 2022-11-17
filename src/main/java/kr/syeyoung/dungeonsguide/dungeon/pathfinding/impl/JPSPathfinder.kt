
package kr.syeyoung.dungeonsguide.dungeon.pathfinding.impl

import kr.syeyoung.dungeonsguide.dungeon.pathfinding.DungeonRoomAccessor
import kr.syeyoung.dungeonsguide.dungeon.pathfinding.PathfinderStrategy
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.EnumFacing
import net.minecraft.util.MathHelper
import org.joml.Vector3d
import java.util.*
import kotlin.math.abs

class JPSPathfinder(private val dungeonRoom: DungeonRoomAccessor): PathfinderStrategy(dungeonRoom) {
    private val nodeMap: HashMap<Int, Node> = HashMap()


    private val within = 1.5f

    private val open = PriorityQueue(
        Comparator.comparing { a: Node? -> a?.f ?: Float.MAX_VALUE }
            .thenComparing { _, a: Node? -> a?.x ?: Int.MAX_VALUE }
            .thenComparing { _, a: Node? -> a?.y ?: Int.MAX_VALUE }
            .thenComparing { _, a: Node? -> a?.z ?: Int.MAX_VALUE }
    )


    private var destinationBB: AxisAlignedBB? = null
    private var tx = 0
    private var ty = 0
    private var tz = 0
    private fun openNode(x: Int, y: Int, z: Int): Node {
        val i = Node.makeHash(x, y, z)
        var node = nodeMap[i]
        if (node == null) {
            node = Node(x, y, z)
            nodeMap[i] = node
        }
        return node
    }

    private fun addNode(parent: Node, jumpPt: Node, addToOpen: Boolean): Node {
        val ng = parent.g + distSq(
            (jumpPt.x - parent.x).toFloat(),
            (jumpPt.y - parent.y).toFloat(),
            (jumpPt.z - parent.z).toFloat()
        )
        if (ng < jumpPt.g) {
            if (addToOpen) {
                open.remove(jumpPt)
            }
            jumpPt.g = ng
            jumpPt.h = if (jumpPt.h == -1f) distSq(
                (tx - jumpPt.x).toFloat(),
                (ty - jumpPt.y).toFloat(),
                (tz - jumpPt.z).toFloat()
            ) else jumpPt.h
            jumpPt.f = jumpPt.h + jumpPt.g
            jumpPt.parent = parent
            if (addToOpen) open.add(jumpPt)
        }
        return jumpPt
    }

    private fun distSq(x: Float, y: Float, z: Float): Float {
        return MathHelper.sqrt_float(x * x + y * y + z * z)
    }

    fun getNeighbors(prevN: Node, n: Node): Set<Node> {
//        if (true) throw new RuntimeException("ah");
        val dx = MathHelper.clamp_int(n.x - prevN.x, -1, 1)
        val dy = MathHelper.clamp_int(n.y - prevN.y, -1, 1)
        val dz = MathHelper.clamp_int(n.z - prevN.z, -1, 1)
        val x = n.x
        val y = n.y
        val z = n.z
        val nx = n.x + dx
        val ny = n.y + dy
        val nz = n.z + dz
        val nexts: MutableSet<Node> = HashSet()
        val determinant = abs(dx) + abs(dy) + abs(dz)
        if (determinant == 0) {
            for (i in -1..1) for (j in -1..1) for (k in -1..1) {
                if (i == 0 && j == 0 && k == 0) continue
                nexts.add(openNode(x + i, y + j, z + k))
            }
        } else if (determinant == 1) {
            nexts.add(openNode(nx, ny, nz))
            for (i in -1..1) {
                for (j in -1..1) {
                    if (i == 0 && j == 0) continue
                    if (dx != 0 && dungeonRoom.isBlocked(x, y + i, z + j)) nexts.add(openNode(nx, y + i, z + j))
                    if (dy != 0 && dungeonRoom.isBlocked(x + i, y, z + j)) nexts.add(openNode(x + i, ny, z + j))
                    if (dz != 0 && dungeonRoom.isBlocked(x + i, y + j, z)) nexts.add(openNode(x + i, y + j, nz))
                }
            }
        } else if (determinant == 2) {
            if (dz != 0) nexts.add(openNode(x, y, nz))
            if (dy != 0) nexts.add(openNode(x, ny, z))
            if (dx != 0) nexts.add(openNode(nx, y, z))
            nexts.add(openNode(nx, ny, nz))
            if (dx == 0) {
                if (dungeonRoom.isBlocked(x, y, z - dz)) {
                    nexts.add(openNode(x, ny, z - dz))
                    if (dungeonRoom.isBlocked(x + 1, y, z - dz)) nexts.add(openNode(x + 1, ny, z - dz))
                    if (dungeonRoom.isBlocked(x - 1, y, z - dz)) nexts.add(openNode(x - 1, ny, z - dz))
                }
                if (dungeonRoom.isBlocked(x, y - dy, z)) {
                    nexts.add(openNode(x, y - dy, nz))
                    if (dungeonRoom.isBlocked(x + 1, y - dy, z)) nexts.add(openNode(x + 1, y - dy, nz))
                    if (dungeonRoom.isBlocked(x - 1, y - dy, z)) nexts.add(openNode(x + 1, y - dy, nz))
                }
            } else if (dy == 0) {
                if (dungeonRoom.isBlocked(x, y, z - dz)) {
                    nexts.add(openNode(nx, y, z - dz))
                    if (dungeonRoom.isBlocked(x, y + 1, z - dz)) nexts.add(openNode(nx, y + 1, z - dz))
                    if (dungeonRoom.isBlocked(x, y - 1, z - dz)) nexts.add(openNode(nx, y - 1, z - dz))
                }
                if (dungeonRoom.isBlocked(x - dx, y, z)) {
                    nexts.add(openNode(x - dx, y, nz))
                    if (dungeonRoom.isBlocked(x - dx, y + 1, z)) nexts.add(openNode(x - dx, y + 1, nz))
                    if (dungeonRoom.isBlocked(x - dx, y - 1, z)) nexts.add(openNode(x - dx, y - 1, nz))
                }
            } else if (dz == 0) {
                if (dungeonRoom.isBlocked(x, y - dy, z)) {
                    nexts.add(openNode(nx, y - dy, z))
                    if (dungeonRoom.isBlocked(x, y - dy, z + 1)) nexts.add(openNode(nx, y - dy, z + 1))
                    if (dungeonRoom.isBlocked(x, y - dy, z - 1)) nexts.add(openNode(nx, y - dy, z - 1))
                }
                if (dungeonRoom.isBlocked(x - dx, y, z)) {
                    nexts.add(openNode(x - dx, ny, z))
                    if (dungeonRoom.isBlocked(x - dx, y, z + 1)) nexts.add(openNode(x - dx, ny, z + 1))
                    if (dungeonRoom.isBlocked(x - dx, y, z - 1)) nexts.add(openNode(x - dx, ny, z - 1))
                }
            }
        } else if (determinant == 3) {
            nexts.add(openNode(x, y, nz))
            nexts.add(openNode(x, ny, z))
            nexts.add(openNode(nx, y, z))
            nexts.add(openNode(nx, y, nz))
            nexts.add(openNode(x, ny, nz))
            nexts.add(openNode(nx, ny, z))
            nexts.add(openNode(nx, ny, nz))
            if (dungeonRoom.isBlocked(x, y, z - dz)) {
                nexts.add(openNode(x, ny, z - dz))
                nexts.add(openNode(nx, ny, z - dz))
                nexts.add(openNode(nx, y, z - dz))
            }
            if (dungeonRoom.isBlocked(x - dx, y, z)) {
                nexts.add(openNode(x - dx, ny, nz))
                nexts.add(openNode(x - dx, ny, z))
                nexts.add(openNode(x - dx, y, nz))
            }
            if (dungeonRoom.isBlocked(x, y - dy, z)) {
                nexts.add(openNode(x, y - dy, nz))
                nexts.add(openNode(nx, y - dy, z))
                nexts.add(openNode(nx, y - dy, nz))
            }
        }
        return nexts
    }

    fun expand(x: Int, y: Int, z: Int, dx: Int, dy: Int, dz: Int): Node? {
        var x = x
        var y = y
        var z = z
        while (true) {
            val nx = x + dx
            val ny = y + dy
            val nz = z + dz
            if (dungeonRoom.isBlocked(nx, ny, nz)) return null
            if (nx > destinationBB!!.minX && nx < destinationBB!!.maxX && ny > destinationBB!!.minY && ny < destinationBB!!.maxY && nz > destinationBB!!.minZ && nz < destinationBB!!.maxZ) return openNode(
                nx,
                ny,
                nz
            )
            val determinant = Math.abs(dx) + Math.abs(dy) + Math.abs(dz)
            if (determinant == 1) {
                for (i in -1..1) {
                    for (j in -1..1) {
                        if (i == 0 && j == 0) continue
                        if (dx != 0 && dungeonRoom.isBlocked(nx, ny + i, nz + j) && !dungeonRoom.isBlocked(
                                nx + dx,
                                ny + i,
                                nz + j
                            )
                        ) return openNode(nx, ny, nz)
                        if (dy != 0 && dungeonRoom.isBlocked(nx + i, ny, nz + j) && !dungeonRoom.isBlocked(
                                nx + i,
                                ny + dy,
                                nz + j
                            )
                        ) return openNode(nx, ny, nz)
                        if (dz != 0 && dungeonRoom.isBlocked(nx + i, ny + j, nz) && !dungeonRoom.isBlocked(
                                nx + i,
                                ny + j,
                                nz + dz
                            )
                        ) return openNode(nx, ny, nz)
                    }
                }
            } else if (determinant == 2) {
                for (value in EnumFacing.VALUES) {
                    if (value.frontOffsetX == dx || value.frontOffsetY == dy || value.frontOffsetZ == dz) continue
                    val tx = nx + value.frontOffsetX
                    val ty = ny + value.frontOffsetY
                    val tz = nz + value.frontOffsetZ
                    if (dungeonRoom.isBlocked(tx, ty, tz)) return openNode(nx, ny, nz)
                }
                if (dx != 0 && expand(nx, ny, nz, dx, 0, 0) != null) return openNode(nx, ny, nz)
                if (dy != 0 && expand(nx, ny, nz, 0, dy, 0) != null) return openNode(nx, ny, nz)
                if (dz != 0 && expand(nx, ny, nz, 0, 0, dz) != null) return openNode(nx, ny, nz)
            } else if (determinant == 3) {
                if (dungeonRoom.isBlocked(x, ny, nz) || dungeonRoom.isBlocked(nx, y, nz) || dungeonRoom.isBlocked(
                        nx,
                        ny,
                        z
                    )
                ) return openNode(nx, ny, nz)
                if (expand(nx, ny, nz, dx, 0, 0) != null || expand(nx, ny, nz, dx, dy, 0) != null || expand(
                        nx,
                        ny,
                        nz,
                        dx,
                        0,
                        dz
                    ) != null || expand(nx, ny, nz, 0, dy, 0) != null || expand(
                        nx,
                        ny,
                        nz,
                        0,
                        dy,
                        dz
                    ) != null || expand(nx, ny, nz, 0, 0, dz) != null
                ) return openNode(nx, ny, nz)
            }
            x = nx
            y = ny
            z = nz
        }
    }

    class Node(val x: Int, val y: Int, val z:Int) {
        var f = 0f
        var g = Float.MAX_VALUE
        var h = -1f
        var closed = false

        var parent: Node? = null
        fun close(): Node {
            closed = true
            return this
        }

        companion object {
            fun makeHash(x: Int, y: Int, z: Int): Int {
                return y and 255 or (x and 32767 shl 8) or (z and 32767 shl 24) or (if (x < 0) Int.MIN_VALUE else 0) or if (z < 0) 32768 else 0
            }
        }
    }

    override fun pathfind(from: Vector3d, to: Vector3d, timeout: Float): Boolean {
        var from = from
        var to = to
        route.clear()
        nodeMap.clear()
        run {
            from =
                Vector3d((from.x * 2).toInt() / 2.0, (from.y * 2).toInt() / 2.0, (from.z * 2).toInt() / 2.0)
            to = Vector3d((to.x * 2).toInt() / 2.0, (to.y * 2).toInt() / 2.0, (to.z * 2).toInt() / 2.0)
        }
        tx = (to.x * 2).toInt()
        ty = (to.y * 2).toInt()
        tz = (to.z * 2).toInt()
        destinationBB = AxisAlignedBB.fromBounds(
            (to.x - within) * 2,
            (to.y - within) * 2,
            (to.z - within) * 2,
            (to.x + within) * 2,
            (to.y + within) * 2,
            (to.z + within) * 2
        )
        open.clear()
        var start: Node
        open.add(
            openNode(
                from.x.toInt() * 2 + 1,
                from.y.toInt() * 2,
                from.z.toInt() * 2 + 1
            ).also { start = it })
        start.g = 0f
        start.f = 0f
        start.h = from.distanceSquared(to).toFloat()
        var end: Node? = null
        var minDist = Float.MAX_VALUE
        val forceEnd = System.currentTimeMillis() + timeout + 999999999L
        while (!open.isEmpty()) {
            if (forceEnd < System.currentTimeMillis() && timeout != -1F) break
            val n = open.poll()
            if (n != null) {
                n.closed = true
                if (minDist > n.h) {
                    minDist = n.h
                    end = n
                }
                if (n.x > destinationBB!!.minX && n.x < destinationBB!!.maxX && n.y > destinationBB!!.minY && n.y < destinationBB!!.maxY && n.z > destinationBB!!.minZ && n.z < destinationBB!!.maxZ) {
                    break
                }
            }
            for (neighbor in getNeighbors(n!!.parent ?: n, n)) {
                val jumpPT = expand(n.x, n.y, n.z, neighbor.x - n.x, neighbor.y - n.y, neighbor.z - n.z)
                if (jumpPT == null || jumpPT.closed) continue
                addNode(n, jumpPT, true)
            }
        }
        if (end == null) return false
        var p = end
        while (p != null) {
            route.addLast(Vector3d((p.x / 2.0f).toDouble(), p.y / 2.0f + 0.1, (p.z / 2.0f).toDouble()))
            p = p.parent
        }
        return true
    }
}
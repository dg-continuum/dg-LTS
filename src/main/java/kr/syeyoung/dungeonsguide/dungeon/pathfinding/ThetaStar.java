/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.dungeon.pathfinding;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.*;

public class ThetaStar {
    final int dx;
    final int dy;
    final int dz;
    private final DungeonRoom dungeonRoom;
    @Getter
    private final AxisAlignedBB destinationBB;
    private final Map<Coordinate, Node> nodeMap = new HashMap<>();
    @Getter
    private final PriorityQueue<Node> open = new PriorityQueue<>(
            Comparator.comparing(
                    (Node a) -> a == null ? Float.MAX_VALUE : a.f)
            .thenComparing(a -> a == null ? Float.MAX_VALUE : a.coordinate.x)
            .thenComparing(a -> a == null ? Float.MAX_VALUE : a.coordinate.y)
            .thenComparing(a -> a == null ? Float.MAX_VALUE : a.coordinate.z)
    );
    int lastSx;
    int lastSy;
    int lastSz;
    @Getter
    private LinkedList<Vec3> route = new LinkedList<>();
    private int pfindIdx = 0;

    public ThetaStar(DungeonRoom dungeonRoom, Vec3 destination) {
        this.dungeonRoom = dungeonRoom;

        this.dx = (int) (destination.xCoord * 2);
        this.dy = (int) (destination.yCoord * 2);
        this.dz = (int) (destination.zCoord * 2);
        destinationBB = AxisAlignedBB.fromBounds(dx - 2, dy - 2, dz - 2, dx + 2, dy + 2, dz + 2);
    }

    private Node openNode(int x, int y, int z) {
        Coordinate coordinate = new Coordinate(x, y, z);

        return nodeMap.computeIfAbsent(coordinate, c -> new Node(coordinate));
    }

    public boolean pathfind(Vec3 from, long timeout) {

        pfindIdx++;
        if (lastSx != (int) Math.round(from.xCoord * 2) || lastSy != (int) Math.round(from.yCoord * 2) || lastSz != (int) Math.round(from.zCoord * 2)) {
            open.clear();
        }

        this.lastSx = (int) Math.round(from.xCoord * 2);
        this.lastSy = (int) Math.round(from.yCoord * 2);
        this.lastSz = (int) Math.round(from.zCoord * 2);
        if (dungeonRoom.isBlocked(lastSx, lastSy, lastSz)) return false;

        Node startNode = openNode(dx, dy, dz);
        Node goalNode = openNode(lastSx, lastSy, lastSz);
        startNode.g = 0;
        startNode.f = 0;
        goalNode.g = Integer.MAX_VALUE;
        goalNode.f = Integer.MAX_VALUE;
        if (goalNode.parent != null) {
            LinkedList<Vec3> route = new LinkedList<>();
            Node curr = goalNode;
            while (curr.parent != null) {
                route.addLast(new Vec3(curr.coordinate.x / 2.0, curr.coordinate.y / 2.0 + 0.1, curr.coordinate.z / 2.0));
                curr = curr.parent;
            }
            route.addLast(new Vec3(curr.coordinate.x / 2.0, curr.coordinate.y / 2.0 + 0.1, curr.coordinate.z / 2.0));
            this.route = route;
            return true;
        }
        open.add(startNode);

        long end = System.currentTimeMillis() + timeout;

        while (!open.isEmpty()) {
            if (System.currentTimeMillis() > end) {
                return false;
            }
            Node n = open.poll();
            if (n.lastVisited == pfindIdx) {
                continue;
            }
            n.lastVisited = pfindIdx;

            if (n == goalNode) {
                // route = reconstructPath(startNode)
                LinkedList<Vec3> route = new LinkedList<>();
                Node curr = goalNode;
                while (curr.parent != null) {
                    route.addLast(new Vec3(curr.coordinate.x / 2.0, curr.coordinate.y / 2.0 + 0.1, curr.coordinate.z / 2.0));
                    curr = curr.parent;
                }
                route.addLast(new Vec3(curr.coordinate.x / 2.0, curr.coordinate.y / 2.0 + 0.1, curr.coordinate.z / 2.0));
                this.route = route;
                return true;
            }

            for (EnumFacing value : EnumFacing.VALUES) {
                Node neighbor = openNode(n.coordinate.x + value.getFrontOffsetX(), n.coordinate.y + value.getFrontOffsetY(), n.coordinate.z + value.getFrontOffsetZ());

                // check blocked.
                if (!((destinationBB.minX <= neighbor.coordinate.x && neighbor.coordinate.x <= destinationBB.maxX &&
                        destinationBB.minY <= neighbor.coordinate.y && neighbor.coordinate.y <= destinationBB.maxY &&
                        destinationBB.minZ <= neighbor.coordinate.z && neighbor.coordinate.z <= destinationBB.maxZ) // near destination
                        || !dungeonRoom.isBlocked(neighbor.coordinate.x, neighbor.coordinate.y, neighbor.coordinate.z))) { // not blocked
                    continue;
                }
                if (neighbor.lastVisited == pfindIdx) continue;

                boolean flag = false;
                if (n.parent != null) {
                    float tempGScore = n.parent.g + distSq(n.parent.coordinate.x - neighbor.coordinate.x, n.parent.coordinate.y - neighbor.coordinate.y, n.parent.coordinate.z - neighbor.coordinate.z);
                    if (tempGScore < neighbor.g && lineofsight(n.parent, neighbor)) {
                        neighbor.parent = n.parent;
                        neighbor.g = tempGScore;
                        neighbor.f = tempGScore + distSq(goalNode.coordinate.x - neighbor.coordinate.x, goalNode.coordinate.y - neighbor.coordinate.y, goalNode.coordinate.z - neighbor.coordinate.z);
                        open.add(neighbor);
                        flag = true;
                    }
                }
                if (!flag) {
                    float gScore = n.g + 1; // altho it's sq, it should be fine
                    if (gScore < neighbor.g) {
                        neighbor.parent = n;
                        neighbor.g = gScore;
                        neighbor.f = gScore + distSq(goalNode.coordinate.x - neighbor.coordinate.x, goalNode.coordinate.y - neighbor.coordinate.y, goalNode.coordinate.z - neighbor.coordinate.z);
                        open.add(neighbor);
                    } else if (neighbor.lastVisited != pfindIdx) {
                        neighbor.f = neighbor.g + distSq(goalNode.coordinate.x - neighbor.coordinate.x, goalNode.coordinate.y - neighbor.coordinate.y, goalNode.coordinate.z - neighbor.coordinate.z);
                        open.add(neighbor);
                    }
                }
            }
        }
        return true;
    }

    private boolean lineofsight(Node a, Node b) {
        if (a == null || b == null) return false;
        float sx = a.coordinate.x;
        float sy = a.coordinate.y;
        float sz = a.coordinate.z;
        int ex = b.coordinate.x;
        int ey = b.coordinate.y;
        int ez = b.coordinate.z;

        float dxx = ex - sx;
        float dyy = ey - sy;
        float dzz = ez - sz;
        float len = distSq(dxx, dyy, dzz);
        dxx /= len;
        dyy /= len;
        dzz /= len;

        for (int d = 0; d <= len; d += 1) {
            int round = Math.round(sx);
            double ceil = Math.ceil(sy);
            int round1 = Math.round(sz);
            if (dungeonRoom.isBlocked(round, (int) ceil, round1)) return false;
            if (dungeonRoom.isBlocked(round + 1, (int) ceil, round1 + 1)) return false;
            if (dungeonRoom.isBlocked(round - 1, (int) ceil, round1 - 1)) return false;
            if (dungeonRoom.isBlocked(round + 1, (int) ceil, round1 - 1)) return false;
            if (dungeonRoom.isBlocked(round - 1, (int) ceil, round1 + 1)) return false;
            sx += dxx;
            sy += dyy;
            sz += dzz;
        }
        return true;
    }


    private float distSq(float x, float y, float z) {
        return MathHelper.sqrt_float(x * x + y * y + z * z);
    }

    @Data
    public static final class Node {
        private final Coordinate coordinate;
        private float f = Float.MAX_VALUE;
        private float g = Float.MAX_VALUE;
        private int lastVisited;
        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        private Node parent;
    }
    @Data
    public static final class Coordinate {
        private final int x;
        private final int y;
        private final int z;
    }
}

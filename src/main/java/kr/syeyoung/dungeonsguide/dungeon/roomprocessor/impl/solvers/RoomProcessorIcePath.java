/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.solvers;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.oneconfig.solvers.SilverfishPage;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.*;
import java.util.function.Predicate;

public class RoomProcessorIcePath extends GeneralRoomProcessor {

    private final Set<OffsetPoint> endNode = new HashSet<>();
    private final List<Vector3i> solution = new ArrayList<>();
    private int[][] map;
    private OffsetPoint[][] map2;
    private BlockPos lastSilverfishLoc;
    private int sameTick;

    private Entity silverfish;

    private boolean err;

    public RoomProcessorIcePath(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
        findSilverFishanddoStuff();
    }

    // Taken from https://stackoverflow.com/a/55271133 and modified to suit our needs
    // Answer by ofekp (https://stackoverflow.com/users/4295037/ofekp)
    public static List<Vector2i> solve(int[][] board, int startX, int startY, Predicate<Vector2i> finishLinePredicate) {
        Vector2i startPoint = new Vector2i(startX, startY);

        LinkedList<Vector2i> queue = new LinkedList<>();
        Vector2i[][] boardSearch = new Vector2i[board.length][board[0].length];

        queue.addLast(new Vector2i(startX, startY));
        boardSearch[startY][startX] = startPoint;

        while (!queue.isEmpty()) {
            Vector2i currPos = queue.pollFirst();
            for (Direction dir : Direction.values()) {
                Vector2i nextPos = move(board, boardSearch, currPos, dir);
                if (nextPos != null) {
                    queue.addLast(nextPos);
                    boardSearch[nextPos.y][nextPos.x] = new Vector2i(currPos.x, currPos.y);
                    if (finishLinePredicate.test(nextPos)) {
                        List<Vector2i> route = new ArrayList<>();
                        Vector2i tmp = currPos;
                        route.add(nextPos);
                        route.add(currPos);
                        while (tmp != startPoint) {
                            tmp = boardSearch[tmp.y][tmp.x];
                            route.add(tmp);
                        }
                        return route;
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    public static Vector2i move(int[][] board, Vector2i[][] boardSearch, Vector2i currPos, Direction dir) {
        int x = currPos.x;
        int y = currPos.y;

        int diffX = dir.dx;
        int diffY = dir.dy;

        int i = 1;
        while (x + i * diffX >= 0 && x + i * diffX < board[0].length
                && y + i * diffY >= 0 && y + i * diffY < board.length
                && board[y + i * diffY][x + i * diffX] != 1) {
            i++;
        }
        i--;

        if (boardSearch[y + i * diffY][x + i * diffX] != null) {
            return null;
        }

        return new Vector2i(x + i * diffX, y + i * diffY);
    }

    public void findSilverFishanddoStuff() {
        final Vector3i low = getDungeonRoom().getMin();
        final Vector3i high = getDungeonRoom().getMax();
        getDungeonRoom();
        List<EntitySilverfish> silverfishs = Minecraft.getMinecraft().theWorld.getEntities(EntitySilverfish.class, input -> {
            if (input.isInvisible()) return false;
            BlockPos pos = input.getPosition();
            return low.x < pos.getX() && pos.getX() < high.x
                    && low.z < pos.getZ() && pos.getZ() < high.z;
        });

        if (!silverfishs.isEmpty()) silverfish = silverfishs.get(0);
        if (silverfishs.isEmpty()) {
            err = true;
            return;
        }
        try {
            buildMap();
            err = false;
        } catch (Exception e) {
            e.printStackTrace();
            err = true;
        }
    }

    private void buildMap() {
        int width = (Integer) getDungeonRoom().getDungeonRoomInfo().getProperties().get("width");
        int height = (Integer) getDungeonRoom().getDungeonRoomInfo().getProperties().get("height");
        OffsetPointSet ops = (OffsetPointSet) getDungeonRoom().getDungeonRoomInfo().getProperties().get("board");
        OffsetPointSet endNodes = (OffsetPointSet) getDungeonRoom().getDungeonRoomInfo().getProperties().get("endnodes");
        map2 = new OffsetPoint[width][height];
        map = new int[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                OffsetPoint op = ops.getOffsetPointList().get(y * width + x);
                map2[y][x] = op;
                map[y][x] = op.getBlock(getDungeonRoom()) == Blocks.air ? 0 : 1;
            }
        }
        endNode.addAll(endNodes.getOffsetPointList());
    }

    public void tick() {
        super.tick();
        if (err || silverfish.isDead) {
            findSilverFishanddoStuff();
            if (err) return;
        }
        if (silverfish.getPosition().equals(lastSilverfishLoc)) {
            if (sameTick < 10) {
                sameTick++;
                return;
            } else if (sameTick == 10) {
                sameTick++;
                if(map != null) {
                    Vector2i silverfish = getPointOfSilverFishOnMap(this.silverfish.getPosition());
                    List<Vector2i> tempSol = solve(map, silverfish.x, silverfish.y, input -> endNode.contains(map2[input.y][input.x]));
                    solution.clear();
                    for (Vector2i point : tempSol) {
                        solution.add(map2[point.y][point.x].getVector3i(getDungeonRoom()));
                    }
                }

            }
        } else {
            sameTick = 0;
        }

        lastSilverfishLoc = silverfish.getPosition();
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!DgOneCongifConfig.silverFishSolver) return;
        if (!err)
            RenderUtils.drawLines(solution, DgOneCongifConfig.oneconftodgcolor(SilverfishPage.color), SilverfishPage.width, partialTicks, true);
    }

    public Vector2i getPointOfSilverFishOnMap(BlockPos blockPos) {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                if (map2[y][x].getVector3i(getDungeonRoom()).equals(blockPos))
                    return new Vector2i(x, y);
            }
        }
        return null;
    }

    @Getter
    @AllArgsConstructor
    public enum Direction {
        LEFT(-1, 0),
        RIGHT(1, 0),
        UP(0, -1),
        DOWN(0, 1);

        int dx, dy;
    }
}

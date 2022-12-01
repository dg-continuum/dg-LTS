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

package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.solvers.boxpuzzle;

import kr.syeyoung.dungeonsguide.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.utils.BlockCache;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.VectorUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.*;

import static kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig.solverBoxLinewidth;

public class RoomProcessorBoxSolver extends GeneralRoomProcessor {


    private static final java.util.List<Point> directions = Arrays.asList(new Point(-1, 0), new Point(1, 0), new Point(0, 1), new Point(0, -1));
    private Vector3i[][] poses = new Vector3i[7][7];
    private boolean bugged = true;
    private BoxPuzzleSolvingThread puzzleSolvingThread;
    private boolean calcReq = true;
    private boolean calcDone = false;
    private boolean calcDone2 = false;
    private int step = 0;
    private byte[][] lastState;
    private boolean yState = true;
    private List<BoxPuzzleSolvingThread.BoxMove> solution;
    private List<Vector3i> pathFound;
    private List<Vector3i> totalPath;
    private List<Vector3i> totalPushedBlocks;
    private Point lastPlayer;
    public RoomProcessorBoxSolver(DungeonRoom dungeonRoom) {
        super(dungeonRoom);

        OffsetPointSet ops = (OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("board");
        try {
            if (ops != null) {
                for (int y = 0; y < 7; y++) {
                    for (int x = 0; x < 7; x++) {
                        poses[y][x] = ops.getOffsetPointList().get(y * 7 + x).getVector3i(dungeonRoom);
                    }
                }
                bugged = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[][] buildCurrentState() {
        World w = getDungeonRoom().getContext().getWorld();
        byte[][] board = new byte[poses.length][poses[0].length];
        for (int y = 0; y < poses.length; y++) {
            for (int x = 0; x < poses[0].length; x++) {
                if (y == 6) {
                    board[y][x] = -1;
                    continue;
                }
                Vector3i pos = poses[y][x];
                Block b = BlockCache.getBlock(pos);
                if (b == Blocks.air)
                    board[y][x] = 0;
                else
                    board[y][x] = 1;
            }
        }
        return board;
    }

    @Override
    public void tick() {
        super.tick();
        if (!DgOneCongifConfig.solverBox) return;
        if (bugged) return;
        byte[][] currboard = buildCurrentState();
        if (puzzleSolvingThread == null) {
            calcDone = false;
            puzzleSolvingThread = new BoxPuzzleSolvingThread(currboard, 0, 6, new Runnable() {
                @Override
                public void run() {
                    calcDone = true;
                    calcDone2 = true;
                }
            });
            puzzleSolvingThread.start();
        }
        if (calcReq) {
            OffsetPointSet ops = (OffsetPointSet) getDungeonRoom().getDungeonRoomInfo().getProperties().get("board");
            if (ops != null) {
                poses = new Vector3i[7][7];
                for (int y = 0; y < 7; y++) {
                    for (int x = 0; x < 7; x++) {
                        poses[y][x] = ops.getOffsetPointList().get(y * 7 + x).getVector3i(getDungeonRoom());
                    }
                }
                bugged = false;
            }

            calcDone = false;
            if (puzzleSolvingThread != null)
                puzzleSolvingThread.stop();
            puzzleSolvingThread = new BoxPuzzleSolvingThread(currboard, 0, 6, new Runnable() {
                @Override
                public void run() {
                    calcDone = true;
                    calcDone2 = true;
                }
            });
            puzzleSolvingThread.start();
            calcReq = false;
        }

        boolean pathFindReq = false;
        if (calcDone2) {
            BoxPuzzleSolvingThread.Route semi_solution = puzzleSolvingThread.solution;
            if (semi_solution == null) {
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §eBox Solver §7:: §cCouldn't find solution involving less than 20 box moves within 3m concurrent possibility"));
                step = 0;
                calcDone2 = false;
                pathFindReq = true;
                totalPath = new LinkedList<Vector3i>();
                totalPushedBlocks = new LinkedList<Vector3i>();
                solution = new LinkedList<BoxPuzzleSolvingThread.BoxMove>();
                return;
            } else {
                solution = semi_solution.boxMoves;
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §eBox Solver §7:: Solution Found!"));
            }
            step = 0;
            lastState = currboard;
            calcDone2 = false;
            pathFindReq = true;

            calcTotalPath();
        }

        if (lastState == null) return;
        boolean moved = false;
        label:
        for (int y = 0; y < currboard.length; y++) {
            for (int x = 0; x < currboard[y].length; x++) {
                if (lastState[y][x] != currboard[y][x]) {
                    moved = true;
                    lastState = currboard;
                    break label;
                }
            }
        }

        if (moved) {
            step++;
        }

        Point player = getPlayerPos(currboard);
        boolean currYState = Minecraft.getMinecraft().thePlayer.getPosition().getY() < 68;
        if (((currYState && !player.equals(lastPlayer)) || (currYState != yState) || (moved) || pathFindReq) && solution != null) {
            Point target = null;
            if (step < solution.size()) {
                BoxPuzzleSolvingThread.BoxMove boxMove = solution.get(step);
                target = new Point(boxMove.x - boxMove.dx, boxMove.y - boxMove.dy);
            }
            List<Point> semi_pathFound = pathfind(currboard, player, target);
            pathFound = new LinkedList<Vector3i>();
            for (Point point : semi_pathFound) {
                pathFound.add(poses[point.y][point.x].add(0, -1, 0));
            }

            lastPlayer = player;
            yState = currYState;
        }

    }

    public void calcTotalPath() {
        Point player = new Point(0, 6);
        totalPath = new LinkedList<Vector3i>();
        totalPushedBlocks = new LinkedList<Vector3i>();
        byte[][] currboard = buildCurrentState();
        for (int i = 0; i <= solution.size(); i++) {
            Point target = null;
            BoxPuzzleSolvingThread.BoxMove boxMove = null;
            if (i < solution.size()) {
                boxMove = solution.get(i);
                target = new Point(boxMove.x - boxMove.dx, boxMove.y - boxMove.dy);
            }
            List<Point> semi_pathFound = pathfind(currboard, player, target);
            for (int i1 = semi_pathFound.size() - 1; i1 >= 0; i1--) {
                Point point = semi_pathFound.get(i1);
                totalPath.add(poses[point.y][point.x].add(0, -1, 0));
            }

            player = target;
            if (boxMove != null) {
                BoxPuzzleSolvingThread.push(currboard, boxMove.x, boxMove.y, boxMove.dx, boxMove.dy);
                int fromX = boxMove.x - boxMove.dx;
                int fromY = boxMove.y - boxMove.dy;

                Vector3i pos = poses[fromY][fromX];
                Vector3i pos2 = poses[boxMove.y][boxMove.x];
                Vector3i dir = pos.sub(pos2);
                dir = new Vector3i(MathHelper.clamp_int(dir.x, -1, 1), 0, (int) MathHelper.clamp_double(dir.z, -1, 1));

                Vector3i highlight = pos2.add(dir);
                totalPushedBlocks.add(highlight);
            }
        }
    }

    public Point getPlayerPos(byte[][] map) {
        Vector3i playerPos = VectorUtils.getPlayerVector3i();
        int minDir = Integer.MAX_VALUE;
        Point pt = null;
        for (int y = 0; y < poses.length; y++) {
            for (int x = 0; x < poses[0].length; x++) {
                if (map[y][x] == 1) continue;
                int dir = (int) poses[y][x].distance(playerPos);
                if (dir < minDir) {
                    minDir = dir;
                    pt = new Point(x, y);
                }
            }
        }
        return pt;
    }

    public List<Point> pathfind(byte[][] map, Point start, Point target2) {
        int[][] distances = new int[map.length][map[0].length];

        Queue<Point> evalulate = new LinkedList<Point>();
        evalulate.add(start);
        Point target = null;
        while (!evalulate.isEmpty()) {
            Point p = evalulate.poll();
            if (p.equals(target2) || (target2 == null && p.y == 0)) {
                target = p;
                break;
            }
            int max = 0;
            for (Point dir : directions) {
                int resX = p.x + dir.x;
                int resY = p.y + dir.y;
                if (resX < 0 || resY < 0 || resX >= distances[0].length || resY >= distances.length) {
                    continue;
                }

                if (max < distances[resY][resX]) {
                    max = distances[resY][resX];
                }
                if (distances[resY][resX] == 0 && (map[resY][resX] == 0 || map[resY][resX] == -1)) {
                    evalulate.add(new Point(resX, resY));
                }
            }
            distances[p.y][p.x] = max + 1;
        }
        if (target == null) return Collections.emptyList();

        List<Point> route = new LinkedList<Point>();
        while (!target.equals(start)) {
            route.add(target);
            int min = Integer.MAX_VALUE;
            Point minPoint = null;
            for (Point dir : directions) {
                int resX = target.x + dir.x;
                int resY = target.y + dir.y;
                if (resX < 0 || resY < 0 || resX >= distances[0].length || resY >= distances.length) {
                    continue;
                }

                if (min > distances[resY][resX] && distances[resY][resX] != 0) {
                    min = distances[resY][resX];
                    minPoint = new Point(resX, resY);
                }
            }
            target = minPoint;
        }
        route.add(start);
        return route;
    }

    @Override
    public void chatReceived(IChatComponent chat) {
        if (!DgOneCongifConfig.solverBox) return;
        if (chat.getFormattedText().toLowerCase().contains("recalc")) {
            if (calcDone) {
                calcReq = true;
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide :::: Recalculating Route..."));
            } else {
                calcReq = true;
                ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide :::: Currently Calculating Route..."));
            }
        }
    }

    @Override
    public void drawScreen(float partialTicks) {
        super.drawScreen(partialTicks);
        if (!DgOneCongifConfig.solverBox) return;
        if (DgOneCongifConfig.solverBoxDisableText) return;
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        fr.drawString("Type \"recalc\" in chat to recalculate the solution", 0, 0, 0xFFFFFFFF);
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!DgOneCongifConfig.solverBox) return;
        if (bugged) return;
        if (!calcDone) return;
        if (solution == null) return;
        if (Minecraft.getMinecraft().thePlayer.getPosition().getY() < 68) {
            if (step < solution.size()) {
                BoxPuzzleSolvingThread.BoxMove boxMove = solution.get(step);
                int fromX = boxMove.x - boxMove.dx;
                int fromY = boxMove.y - boxMove.dy;

                Vector3i pos = poses[fromY][fromX];
                Vector3i pos2 = poses[boxMove.y][boxMove.x];
                Vector3i dir = pos.sub(pos2);
                dir = new Vector3i(MathHelper.clamp_int(dir.x, -1, 1), 0, (int) MathHelper.clamp_double(dir.z, -1, 1));

                Vector3i highlight = pos2.add(dir);
                AColor color = new AColor(DgOneCongifConfig.solverBoxTargetColor.getRed(), DgOneCongifConfig.solverBoxTargetColor.getBlue(), DgOneCongifConfig.solverBoxTargetColor.getGreen(), DgOneCongifConfig.solverBoxTargetColor.getAlpha()).multiplyAlpha(MathHelper.clamp_double(VectorUtils.getPlayerVector3i().distance(highlight), 100, 255) / 255);
                RenderUtils.highlightBoxAColor(AxisAlignedBB.fromBounds(highlight.x, highlight.y, highlight.z, highlight.x + 1, highlight.y + 1, highlight.z + 1), color, partialTicks, false);
            }

            if (pathFound != null) {
                RenderUtils.drawLines(pathFound, new AColor(DgOneCongifConfig.solverBoxLineColor.getRed(), DgOneCongifConfig.solverBoxLineColor.getBlue(), DgOneCongifConfig.solverBoxLineColor.getGreen(), DgOneCongifConfig.solverBoxLineColor.getAlpha()), solverBoxLinewidth, partialTicks, true);
            }
        } else {
            if (totalPath != null) {
                RenderUtils.drawLines(totalPath, new AColor(DgOneCongifConfig.solverBoxLineColor.getRed(), DgOneCongifConfig.solverBoxLineColor.getBlue(), DgOneCongifConfig.solverBoxLineColor.getGreen(), DgOneCongifConfig.solverBoxLineColor.getAlpha()), solverBoxLinewidth, partialTicks, false);
            }
            if (totalPushedBlocks != null) {
                for (int i = 0; i < totalPushedBlocks.size(); i++) {
                    Vector3i pos = totalPushedBlocks.get(i);
                    RenderUtils.highlightBoxAColor(AxisAlignedBB.fromBounds(pos.x, pos.y, pos.z, pos.x + 1, pos.y + 1, pos.z + 1), new AColor(DgOneCongifConfig.solverBoxTargetColor.getRed(), DgOneCongifConfig.solverBoxTargetColor.getBlue(), DgOneCongifConfig.solverBoxTargetColor.getGreen(), DgOneCongifConfig.solverBoxTargetColor.getAlpha()), partialTicks, false);
                    RenderUtils.drawTextAtWorld("#" + i, pos.x + 0.5f, pos.y + 0.5f, pos.z + 0.5f, i != step ?
                            RenderUtils.getColorAt(pos.x, pos.y, pos.z, new AColor(DgOneCongifConfig.solverBoxTextColor.getRed(), DgOneCongifConfig.solverBoxTextColor.getBlue(), DgOneCongifConfig.solverBoxTextColor.getGreen(), DgOneCongifConfig.solverBoxTextColor.getAlpha())) : RenderUtils.getColorAt(pos.x, pos.y, pos.z, new AColor(DgOneCongifConfig.solverBoxTextColorNextStep.getRed(), DgOneCongifConfig.solverBoxTextColorNextStep.getBlue(), DgOneCongifConfig.solverBoxTextColorNextStep.getGreen(), DgOneCongifConfig.solverBoxTextColorNextStep.getAlpha())), 0.1f, false, false, partialTicks);
                }
            }
        }

    }

}

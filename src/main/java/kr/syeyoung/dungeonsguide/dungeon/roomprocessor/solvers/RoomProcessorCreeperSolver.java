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

package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.solvers;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.utils.BlockCache;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.VectorUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.joml.Vector3i;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RoomProcessorCreeperSolver extends GeneralRoomProcessor {

    private static final Color[] colors = new Color[]{Color.red, Color.orange, Color.green, Color.cyan, Color.blue, Color.pink, Color.yellow, Color.darkGray, Color.lightGray};
    private final List<Vector3i[]> poses = new ArrayList<>();
    private final boolean bugged = false;

    public RoomProcessorCreeperSolver(DungeonRoom dungeonRoom) {
        super(dungeonRoom);

        findCreeperAndDoPoses();
    }

    private boolean check(AxisAlignedBB axis, Vec3 vec) {
        if (vec == null) return false;
        return axis.isVecInside(vec);
    }

    private void findCreeperAndDoPoses() {
        World w = getDungeonRoom().getContext().getWorld();
        List<Vector3i> prismarines = new ArrayList<>();
        final Vector3i low = getDungeonRoom().getMin().add(0, -2, 0);
        final Vector3i high = getDungeonRoom().getMax().add(0, 20, 0);
        final AxisAlignedBB axis = AxisAlignedBB.fromBounds(
                low.x + 17, low.y + 7, low.z + 17,
                low.x + 16, low.y + 10.5, low.z + 16
        );

        for (BlockPos pos : BlockPos.getAllInBox(VectorUtils.Vec3iToBlockPos(low), VectorUtils.Vec3iToBlockPos(high) )) {
            Block b = DungeonsGuide.getDungeonsGuide().getBlockCache().getBlockState(pos).getBlock();
            if (b == Blocks.prismarine || b == Blocks.sea_lantern) {
                for (EnumFacing face : EnumFacing.VALUES) {
                    if (w.getBlockState(pos.offset(face)).getBlock() == Blocks.air) {
                        prismarines.add(VectorUtils.BlockPosToVec3i(pos));
                        break;
                    }
                }
            }
        }
        double offset = 0.1;

        while (prismarines.size() > 1) {
            Vector3i first = prismarines.get(0);
            Vector3i highestMatch = null;
            int highestDist = 0;
            for (int i = 1; i < prismarines.size(); i++) {
                Vector3i second = prismarines.get(i);

                if (second.distanceSquared(first) < highestDist) continue;

                Vec3 startLoc = new Vec3(VectorUtils.Vec3iToBlockPos(first)).addVector(0.5, 0.5, 0.5);
                Vec3 dest = new Vec3(VectorUtils.Vec3iToBlockPos(second)).addVector(0.5, 0.5, 0.5);
                if (check(axis, startLoc.getIntermediateWithYValue(dest, axis.minY + offset)) ||
                        check(axis, startLoc.getIntermediateWithYValue(dest, axis.maxY - offset)) ||
                        check(axis, startLoc.getIntermediateWithXValue(dest, axis.minX + offset)) ||
                        check(axis, startLoc.getIntermediateWithXValue(dest, axis.maxX - offset)) ||
                        check(axis, startLoc.getIntermediateWithZValue(dest, axis.minZ + offset)) ||
                        check(axis, startLoc.getIntermediateWithZValue(dest, axis.maxZ - offset))) {
                    highestDist = (int) second.distanceSquared(first);
                    highestMatch = second;
                }

            }


            if (highestMatch == null) {
                prismarines.remove(first);
            } else {
                prismarines.remove(first);
                prismarines.remove(highestMatch);
                poses.add(new Vector3i[]{first, highestMatch});
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (bugged) {
            findCreeperAndDoPoses();
        }
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!DgOneCongifConfig.creeperSolver) return;
        World w = getDungeonRoom().getContext().getWorld();
        for (int i = 0; i < poses.size(); i++) {
            Vector3i[] poset = poses.get(i);
            Color color = colors[i % colors.length];

            boolean oneIsConnected = BlockCache.getBlockState(poset[0]).getBlock() != Blocks.sea_lantern &&
                    BlockCache.getBlockState(poset[1]).getBlock() != Blocks.sea_lantern;
            RenderUtils.drawLine(new Vec3(poset[0].x + 0.5, poset[0].y + 0.5, poset[0].z + 0.5),
                    new Vec3(poset[1].x + 0.5, poset[1].y + 0.5, poset[1].z + 0.5), oneIsConnected ? new Color(0, 0, 0, 50) : color, partialTicks, true);
        }
        final Vector3i low = getDungeonRoom().getMin();
        final AxisAlignedBB axis = AxisAlignedBB.fromBounds(
                low.x + 17, low.y + 5, low.z + 17,
                low.x + 16, low.y + 8.5, low.z + 16
        );
        RenderUtils.highlightBox(axis, new Color(0x4400FF00, true), partialTicks, false);
    }
}

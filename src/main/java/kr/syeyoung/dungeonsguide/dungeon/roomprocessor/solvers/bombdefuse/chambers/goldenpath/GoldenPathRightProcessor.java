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

package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.solvers.bombdefuse.chambers.goldenpath;

import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.solvers.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.solvers.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.solvers.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import kr.syeyoung.dungeonsguide.utils.VectorUtils;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import org.joml.Vector3i;

import java.awt.*;
import java.util.LinkedList;

public class GoldenPathRightProcessor extends GeneralDefuseChamberProcessor {
    // 1 up 2 right 3 down 4 left
    private static final Point[] vectors = new Point[]{
            new Point(0, 1),
            new Point(-1, 0),
            new Point(0, -1),
            new Point(1, 0)
    };
    private final Vector3i center;
    private final LinkedList<Vector3i> blocksolution = new LinkedList<Vector3i>();
    public GoldenPathRightProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);

        center = chamber.getBlockPos(4, 4, 4);
    }

    @Override
    public String getName() {
        return "goldenPathRight";
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        RenderUtils.drawTextAtWorld(blocksolution.size() == 0 ? "Answer not received yet. Visit left room to obtain solution" : "", center.x + 0.5f, center.y, center.z + 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);

        RenderUtils.drawLines(blocksolution, new AColor(0, 0, 255, 0), 1, partialTicks, false);

    }

    @Override
    public void chatReceived(IChatComponent chat) {
        super.chatReceived(chat);
        if (chat.getFormattedText().contains("$DG-BDGP ")) {
            String data = chat.getFormattedText().substring(chat.getFormattedText().indexOf("$DG-BDGP"));
            String actual = TextUtils.stripColor(data).trim().split(" ")[1].trim();

            blocksolution.clear();
            BlockPos lastLoc = new BlockPos(4, 0, 0);
            blocksolution.addFirst(getChamber().getBlockPos(4, 1, 0));
            for (Character c : actual.toCharArray()) {
                int dir = Integer.parseInt(c + "") % 4;
                lastLoc = lastLoc.add(vectors[dir].x, 0, vectors[dir].y);
                blocksolution.add(getChamber().getBlockPos(lastLoc.getX(), 1, lastLoc.getZ()));
            }

            World w = getChamber().getRoom().getContext().getWorld();
            for (int x = 0; x < 9; x++) {
                for (int z = 0; z < 6; z++) {
                    Vector3i pos = getChamber().getBlockPos(x, 1, z);
                    if (blocksolution.contains(pos)) {
                        w.setBlockState(VectorUtils.Vec3iToBlockPos(pos), Blocks.light_weighted_pressure_plate.getDefaultState());
                    } else {
                        w.setBlockState(VectorUtils.Vec3iToBlockPos(pos), Blocks.wooden_pressure_plate.getDefaultState());
                    }
                }
            }
        }
    }
}

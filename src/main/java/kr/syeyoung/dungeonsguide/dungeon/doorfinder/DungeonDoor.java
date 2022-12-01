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

package kr.syeyoung.dungeonsguide.dungeon.doorfinder;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.utils.BlockCache;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import org.joml.Vector3i;

import java.util.Set;

@Getter
public class DungeonDoor {
    private static final Set<Block> legalBlocks = Sets.newHashSet(Blocks.coal_block, Blocks.barrier, Blocks.monster_egg, Blocks.air, Blocks.stained_hardened_clay);
    private final World w;
    private final Vector3i position;

    public EDungeonDoorType getType() {
        return type;
    }

    private final EDungeonDoorType type;
    private boolean isZDir;

    public DungeonDoor(World world, Vector3i pos, EDungeonDoorType type) {
        this.w = world;
        this.position = pos;

        if (type == EDungeonDoorType.WITHER && BlockCache.getBlockState(pos).getBlock() == Blocks.air) type = EDungeonDoorType.WITHER_FAIRY;
        this.type = type;
        boolean exist = type.isExist();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Vector3i pos2 = pos.add(x, y, z);
                    Block block = BlockCache.getBlockState(pos2).getBlock();
                    if (BlockCache.getBlockState(pos).getBlock() != block) exist = false;
                }
            }
        }
        if (exist) {
            Vector3i ZCheck = pos.add(0, 0, 2);
            isZDir = BlockCache.getBlockState(ZCheck).getBlock() == Blocks.air;

            if (isZDir) {
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -2; z <= 2; z += 4) {
                            Vector3i pos2 = pos.add(x, y, z);
                            if (BlockCache.getBlockState(pos2).getBlock() != Blocks.air) exist = false;
                        }
                    }
                }
            } else {
                for (int x = -2; x <= 2; x += 4) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            Vector3i pos2 = pos.add(x, y, z);
                            if (BlockCache.getBlockState(pos2).getBlock() != Blocks.air) exist = false;
                        }
                    }
                }
            }
        }
        if (!exist) {
            isZDir = false;
        }
    }
}

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

package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.solvers.waterpuzzle.nodes;

import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.solvers.waterpuzzle.LeverState;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.solvers.waterpuzzle.WaterNode;
import kr.syeyoung.dungeonsguide.utils.BlockCache;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import org.joml.Vector3i;

@Data
@AllArgsConstructor
public class WaterNodeWall implements WaterNode {

    Vector3i blockPos;
    private int x, y;

    @Override
    public boolean canWaterGoThrough() {
        return false;
    }

    @Override
    public LeverState getCondition() {
        return null;
    }

    @Override
    public boolean isWaterFilled(World w) {
        Block b = BlockCache.getBlock(blockPos);
        return b == Blocks.water || b == Blocks.flowing_water;
    }


    public String toString() {
        return "W";
    }
}

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

package kr.syeyoung.dungeonsguide.dungeon.data;

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.VectorUtils;
import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.vecmath.Vector2d;
import java.io.Serializable;

@Data
public class OffsetPoint implements Cloneable, Serializable {
    private static final long serialVersionUID = 3102336358774967540L;

    private int x;
    private int y;
    private int z;

    public OffsetPoint(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public OffsetPoint(DungeonRoom dungeonRoom, Vector3i pos) {
        setPosInWorld(dungeonRoom, pos);
    }

    public OffsetPoint(DungeonRoom dungeonRoom, Vec3 pos) {
        setPosInWorld(dungeonRoom, new Vector3i((int) pos.xCoord, (int) pos.yCoord, (int) pos.zCoord));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public void setPosInWorld(DungeonRoom dungeonRoom, Vector3i pos) {
        Vector2d vector2d = new Vector2d(pos.x - dungeonRoom.getMin().x, pos.z - dungeonRoom.getMin().z);
        for (int i = 0; i < dungeonRoom.getRoomMatcher().getRotation(); i++) {
            vector2d = VectorUtils.rotateClockwise(vector2d);
            if (i % 2 == 0) {
                vector2d.x += dungeonRoom.getDungeonRoomInfo().getBlocks()[0].length - 1; // + Z
            } else {
                vector2d.x += dungeonRoom.getDungeonRoomInfo().getBlocks().length - 1; // + X
            }
        }

        this.x = (int) vector2d.x;
        this.z = (int) vector2d.y;
        this.y = pos.y - dungeonRoom.getMin().y;
    }

    public Vector3i toRotatedRelBlockPos(DungeonRoom dungeonRoom) {
        Vector2d rot = new Vector2d(x, z);
        for (int i = 0; i < dungeonRoom.getRoomMatcher().getRotation(); i++) {
            rot = VectorUtils.rotateCounterClockwise(rot);
            if (i % 2 == 0) {
                rot.y += dungeonRoom.getMax().z - dungeonRoom.getMin().z + 1; // + Z
            } else {
                rot.y += dungeonRoom.getMax().x - dungeonRoom.getMin().x + 1; // + X
            }
        }

        return new Vector3i((int) rot.x, y, (int) rot.y);
    }

    public Block getBlock(DungeonRoom dungeonRoom) {
        Vector3i relBp = toRotatedRelBlockPos(dungeonRoom);

        return dungeonRoom.getRelativeBlockAt(relBp.x, relBp.y, relBp.z);
    }

    public Vector3i getVector3i(DungeonRoom dungeonRoom) {
        Vector3i relBp = toRotatedRelBlockPos(dungeonRoom);
        return dungeonRoom.getRelativeBlockPosAt(relBp.x, relBp.y, relBp.z);
    }

    public Vector3d getVector3d(DungeonRoom dungeonRoom) {
        Vector3i relBp = toRotatedRelBlockPos(dungeonRoom);
        Vector3i relativeBlockPosAt = dungeonRoom.getRelativeBlockPosAt(relBp.x, relBp.y, relBp.z);
        return new Vector3d(relativeBlockPosAt.x, relativeBlockPosAt.y, relativeBlockPosAt.z);
    }

    public BlockPos getBlockPos(DungeonRoom dungeonRoom) {
        Vector3i relBp = toRotatedRelBlockPos(dungeonRoom);
        Vector3i relativeBlockPosAt = dungeonRoom.getRelativeBlockPosAt(relBp.x, relBp.y, relBp.z);
        return new BlockPos(relativeBlockPosAt.x, relativeBlockPosAt.y, relativeBlockPosAt.z);
    }

    public int getData(DungeonRoom dungeonRoom) {
        Vector3i relBp = toRotatedRelBlockPos(dungeonRoom);

        return dungeonRoom.getRelativeBlockDataAt(relBp.x, relBp.y, relBp.z);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return new OffsetPoint(x, y, z);
    }

    @Override
    public String toString() {
        return "OffsetPoint{x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}

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

package kr.syeyoung.dungeonsguide.dungeon;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.detection.RoomMatcher;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonDoor;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.EDungeonDoorType;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.impl.DungeonRoomDoor;
import kr.syeyoung.dungeonsguide.dungeon.pathfinding.DungeonRoomAccessor;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.ProcessorFactory;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.utils.BlockCache;
import kr.syeyoung.dungeonsguide.utils.VectorUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector3i;

import javax.vecmath.Vector2d;
import java.util.*;

@Getter
public class DungeonRoom implements DungeonRoomAccessor {
    public static final Set<Block> allowed = Sets.newHashSet(Blocks.air, Blocks.water, Blocks.lava, Blocks.flowing_water, Blocks.flowing_lava, Blocks.vine, Blocks.ladder
            , Blocks.standing_sign, Blocks.wall_sign, Blocks.trapdoor, Blocks.iron_trapdoor, Blocks.wooden_button, Blocks.stone_button, Blocks.fire,
            Blocks.torch, Blocks.rail, Blocks.golden_rail, Blocks.activator_rail, Blocks.detector_rail, Blocks.carpet, Blocks.redstone_torch);
    public static final IBlockState preBuilt = Blocks.stone.getStateFromMeta(2);
    private static final Set<Vector2d> directions = Sets.newHashSet(new Vector2d(0, 16), new Vector2d(0, -16), new Vector2d(16, 0), new Vector2d(-16, 0));
    private static final float playerWidth = 0.3f;
    private final List<Vector2i> unitPoints;
    private final short shape;
    private final byte color;

    public static boolean isValidBlock(IBlockState state) {
        return state.equals(preBuilt) || allowed.contains(state.getBlock());
    }

    public Vector3i getMin() {
        return min;
    }

    private final Vector3i min;

    public Vector3i getMax() {
        return max;
    }

    private final Vector3i max;
    private final Vector2i minRoomPt;

    public DungeonContext getContext() {
        return context;
    }

    private final DungeonContext context;

    public List<DungeonDoor> getDoors() {
        return doors;
    }

    private final List<DungeonDoor> doors = new ArrayList<>();
    private final int unitWidth; // X
    private final int unitHeight; // Z

    public Map<String, Object> getRoomContext() {
        return roomContext;
    }

    private final Map<String, Object> roomContext = new HashMap<>();
    // These values are doubled
    private final int minx;
    private final int miny;
    private final int minz;
    private final int maxx;
    private final int maxy;
    private final int maxz;
    private final int lenx;
    private final int leny;
    private final int lenz;
    long[] arr;

    public DungeonRoomInfo getDungeonRoomInfo() {
        return dungeonRoomInfo;
    }

    private DungeonRoomInfo dungeonRoomInfo;

    public int getTotalSecrets() {
        return totalSecrets;
    }

    public void setTotalSecrets(int totalSecrets) {
        this.totalSecrets = totalSecrets;
    }

    private int totalSecrets = -1;

    public RoomState getCurrentState() {
        return currentState;
    }

    private RoomState currentState = RoomState.DISCOVERED;
    private Map<String, DungeonMechanic> cached = null;

    public RoomProcessor getRoomProcessor() {
        return roomProcessor;
    }

    private RoomProcessor roomProcessor;

    public RoomMatcher getRoomMatcher() {
        return roomMatcher;
    }

    private RoomMatcher roomMatcher = null;

    public DungeonRoom(List<Vector2i> points, short shape, byte color, Vector3i min, Vector3i max, DungeonContext context, Set<Tuple<Vector2d, EDungeonDoorType>> doorsAndStates) {
        this.unitPoints = points;
        this.shape = shape;
        this.color = color;
        this.min = min;
        this.max = max;
        this.context = context;

        minRoomPt = new Vector2i(Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (Vector2i pt : unitPoints) {
            if (pt.x < minRoomPt.x) minRoomPt.x = pt.x;
            if (pt.y < minRoomPt.y) minRoomPt.y = pt.y;
        }
        unitWidth = (int) Math.ceil(max.x - min.x / 32.0);
        unitHeight = (int) Math.ceil(max.z - min.z / 32.0);

        minx = min.x * 2;
        miny = 0;
        minz = min.z * 2;
        maxx = max.x * 2 + 2;
        maxy = 255 * 2 + 2;
        maxz = max.z * 2 + 2;

        lenx = maxx - minx;
        leny = maxy - miny;
        lenz = maxz - minz;
        arr = new long[lenx * leny * lenz * 2 / 8];

        buildDoors(doorsAndStates);
        buildRoom();
        updateRoomProcessor();
    }

    public Map<String, DungeonMechanic> getMechanics() {
        if (cached == null || EditingContext.getEditingContext() != null) {
            cached = new HashMap<>(dungeonRoomInfo.getMechanics());
            int index = 0;
            for (DungeonDoor door : doors) {
                if (door.getType().isExist())
                    cached.put((door.getType().getName()) + "-" + (++index), new DungeonRoomDoor(this, door));
            }
        }
        return cached;
    }

    public void setCurrentState(RoomState currentState) {
        this.currentState = currentState;
    }

    private void buildDoors(Set<Tuple<Vector2d, EDungeonDoorType>> doorsAndStates) {
        Set<Tuple<Vector3i, EDungeonDoorType>> positions = new HashSet<>();
        Vector3i pos = context.mapProcessor.roomPointToWorldPoint(minRoomPt).add(16, 0, 16);
        for (Tuple<Vector2d, EDungeonDoorType> doorsAndState : doorsAndStates) {
            Vector2d vector2d = doorsAndState.getFirst();
            Vector3i neu = pos.add((int) (vector2d.x * 32), 0, (int) (vector2d.y * 32));
            positions.add(new Tuple<>(neu, doorsAndState.getSecond()));
        }

        for (Tuple<Vector3i, EDungeonDoorType> door : positions) {
            doors.add(new DungeonDoor(Minecraft.getMinecraft().theWorld, door.getFirst(), door.getSecond()));
        }
    }

    private void buildRoom() {
        if (roomMatcher == null)
            roomMatcher = new RoomMatcher(this);
        DungeonRoomInfo dungeonRoomInfo = roomMatcher.match();
        if (dungeonRoomInfo == null) {
            dungeonRoomInfo = roomMatcher.createNew();
            if (color == 18) dungeonRoomInfo.setProcessorId("bossroom");
        }
        this.dungeonRoomInfo = dungeonRoomInfo;
        totalSecrets = dungeonRoomInfo.getTotalSecrets();
    }

    public void updateRoomProcessor() {
        this.roomProcessor = ProcessorFactory.createRoomProcessor(dungeonRoomInfo.getProcessorId(), this);
    }

    public Block getRelativeBlockAt(int x, int y, int z) {
        // validate x y z's
        if (canAccessRelative(x, z)) {
            BlockPos pos = new BlockPos(x, y, z).add(min.x, min.y, min.z);
            return BlockCache.getBlockState(pos).getBlock();
        }
        return null;
    }

    public Vector3i getRelativeBlockPosAt(int x, int y, int z) {
        return new Vector3i(x, y, z).add(min.x, min.y, min.z);
    }

    public int getRelativeBlockDataAt(int x, int y, int z) {
        // validate x y z's
        if (canAccessRelative(x, z)) {
            BlockPos pos = new BlockPos(x, y, z).add(min.x, min.y, min.z);
            IBlockState iBlockState = BlockCache.getBlockState(pos);
            return iBlockState.getBlock().getMetaFromState(iBlockState);
        }
        return -1;
    }

    /**
     * This code appears to be a method for determining whether a given point is accessible in a three-dimensional space.
     * The method takes the position of the point as input (represented as a Vector3i object)
     * and returns a boolean value indicating whether the point is accessible.
     * The method first gets a MapProcessor object from the current context,
     * which is used to convert the given point from world coordinates to room coordinates.
     * It then subtracts the minimum room coordinates (minRoomPt) from the converted point,
     * to get the relative coordinates of the point within the room.
     * Next, the method calculates the index of the point in a bit array (shape)
     * that is used to represent the accessible/inaccessible state of the room.
     * The method then checks the value of the bit at this index in the shape array,
     * and returns true if the bit is set to 1, indicating that the point is accessible.
     * If the bit is not set to 1, the method returns false to indicate that the point is not accessible.
     */
    public boolean canAccessAbsolute(Vector3i pos) {
        MapProcessor mapProcessor = this.context.mapProcessor;
        Vector2i roomPt = mapProcessor.worldPointToRoomPoint(pos);

        roomPt.add(-minRoomPt.x, -minRoomPt.y);
        return (shape >> (roomPt.y * 4 + roomPt.x) & 0x1) > 0;
    }

    /**
     * This code appears to be a method for determining whether a given point is accessible in a two-dimensional space.
     * The method takes the x and z coordinates of the point as input and returns a boolean value indicating whether the point is accessible.
     * The method first checks if the given coordinates are within the bounds of the space, and returns false if they are not.
     * It then calculates the index of the point in a bit array (shape) that is used to represent the accessible/inaccessible state of the space.
     * The method then checks the value of the bit at this index in the shape array, and returns true if the bit is set to 1, indicating that the point is accessible.
     * If the bit is not set to 1, the method returns false to indicate that the point is not accessible.
     * @param x
     * @param z
     * @return
     */
    public boolean canAccessRelative(int x, int z) {
        return x >= 0 && z >= 0 && (shape >> ((z / 32) * 4 + (x / 32)) & 0x1) > 0;
    }

    /**
     * This code appears to be a method for determining whether a given position in a three-dimensional space is blocked or not.
     * The method takes the x, y, and z coordinates of the position as input and returns a boolean value indicating whether the position is blocked.
     *
     * The method first checks if the given coordinates are outside the bounds of the space, and returns true if they are.
     * It then calculates the index of the given position in an array of bits (arr) that is used to represent the blocked/unblocked state of the space.
     * The method then checks the value of the bit at this index in the arr array.
     * If the bit is set to 0, the method determines whether the position is blocked by checking if there are any solid blocks within a certain distance of the position.
     * This is done by constructing an AxisAlignedBB object that represents the bounding box of the position and its surrounding area,
     * and then iterating over all blocks within this bounding box.
     * If any of these blocks are solid and not equal to a pre-built block, the method sets the bit in the arr array to 1 and returns true to indicate that the position is blocked.
     * If no such blocks are found, the method sets the bit in the arr array to 2 and returns false to indicate that the position is not blocked.
     * If the bit in the arr array was already set to 1, the method simply returns true, and if it was set to 2, the method returns false.
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    @Override
    public boolean isBlocked(int x, int y, int z) {
        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y >= maxy) return true;
        int dx = x - minx, dy = y - miny, dz = z - minz;
        int bitIdx = dx * leny * lenz + dy * lenz + dz;
        int location = bitIdx / 4;
        int bitStart = (2 * (bitIdx % 4));
        long theBit = arr[location];
        if (((theBit >> bitStart) & 0x2) > 0) return ((theBit >> bitStart) & 1) > 0;
        float wX = x / 2.0f, wY = y / 2.0f, wZ = z / 2.0f;


        AxisAlignedBB bb = AxisAlignedBB.fromBounds(wX - playerWidth, wY, wZ - playerWidth, wX + playerWidth, wY + 1.9f, wZ + playerWidth);

        int i = MathHelper.floor_double(bb.minX);
        int j = MathHelper.floor_double(bb.maxX + 1.0D);
        int k = MathHelper.floor_double(bb.minY);
        int l = MathHelper.floor_double(bb.maxY + 1.0D);
        int i1 = MathHelper.floor_double(bb.minZ);
        int j1 = MathHelper.floor_double(bb.maxZ + 1.0D);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        List<AxisAlignedBB> list = new ArrayList<>();
        for (int k1 = i; k1 < j; ++k1) {
            for (int l1 = i1; l1 < j1; ++l1) {
                for (int i2 = k - 1; i2 < l; ++i2) {
                    blockPos.set(k1, i2, l1);
                    IBlockState iblockstate1 = BlockCache.getBlockState(blockPos);
                    Block b = iblockstate1.getBlock();
                    if (!b.getMaterial().blocksMovement()) continue;
                    if (b.isFullCube() && i2 == k - 1) continue;
                    if (iblockstate1.equals(preBuilt)) continue;
                    if (b.isFullCube()) {
                        theBit |= (3L << bitStart);
                        arr[location] = theBit;
                        return true;
                    }
                    try {
                        b.addCollisionBoxesToList(Minecraft.getMinecraft().theWorld, blockPos, iblockstate1, bb, list, null);
                    } catch (Exception e) {
                        return true;
                    }
                    if (!list.isEmpty()) {
                        theBit |= (3L << bitStart);
                        arr[location] = theBit;
                        return true;
                    }
                }
            }
        }
        theBit |= 2L << bitStart;
        arr[location] = theBit;
        return false;
    }

    public void resetBlock(BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    resetBlock(pos.getX() * 2 + x, pos.getY() * 2 + y, pos.getZ() * 2 + z);
                }
            }
        }
    }

    private void resetBlock(int x, int y, int z) {
        if (x < minx || z < minz || x >= maxx || z >= maxz || y < miny || y >= maxy) return;
        int dx = x - minx;
        int dy = y - miny;
        int dz = z - minz;
        int bitIdx = dx * leny * lenz + dy * lenz + dz;
        int location = bitIdx / 4;
        arr[location] = 0;
    }

    @Nullable
    @Override
    public IBlockState getBlockState(@NotNull org.joml.Vector3d location) {
        return BlockCache.getBlockState(VectorUtils.Vec3ToBlockPos(location));
    }

    @AllArgsConstructor
    @Getter
    public enum RoomState {
        DISCOVERED(0), COMPLETE_WITHOUT_SECRETS(0), FINISHED(0), FAILED(-14);
        private final int scoreModifier;
    }
}

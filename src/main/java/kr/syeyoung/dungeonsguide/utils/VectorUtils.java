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

package kr.syeyoung.dungeonsguide.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.vecmath.Vector2d;

public class VectorUtils {
    // Ior rooms, different coordinate system is used. Y Increses as marker goes down. X is same.

    public static Vector2d rotateCounterClockwise(Vector2d vector2d) {
        return new Vector2d(vector2d.y, -vector2d.x);
    }
    public static Vector2d rotateClockwise(Vector2d vector2d) {
        return new Vector2d(-vector2d.y, vector2d.x);
    }

    public static Vec3 Vec3iToVec3(Vector3i pos) {
        return new Vec3(pos.x, pos.y, pos.z);
    }

    @Data @AllArgsConstructor
    public static class ProjectionResult {
        private float x;
        private float y;
        private boolean back;
    }

    public static double distSquared(Vec3 lookVec, Vec3 posVec, Vec3 objectVec) {
        Vec3 v = objectVec.subtract(posVec);
        double t = v.dotProduct(lookVec);
        Vec3 p = posVec.addVector(lookVec.xCoord * t, lookVec.yCoord * t, lookVec.zCoord * t);
        return p.squareDistanceTo(objectVec) / p.squareDistanceTo(posVec);
    }

    public static double distSquared(Vector3d lookVec, Vector3d posVec, Vector3d objectVec) {
        Vector3d v = objectVec.sub(posVec);
        double t = v.dot(lookVec);
        Vector3d p = posVec.add(lookVec.x * t, lookVec.y * t, lookVec.z * t);
        return p.distanceSquared(objectVec) / p.distanceSquared(posVec);
    }

    @NotNull
    public static BlockPos Vec3ToBlockPos(@NotNull Vector3d vec){
        return new BlockPos(vec.x, vec.y, vec.z);
    }

    public static Vector3d vec3ToVec3d(Vec3 vec){
        return new Vector3d(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public static Vector3i getPlayerVector3i(){
        val player = Minecraft.getMinecraft().thePlayer;
        return new Vector3i((int) player.posX, (int) player.posY, (int) player.posZ);
    }

    public static Vector3d getPlayerVector3d(){
        val player = Minecraft.getMinecraft().thePlayer;
        return new Vector3d( player.posX,  player.posY,  player.posZ);
    }

    @NotNull
    public static BlockPos Vec3iToBlockPos(@NotNull Vector3i vec){
        return new BlockPos(vec.x, vec.y, vec.z);
    }

    @NotNull
    public static Vector3i BlockPosToVec3i(@NotNull BlockPos vec){
        return new Vector3i(vec.getX(), vec.getY(), vec.getZ());
    }


}

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
package kr.syeyoung.dungeonsguide.dungeon.doorfinder

import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.solvers.bossfight.BossfightProcessor
import net.minecraft.util.BlockPos
import net.minecraft.world.World
import javax.vecmath.Vector2d

interface DungeonSpecificDataProvider {
    fun findDoor(w: World, dungeonName: String): BlockPos?
    fun findDoorOffset(w: World, dungeonName: String): Vector2d?
    fun createBossfightProcessor(w: World, dungeonName: String): BossfightProcessor?
    fun hasTrapRoom(dungeonName: String): Boolean
    fun secretPercentage(dungeonName: String): Double
    fun speedSecond(dungeonName: String): Int
}
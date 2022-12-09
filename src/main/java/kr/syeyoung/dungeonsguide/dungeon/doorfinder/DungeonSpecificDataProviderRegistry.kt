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

import kr.syeyoung.dungeonsguide.dungeon.doorfinder.catacombs.impl.MasterModeDataProvider
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.catacombs.impl.NormalModeDataProvider
import java.util.regex.Pattern

object DungeonSpecificDataProviderRegistry {
    @JvmField
    val doorFinders: MutableMap<Pattern, DungeonSpecificDataProvider> = HashMap()

    init {
        doorFinders[Pattern.compile("The Catacombs (?:F[0-9]|E)")] = NormalModeDataProvider()
        doorFinders[Pattern.compile("The Catacombs (?:M[0-9])")] = MasterModeDataProvider()
    }

    @JvmStatic
    fun getDoorFinder(dungeonName: String?): DungeonSpecificDataProvider? {
        for ((key, value) in doorFinders) {
            if (key.matcher(dungeonName).matches()) return value
        }
        return null
    }
}
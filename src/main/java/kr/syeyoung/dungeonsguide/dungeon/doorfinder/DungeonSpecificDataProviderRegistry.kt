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
    fun getDoorFinder(dungeonName: String): DungeonSpecificDataProvider? {
        for ((key, value) in doorFinders) {
            if (key.matcher(dungeonName).matches()) return value
        }
        return null
    }
}
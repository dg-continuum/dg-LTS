package kr.syeyoung.dungeonsguide.cosmetics.data

import java.util.*

data class ActiveCosmetic(
    var activityUID: UUID? = null,
    var playerUID: UUID? = null,
    var cosmeticData: UUID? = null,
    var username: String? = null,
)
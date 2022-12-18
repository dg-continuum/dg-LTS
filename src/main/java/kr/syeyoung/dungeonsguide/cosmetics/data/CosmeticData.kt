package kr.syeyoung.dungeonsguide.cosmetics.data

import java.util.*

data class CosmeticData(
    val id: UUID? = null,
    val cosmeticType: String? = null,
    val reqPerm: String? = null,
    val data: String? = null,
)
package kr.syeyoung.dungeonsguide.dungeon.actions

import kr.syeyoung.dungeonsguide.config.types.AColor

data class ActionPlanProperties (
    var isPathfind: Boolean = false,
    var lineRefreshRate: Int = 0,
    var lineColor: AColor? = null,
    var lineWidth: Float = 0f,
    var beacon: Boolean = false,
    var beaconColor: AColor? = null,
    var beaconBeamColor: AColor? = null
)
package kr.syeyoung.dungeonsguide.dungeon.actions.tree

import kr.syeyoung.dungeonsguide.config.types.AColor


class ActionRouteProperties {
    var isPathfind = false
    var lineRefreshRate = 0
    var lineColor: AColor? = null
    var lineWidth = 0f
    var beacon = false
    var beaconColor: AColor? = null
    var beaconBeamColor: AColor? = null
}
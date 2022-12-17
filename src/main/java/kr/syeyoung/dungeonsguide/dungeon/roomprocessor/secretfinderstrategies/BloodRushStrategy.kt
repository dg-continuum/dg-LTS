package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.secretfinderstrategies

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionState
import kr.syeyoung.dungeonsguide.dungeon.mechanics.impl.DungeonRoomDoor
import kr.syeyoung.dungeonsguide.features.FeatureRegistry
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig

class BloodRushStrategy(room: DungeonRoom) : SecretGuideStrategy(room) {
    override fun init() {
        DgOneCongifConfig.bloodRush = true
        for ((key, value) in room.mechanics) {
            if (value is DungeonRoomDoor) {
                if (value.doorfinder.type.isHeadToBlood) {
                    createActionRoute(
                        key,
                        ActionState.navigate,
                        FeatureRegistry.SECRET_BLOOD_RUSH_LINE_PROPERTIES.routeProperties
                    )
                }
            }
        }
    }
}
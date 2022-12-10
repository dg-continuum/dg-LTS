package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.secretfinderstrategies

import kr.syeyoung.dungeonsguide.dungeon.actions.ActionState
import kr.syeyoung.dungeonsguide.dungeon.mechanics.impl.DungeonRoomDoor
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.GeneralRoomProcessor
import kr.syeyoung.dungeonsguide.features.FeatureRegistry
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig

class BloodRushStrategy(parent: GeneralRoomProcessor) : SecretGuideStrategy(parent) {
    override fun init() {
        DgOneCongifConfig.bloodRush = true
        for ((key, value) in parent.dungeonRoom.mechanics) {
            if (value is DungeonRoomDoor) {
                if (value.doorfinder.type.isHeadToBlood) {
                    addAction(
                        key,
                        ActionState.navigate,
                        FeatureRegistry.SECRET_BLOOD_RUSH_LINE_PROPERTIES.routeProperties
                    )
                }
            }
        }
    }
}
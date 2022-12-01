package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.secretfinderstrategies

import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonSecret
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.GeneralRoomProcessor
import kr.syeyoung.dungeonsguide.features.FeatureRegistry
import kr.syeyoung.dungeonsguide.oneconfig.secrets.PathfindToALlPage

class PathfindToAllStrategy(parent: GeneralRoomProcessor) : SecretGuideStrategy(parent) {
    override fun init() {
        for ((key, value1) in parent.dungeonRoom.dungeonRoomInfo.mechanics) {
            if (value1 is DungeonSecret && value1.getSecretStatus(parent.dungeonRoom) != DungeonSecret.SecretStatus.FOUND) {
                if (PathfindToALlPage.pfTaBAT && value1.secretType == DungeonSecret.SecretType.BAT) {
                    addAction(
                        key,
                        "found",
                        FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_BAT.routeProperties
                    )
                }
                if (PathfindToALlPage.pfTaCHEST && value1.secretType == DungeonSecret.SecretType.CHEST) {
                    addAction(
                        key,
                        "found",
                        FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_CHEST.routeProperties
                    )
                }
                if (PathfindToALlPage.pfTaESSENCE && value1.secretType == DungeonSecret.SecretType.ESSENCE) {
                    addAction(
                        key,
                        "found",
                        FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_ESSENCE.routeProperties
                    )
                }
                if (PathfindToALlPage.pfTaITEMDROP && value1.secretType == DungeonSecret.SecretType.ITEM_DROP) {
                    addAction(
                        key,
                        "found",
                        FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_ITEM_DROP.routeProperties
                    )
                }
            }
        }
    }
}
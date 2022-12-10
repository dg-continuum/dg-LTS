package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.secretfinderstrategies

import kr.syeyoung.dungeonsguide.dungeon.actions.ActionState
import kr.syeyoung.dungeonsguide.dungeon.mechanics.impl.DungeonSecret
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.GeneralRoomProcessor
import kr.syeyoung.dungeonsguide.features.FeatureRegistry
import kr.syeyoung.dungeonsguide.oneconfig.secrets.PathfindToALlPage

class PathfindToAllStrategy(parent: GeneralRoomProcessor) : SecretGuideStrategy(parent) {
    override fun init() {
        parent.dungeonRoom.dungeonRoomInfo.mechanics.forEach { (mechanicName, mechanic) ->
            if (mechanic is DungeonSecret && mechanic.getSecretStatus(parent.dungeonRoom) != DungeonSecret.SecretStatus.FOUND) {
                if (PathfindToALlPage.pfTaBAT && mechanic.secretType == DungeonSecret.SecretType.BAT) {
                    addAction(
                        mechanicName,
                        ActionState.found,
                        FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_BAT.routeProperties
                    )
                }
                if (PathfindToALlPage.pfTaCHEST && mechanic.secretType == DungeonSecret.SecretType.CHEST) {
                    addAction(
                        mechanicName,
                        ActionState.found,
                        FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_CHEST.routeProperties
                    )
                }
                if (PathfindToALlPage.pfTaESSENCE && mechanic.secretType == DungeonSecret.SecretType.ESSENCE) {
                    addAction(
                        mechanicName,
                        ActionState.found,
                        FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_ESSENCE.routeProperties
                    )
                }
                if (PathfindToALlPage.pfTaITEMDROP && mechanic.secretType == DungeonSecret.SecretType.ITEM_DROP) {
                    addAction(
                        mechanicName,
                        ActionState.found,
                        FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_ITEM_DROP.routeProperties
                    )
                }
            }
        }
    }
}
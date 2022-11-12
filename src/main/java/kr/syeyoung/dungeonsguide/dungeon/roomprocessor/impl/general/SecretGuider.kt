package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.general

import kr.syeyoung.dungeonsguide.DungeonsGuide
import kr.syeyoung.dungeonsguide.chat.ChatTransmitter
import kr.syeyoung.dungeonsguide.dungeon.DungeonFacade
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionComplete
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionMove
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionMoveNearestAir
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRoute
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRouteProperties
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonRoomDoor
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonSecret
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom
import kr.syeyoung.dungeonsguide.events.impl.KeyBindPressedEvent
import kr.syeyoung.dungeonsguide.events.impl.PlayerInteractEntityEvent
import kr.syeyoung.dungeonsguide.features.FeatureRegistry
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig
import kr.syeyoung.dungeonsguide.oneconfig.secrets.AutoPathfindPage
import kr.syeyoung.dungeonsguide.oneconfig.secrets.PathfindToALlPage
import kr.syeyoung.dungeonsguide.utils.SimpleFuse
import kr.syeyoung.dungeonsguide.utils.SimpleTimer
import kr.syeyoung.dungeonsguide.utils.VectorUtils
import net.minecraft.client.Minecraft
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import org.apache.logging.log4j.LogManager
import java.util.*

class SecretGuider(private val dungeonRoom: DungeonRoom) {
    val actionPath: MutableMap<String, ActionRoute> = HashMap()
    private val visited: MutableSet<String> = HashSet()
    private val tickedFuse = SimpleFuse()
    fun drawScreen(partialTicks: Float) {
        for (a in actionPath.values) {
            a.onRenderScreen(partialTicks)
        }
    }

    fun drawWorld(partialTicks: Float) {
        val finalSmallest = getBestFit(partialTicks)
        for (a in actionPath.values) {
            a.onRenderWorld(partialTicks, finalSmallest === a)
        }
    }

    private fun getBestFit(partialTicks: Float): ActionRoute? {
        var smallest: ActionRoute? = null
        var smallestTan = 0.002
        for (value in actionPath.values) {
            var target: BlockPos?
            val currentAction = value.currentAction
            target = if (currentAction is ActionMove) {
                currentAction.target?.getBlockPos(dungeonRoom)
            } else if (currentAction is ActionMoveNearestAir) {
                currentAction.target?.getBlockPos(dungeonRoom)
            } else {
                if (value.current >= 1) {
                    val abstractAction = value.actions[value.current - 1]
                    if (abstractAction is ActionMove) {
                        abstractAction.target.getBlockPos(dungeonRoom)
                    } else if (abstractAction is ActionMoveNearestAir) {
                        abstractAction.target.getBlockPos(dungeonRoom)
                    } else {
                        continue
                    }
                } else {
                    continue
                }
            }
            if (value.actionRouteProperties.lineRefreshRate != -1 && value.actionRouteProperties.isPathfind && !DgOneCongifConfig.freezePathfindingStatus) continue
            val e = Minecraft.getMinecraft().renderViewEntity
            val vectorV = VectorUtils.distSquared(
                e.getLook(partialTicks),
                e.getPositionEyes(partialTicks),
                Vec3(target).addVector(0.5, 0.5, 0.5)
            )
            if (vectorV < smallestTan) {
                smallest = value
                smallestTan = vectorV
            }
        }
        return smallest
    }

    private val reevaluateOurWholeExisitance = SimpleTimer(20)
    fun tick() {
        if (!tickedFuse.isBlown) {
            logger.info("Creating Pathfinding lines")
            when (DgOneCongifConfig.secretFindMode) {
                0 -> for ((key, value1) in dungeonRoom.dungeonRoomInfo.mechanics) {
                    if (value1 is DungeonSecret && value1.getSecretStatus(dungeonRoom) != DungeonSecret.SecretStatus.FOUND) {
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

                1 -> {
                    DgOneCongifConfig.bloodRush = true
                    for ((key, value1) in dungeonRoom.mechanics) {
                        if (value1 is DungeonRoomDoor) {
                            if (value1.doorfinder.type.isHeadToBlood) {
                                addAction(
                                    key,
                                    "navigate",
                                    FeatureRegistry.SECRET_BLOOD_RUSH_LINE_PROPERTIES.routeProperties
                                )
                            }
                        }
                    }
                }

                2 -> searchForNextTarget()
            }
        } else {
            // start searching for the lowest cost secret and navigate to it
            if (DgOneCongifConfig.secretFindMode == 2) {
                if (reevaluateOurWholeExisitance.shouldRun()) {
                    navigateToLowestScoreSecret()
                }
                reevaluateOurWholeExisitance.tick()
            }
        }
        tickedFuse.blow()
        val toRemove = HashSet<String>()
        for ((key, v) in actionPath) {
            v.onTick()
            if (v.currentAction is ActionComplete) {
                toRemove.add(key)
            }
        }
        for (s in toRemove) {
            visited.add(s)
            actionPath.remove(s)
        }
        for (value in dungeonRoom.mechanics.values) {
            if (value is DungeonSecret) {
                value.tick(dungeonRoom)
            }
        }
        if (toRemove.contains("AUTO-BROWSE") && AutoPathfindPage.autoBrowseToNext) {
            searchForNextTarget()
        }
    }

    fun navigateToLowestScoreSecret() {
        if (DgOneCongifConfig.usePathfindCostCacls) {
            DungeonFacade.ex.submit {
                val lowestWeightMechanic = getSimplesMechanic(true)
                if (!lowestWeightMechanic.isPresent) return@submit
                val mechanic = lowestWeightMechanic.get()

                // make sure we dont start navigating to the same secret twice
                if (mechanic == lastFoundMechanic) {
                    return@submit
                }
                ChatTransmitter.addToQueue("@fFound better solution")

                // remove the last one that has a higher cost
                actionPath.remove("AUTO-BROWSE")

                // start navigating to the new one
                lastFoundMechanic = mechanic
                addAction(
                    "AUTO-BROWSE",
                    mechanic,
                    "found",
                    FeatureRegistry.SECRET_LINE_PROPERTIES_AUTOPATHFIND.routeProperties
                )
            }
        } else {
            val lowestWeightMechanic = getSimplesMechanic(false)
            if (!lowestWeightMechanic.isPresent) return
            val mechanic = lowestWeightMechanic.get()

            // make sure we dont start navigating to the same secret twice
            if (mechanic == lastFoundMechanic) return
            ChatTransmitter.addToQueue("@fFound better solution")

            // remove the last one that has a higher cost
            actionPath.remove("AUTO-BROWSE")

            // start navigating to the new one
            lastFoundMechanic = mechanic
            addAction(
                "AUTO-BROWSE",
                mechanic,
                "found",
                FeatureRegistry.SECRET_LINE_PROPERTIES_AUTOPATHFIND.routeProperties
            )
        }
    }

    private var lastFoundMechanic: String? = null
    fun searchForNextTarget() {
        if (dungeonRoom.currentState == DungeonRoom.RoomState.FINISHED) {
            cancelAll()
            return
        }
        val lowestWeightMechanic = simplesMechanic
        if (lowestWeightMechanic.isPresent) {
            lastFoundMechanic = lowestWeightMechanic.get()
            addAction(
                "AUTO-BROWSE",
                lowestWeightMechanic.get(),
                "found",
                FeatureRegistry.SECRET_LINE_PROPERTIES_AUTOPATHFIND.routeProperties
            )
        } else {
            visited.clear()
        }
    }

    fun cancelAll() {
        actionPath.clear()
    }

    fun cancel(id: String) {
        actionPath.remove(id)
    }

    private val simplesMechanic: Optional<String>
        get() = getSimplesMechanic(false)

    private fun getSimplesMechanic(getAdvancedDistance: Boolean): Optional<String> {
        val pos = Minecraft.getMinecraft().thePlayer.position
        var lowestWeightMechanic: Map.Entry<String, DungeonMechanic>? = null
        var lowestCost = Float.MAX_VALUE.toDouble()
        for (mechanic in dungeonRoom.mechanics.entries) {
            if (mechanic.value is DungeonSecret) {
                val secret = mechanic.value as DungeonSecret
                if (!visited.contains(mechanic.key)) {
                    if (secret.getSecretStatus(dungeonRoom) != DungeonSecret.SecretStatus.FOUND) {
                        var cost = 0.0
                        if (secret.secretType == DungeonSecret.SecretType.BAT) {
                            if (secret.preRequisite.size == 0) {
                                cost -= 100000000.0
                            }
                        }
                        if (secret.getRepresentingPoint(dungeonRoom) != null) {
                            val blockpos = secret.getRepresentingPoint(dungeonRoom).getBlockPos(dungeonRoom)
                            cost += if (getAdvancedDistance) {
                                val f = DungeonsGuide.getDungeonsGuide().dungeonFacade
                                f.calculatePathLenght(pos, blockpos, dungeonRoom).toDouble()
                            } else {
                                blockpos.distanceSq(pos)
                            }
                            cost += (secret.preRequisite.size * 100).toDouble()
                        }
                        if (cost < lowestCost) {
                            lowestCost = cost
                            lowestWeightMechanic = mechanic
                        }
                    }
                }
            }
        }
        return if (lowestWeightMechanic == null) Optional.empty() else Optional.of(lowestWeightMechanic.key)
    }

    fun addAction(mechanic: String?, state: String?, actionRouteProperties: ActionRouteProperties?): String {
        val str = UUID.randomUUID().toString()
        addAction(str, mechanic, state, actionRouteProperties)
        return str
    }

    fun getPath(id: String): ActionRoute? {
        return actionPath[id]
    }

    fun addAction(id: String, mechanic: String?, state: String?, actionRouteProperties: ActionRouteProperties?) {
        actionPath[id] = ActionRoute(dungeonRoom, mechanic, state, actionRouteProperties)
    }


    fun onKeyPress(keyInputEvent: KeyBindPressedEvent) {
        if (AutoPathfindPage.keybind.keyBinds[0] == keyInputEvent.key) {
            if (AutoPathfindPage.autoBrowseToNext) {
                searchForNextTarget()
            }
            return
        }
        if (FeatureRegistry.SECRET_CREATE_REFRESH_LINE.keybind == keyInputEvent.key) {
            if (!FeatureRegistry.SECRET_CREATE_REFRESH_LINE.isEnabled) return
            val actionRoute = getBestFit(0f)
            val currentAction = actionRoute!!.currentAction
            if (currentAction == null) {
                logger.error("currentAction was null after SECRET_CREATE_REFRESH_LINE keypress")
                return
            }
            if (currentAction is ActionMove) {
                currentAction.forceRefresh(dungeonRoom)
            } else if (currentAction is ActionMoveNearestAir) {
                currentAction.forceRefresh(dungeonRoom)
            } else if (actionRoute.current >= 1) {
                val abstractAction = actionRoute.actions[actionRoute.current - 1]
                if (abstractAction is ActionMove) {
                    abstractAction.forceRefresh(dungeonRoom)
                }
                if (abstractAction is ActionMoveNearestAir) {
                    abstractAction.forceRefresh(dungeonRoom)
                }
            }
            if (FeatureRegistry.SECRET_CREATE_REFRESH_LINE.isPathfind && !actionRoute.actionRouteProperties.isPathfind) {
                actionRoute.actionRouteProperties.isPathfind = true
                actionRoute.actionRouteProperties.lineRefreshRate =
                    FeatureRegistry.SECRET_CREATE_REFRESH_LINE.refreshRate
            }
        }
    }

    fun onIteract(event: PlayerInteractEntityEvent?) {
        for (a in actionPath.values) {
            a.onLivingInteract(event)
        }
    }

    fun onIteractBlock(event: PlayerInteractEvent?) {
        for (a in actionPath.values) {
            a.onPlayerInteract(event)
        }
    }

    fun onEntityDeath(deathEvent: LivingDeathEvent?) {
        for (a in actionPath.values) {
            a.onLivingDeath(deathEvent)
        }
    }

    companion object {
        val logger = LogManager.getLogger("SecretGuider")
    }
}
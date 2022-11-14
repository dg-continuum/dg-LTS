package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.secretfinderstrategies

import kr.syeyoung.dungeonsguide.DungeonsGuide
import kr.syeyoung.dungeonsguide.chat.ChatTransmitter
import kr.syeyoung.dungeonsguide.dungeon.DungeonFacade
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionComplete
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRoute
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonSecret
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.GeneralRoomProcessor
import kr.syeyoung.dungeonsguide.features.FeatureRegistry
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig
import kr.syeyoung.dungeonsguide.oneconfig.secrets.AutoPathfindPage
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import kr.syeyoung.dungeonsguide.utils.SimpleTimer
import net.minecraft.client.Minecraft
import net.minecraft.util.BlockPos
import java.awt.Color
import java.util.*

class AutoFinderStrategy(parent: GeneralRoomProcessor) : SecretGuideStrategy(parent) {

    private val reevaluateOurWholeExisitance = SimpleTimer(20)
    override val actionPath: MutableMap<String, ActionRoute> = HashMap()


    override fun update() {
        if (reevaluateOurWholeExisitance.shouldRun()) {
            navigateToLowestScoreSecret()
        }
        reevaluateOurWholeExisitance.tick()

        val toRemove = HashSet<String>()
        for ((key, v) in actionPath) {
            v.onTick()
            if (v.currentAction is ActionComplete) {
                toRemove.add(key)
            }
        }

        for ((_, value) in parent.dungeonRoom.mechanics) {
            if (value is DungeonSecret) {
                value.tick(parent.dungeonRoom)
            }
        }
        if (toRemove.contains("AUTO-BROWSE") && AutoPathfindPage.autoBrowseToNext) {
            searchForNextTarget()
        }

    }

    class AutoGuideSecretCandidate(
        val mechanicName: String,
        var cost: Double,
        var pos: BlockPos,
        var descipriton: String,
        var isDone: Boolean
    )

    private val autoPathFindCandidates: MutableMap<String, AutoGuideSecretCandidate> = HashMap()

    override fun draw(partialTicks: Float) {
        if (DgOneCongifConfig.DEBUG_MODE) {
            for (autoPathFindCandidate in autoPathFindCandidates) {
                RenderUtils.highlightBlock(
                    autoPathFindCandidate.value.pos,
                    if (autoPathFindCandidate.value.isDone) Color.CYAN else Color.GRAY,
                    partialTicks
                )

                val x = autoPathFindCandidate.value.pos.x
                val y = autoPathFindCandidate.value.pos.y
                val z = autoPathFindCandidate.value.pos.z
                RenderUtils.drawTextAtWorld(
                    "Mechanic name: ${autoPathFindCandidate.value.mechanicName}",
                    x.toFloat(),
                    y.toFloat(),
                    z.toFloat(),
                    if(!autoPathFindCandidate.value.isDone) 0xFFFFFF else 0xcccccc,
                    0.1F,
                    false,
                    true,
                    partialTicks
                )

                RenderUtils.drawTextAtWorld(
                    if(!autoPathFindCandidate.value.isDone)"cost: ${autoPathFindCandidate.value.cost}" else "Done",
                    x.toFloat(),
                    y.toFloat() - 1F,
                    z.toFloat(),
                    if(!autoPathFindCandidate.value.isDone) 0xFFFFFF else 0xcccccc,
                    0.1F,
                    false,
                    true,
                    partialTicks
                )


            }


        }
    }

    override fun init() {
        searchForNextTarget()
    }

    var lastFoundMechanic: String = ""

    fun searchForNextTarget() {
        if (parent.dungeonRoom.currentState == DungeonRoom.RoomState.FINISHED) {
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

    private val visited: MutableSet<String> = HashSet()


    private val simplesMechanic: Optional<String>
        get() = calculateMechanicCosts(false)

    private fun calculateMechanicCosts(getAdvancedDistance: Boolean): Optional<String> {
        val pos = Minecraft.getMinecraft().thePlayer.position
        var lowestWeightMechanic: Map.Entry<String, DungeonMechanic>? = null
        var lowestCost = Float.MAX_VALUE.toDouble()
        for (mechanic in parent.dungeonRoom.mechanics.entries) {
            if (mechanic.value is DungeonSecret) {
                val secret = mechanic.value as DungeonSecret
                if (!visited.contains(mechanic.key)) {
                    if (secret.getSecretStatus(parent.dungeonRoom) != DungeonSecret.SecretStatus.FOUND) {
                        var cost = 0.0
                        if (secret.secretType == DungeonSecret.SecretType.BAT) {
                            if (secret.preRequisite.size == 0) {
                                cost -= 100000000.0
                            }
                        }
                        if (secret.getRepresentingPoint(parent.dungeonRoom) != null) {
                            val blockpos = secret.getRepresentingPoint(parent.dungeonRoom).getBlockPos(parent.dungeonRoom)
                            cost += if (getAdvancedDistance) {
                                DungeonsGuide.getDungeonsGuide().dungeonFacade.calculatePathLenght(
                                    pos,
                                    blockpos,
                                    parent.dungeonRoom
                                ).toDouble()
                            } else {
                                blockpos.distanceSq(pos)
                            }
                            cost += (secret.preRequisite.size * 100).toDouble()
                            autoPathFindCandidates[mechanic.key] =
                                AutoGuideSecretCandidate(
                                    mechanic.key,
                                    cost,
                                    blockpos,
                                    mechanic.toString(),
                                    false
                                )
                        }
                        if (cost < lowestCost) {
                            lowestCost = cost
                            lowestWeightMechanic = mechanic
                        }
                    }
                } else {
                    val cock: AutoGuideSecretCandidate? = autoPathFindCandidates[mechanic.key]
                    if(cock != null){
                        cock.isDone = true
                        autoPathFindCandidates[mechanic.key] = cock
                    }
                }
            }
        }
        return if (lowestWeightMechanic == null) Optional.empty() else Optional.of(lowestWeightMechanic.key)
    }


    fun navigateToLowestScoreSecret() {
        DungeonFacade.INSTANCE.ex.submit {
            val lowestWeightMechanic = if (DgOneCongifConfig.usePathfindCostCacls) {
                calculateMechanicCosts(true)
            } else {
                calculateMechanicCosts(false)
            }

            if (!lowestWeightMechanic.isPresent) return@submit
            val mechanic = lowestWeightMechanic.get()

            // make sure we don't start navigating to the same secret twice
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

    }

}
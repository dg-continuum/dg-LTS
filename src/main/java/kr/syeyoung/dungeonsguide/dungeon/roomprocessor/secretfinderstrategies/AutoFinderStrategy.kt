package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.secretfinderstrategies

import kr.syeyoung.dungeonsguide.DungeonsGuide
import kr.syeyoung.dungeonsguide.chat.ChatTransmitter
import kr.syeyoung.dungeonsguide.dungeon.DungeonFacade
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionComplete
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRoute
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonSecret
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
            updateCandidates()
        }
        reevaluateOurWholeExisitance.tick()


        navigateToLowestScoreSecret()

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
                    if (!autoPathFindCandidate.value.isDone) 0xFFFFFF else 0xcccccc,
                    0.1F,
                    false,
                    true,
                    partialTicks
                )

                RenderUtils.drawTextAtWorld(
                    if (!autoPathFindCandidate.value.isDone) "cost: ${autoPathFindCandidate.value.cost}" else "Done",
                    x.toFloat(),
                    y.toFloat() - 1F,
                    z.toFloat(),
                    if (!autoPathFindCandidate.value.isDone) 0xFFFFFF else 0xcccccc,
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
        get() = calculateMechanicCosts()

    private fun calculateMechanicCosts(): Optional<String> {
        val playerPos = Minecraft.getMinecraft().thePlayer.position
        var lowestWeightSecretName: String? = null
        var lowestCost = Float.MAX_VALUE.toDouble()
        for ((secretName, secretMechanic) in parent.dungeonRoom.mechanics) {
            if (secretMechanic is DungeonSecret) {
                if (!visited.contains(secretName)) {
                    if (secretMechanic.getSecretStatus(parent.dungeonRoom) != DungeonSecret.SecretStatus.FOUND) {
                        var cost = 0.0
                        if (secretMechanic.secretType == DungeonSecret.SecretType.BAT) {
                            cost -= 100_000_000
                        }
                        if (secretMechanic.preRequisite.size == 0) {
                            cost -= 100_000_000
                        } else {
                            cost += (secretMechanic.preRequisite.size * 100).toDouble()
                        }

                        val representingPoint = secretMechanic.getRepresentingPoint(parent.dungeonRoom)
                        if (representingPoint != null) {
                            val secretPos = representingPoint
                                .getBlockPos(parent.dungeonRoom)

                            cost += if (DgOneCongifConfig.usePathfindCostCacls) {
                                DungeonsGuide.getDungeonsGuide().dungeonFacade.calculatePathLenght(
                                    playerPos,
                                    secretPos,
                                    parent.dungeonRoom
                                ).toDouble()
                            } else {
                                secretPos.distanceSq(playerPos)
                            }

                            autoPathFindCandidates[secretName] =
                                AutoGuideSecretCandidate(
                                    secretName,
                                    cost,
                                    secretPos,
                                    secretMechanic.toString(),
                                    false
                                )
                        }
                        if (cost < lowestCost) {
                            lowestCost = cost
                            lowestWeightSecretName = secretName
                        }
                    }
                } else {
                    val cock: AutoGuideSecretCandidate? = autoPathFindCandidates[secretName]
                    if (cock != null) {
                        cock.isDone = true
                        autoPathFindCandidates[secretName] = cock
                    }
                }
            }
        }
        return if (lowestWeightSecretName == null) Optional.empty() else Optional.of(lowestWeightSecretName)
    }


    fun updateCandidates() {
        DungeonFacade.INSTANCE.ex.submit {
            calculateMechanicCosts()
        }
    }

    var navigateBiasLastUpdate: Long = -1L

    fun navigateToLowestScoreSecret() {
        var lowestWeightMechanic = ""

        var lowstCost: Double = Double.MAX_VALUE
        for ((key, value) in autoPathFindCandidates) {
            if (value.cost < lowstCost) {
                lowestWeightMechanic = key
                lowstCost = value.cost
            }
        }

        if (lowestWeightMechanic == "") {
            return
        }


        // make sure we don't start navigating to the same secret twice
        if (lowestWeightMechanic == lastFoundMechanic) {
            return
        }

        val now = System.currentTimeMillis()

        // make sure we don't flip-flop between secrets instantly
        // two seconds from update is smaller than now. aka 2 seconds passed since finding better secret
        if (navigateBiasLastUpdate != -1L && ((navigateBiasLastUpdate + 1000) < now)) {
            return
        }

        ChatTransmitter.addToQueue("@sFound better solution")

        // remove the last
        actionPath.remove("AUTO-BROWSE")

        // start navigating to the new one
        lastFoundMechanic = lowestWeightMechanic
        navigateBiasLastUpdate = now
        addAction(
            "AUTO-BROWSE",
            lowestWeightMechanic,
            "found",
            FeatureRegistry.SECRET_LINE_PROPERTIES_AUTOPATHFIND.routeProperties
        )
    }


}
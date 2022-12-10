package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.secretfinderstrategies

import kr.syeyoung.dungeonsguide.DungeonsGuide
import kr.syeyoung.dungeonsguide.chat.ChatTransmitter
import kr.syeyoung.dungeonsguide.dungeon.DungeonFacade
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionState
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionComplete
import kr.syeyoung.dungeonsguide.dungeon.mechanics.impl.DungeonSecret
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.GeneralRoomProcessor
import kr.syeyoung.dungeonsguide.features.FeatureRegistry
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig
import kr.syeyoung.dungeonsguide.oneconfig.secrets.AutoPathfindPage
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import kr.syeyoung.dungeonsguide.utils.VectorUtils
import kr.syeyoung.dungeonsguide.utils.simple.SimpleTimer
import net.minecraft.client.Minecraft
import org.joml.Vector3i
import java.awt.Color

class AutoFinderStrategy(parent: GeneralRoomProcessor) : SecretGuideStrategy(parent) {

    private val autoPathFindCandidates: MutableMap<MechanicId, AutoGuideSecretCandidate> = HashMap()

    data class MechanicId(val id: String)

    data class AutoGuideSecretCandidate(
        val mechanic: DungeonSecret,
        var cost: Double,
        var pos: Vector3i,
        var descipriton: String,
        var isDone: Boolean
    )

    private val updateTimer = SimpleTimer(20)

    override fun update() {

        if (parent.dungeonRoom.currentState == DungeonRoom.RoomState.FINISHED) {
            cancelAll()
            return
        }

        if (updateTimer.shouldRun()) {
            updateCandidates()

            navigateToLowestScoreSecret()
        }
        updateTimer.tick()


        // tick mechanics
        for ((_, value) in parent.dungeonRoom.mechanics) {
            if (value is DungeonSecret) {
                value.tick(parent.dungeonRoom)
            }
        }

        // tick actions and remove finished actions
        for ((_, route) in actionPath) {
            val id = MechanicId(route.mechanic)
            route.onTick()


            if ((route.currentAction is ActionComplete) or (route.currentAction.isComplete(parent.dungeonRoom))) {
                autoPathFindCandidates[id]?.let {
                    it.isDone = true
                }
            }
        }


        autoPathFindCandidates[lastFoundMechanic]?.let {
            if (it.isDone && AutoPathfindPage.autoBrowseToNext) {
                searchForNextTarget()
            }
        }


    }

    override fun draw(partialTicks: Float) {
        if (DgOneCongifConfig.debugMode) {
            for ((id, value) in autoPathFindCandidates) {
                RenderUtils.highlightBlock(
                    value.pos,
                    if (value.isDone) Color.CYAN else Color.GRAY,
                    partialTicks
                )

                val x = value.pos.x
                val y = value.pos.y
                val z = value.pos.z
                RenderUtils.drawTextAtWorld(
                    "Mechanic name: ${id.id}",
                    x.toFloat(),
                    y.toFloat(),
                    z.toFloat(),
                    if (!value.isDone) 0xFFFFFF else 0xcccccc,
                    0.1F,
                    false,
                    true,
                    partialTicks
                )

                RenderUtils.drawTextAtWorld(
                    if (!value.isDone) "cost: ${value.cost}" else "Done",
                    x.toFloat(),
                    y.toFloat() - 1F,
                    z.toFloat(),
                    if (!value.isDone) 0xFFFFFF else 0xcccccc,
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

    var lastFoundMechanic: MechanicId? = null

    fun searchForNextTarget() {
        calculateMechanicCosts()
        getCheapestMechanic()?.let {
            if (lastFoundMechanic == it) {
                return
            } else {
                lastFoundMechanic = it
                addAction(
                    "AUTO-BROWSE",
                    it.id,
                    ActionState.found,
                    FeatureRegistry.SECRET_LINE_PROPERTIES_AUTOPATHFIND.routeProperties
                )
            }
        }

    }


    private fun calculateMechanicCosts() {
        // get all mechanics from parent if they already are not candidates
        for ((secretName, secretMechanic) in parent.dungeonRoom.mechanics) {
            val mechId = MechanicId(secretName)
            if (secretMechanic is DungeonSecret) {
                if (!autoPathFindCandidates.contains(mechId)) {
                    val representingPoint = secretMechanic.getRepresentingPoint(parent.dungeonRoom)
                    if (representingPoint != null) {
                        val secretPos = representingPoint
                            .getVector3i(parent.dungeonRoom)

                        autoPathFindCandidates[mechId] =
                            AutoGuideSecretCandidate(
                                secretMechanic,
                                0.0,
                                secretPos,
                                secretMechanic.toString(),
                                false
                            )
                    }

                }
            }
        }


        for ((key, value) in actionPath) {
            val id = MechanicId(key)
            autoPathFindCandidates[id]?.let {
                if(value.currentAction.isComplete(parent.dungeonRoom)){
                    it.isDone = true
                }
            }
        }



        val playerPos = VectorUtils.BlockPosToVec3i(Minecraft.getMinecraft().thePlayer.position)

        // re calculate cost for each candidate
        for ((key, value) in autoPathFindCandidates) {
            if (value.isDone) continue

            value.cost = 0.0

            if (value.mechanic.secretType == DungeonSecret.SecretType.BAT) {
                value.cost -= 100_000_000
            }
            if (value.mechanic.preRequisite.size == 0) {
                value.cost -= 100_000_000
            } else {
                value.cost += (value.mechanic.preRequisite.size * 100).toDouble()
            }

            val representingPoint = value.mechanic.getRepresentingPoint(parent.dungeonRoom)
            if (representingPoint != null) {
                val secretPos = representingPoint
                    .getVector3i(parent.dungeonRoom)

                value.cost += if (DgOneCongifConfig.usePathfindCostCacls) {
                    DungeonsGuide.getDungeonsGuide().dungeonFacade.calculatePathLenght(
                        playerPos,
                        secretPos,
                        parent.dungeonRoom
                    ).toDouble()
                } else {
                    secretPos.distance(playerPos)
                }

                autoPathFindCandidates[key] = value
            }
        }

    }


    fun updateCandidates() {
        DungeonFacade.INSTANCE.ex.submit {
            calculateMechanicCosts()
        }
    }

    var navigateBiasLastUpdate: Long = -1L


    fun getCheapestMechanic(): MechanicId? {
        var lowestWeightMechanic: MechanicId? = null
        var lowstCost: Double = Double.MAX_VALUE
        for ((key, value) in autoPathFindCandidates) {
            if (value.cost < lowstCost && !value.isDone) {
                lowestWeightMechanic = key
                lowstCost = value.cost
            }
        }
        return lowestWeightMechanic
    }

    fun navigateToLowestScoreSecret() {

        val lowestWeightMechanic = getCheapestMechanic() ?: return


        // make sure we don't start navigating to the same secret twice
        if (lowestWeightMechanic == lastFoundMechanic) {
            return
        }

        val now = System.currentTimeMillis()

        // make sure we don't flip-flop between secrets instantly
        // two seconds from update is smaller than now. aka 2 seconds passed since finding better secret
        if (navigateBiasLastUpdate != -1L && ((navigateBiasLastUpdate + 500) > now)) {
            return
        }

        ChatTransmitter.addToQueue("@sFound better solution")

        // remove the last
        actionPath.remove("AUTO-BROWSE")

        lastFoundMechanic = lowestWeightMechanic
        navigateBiasLastUpdate = now

        // start navigating to the new one
        addAction(
            "AUTO-BROWSE",
            lowestWeightMechanic.id,
            ActionState.found,
            FeatureRegistry.SECRET_LINE_PROPERTIES_AUTOPATHFIND.routeProperties
        )
    }


}
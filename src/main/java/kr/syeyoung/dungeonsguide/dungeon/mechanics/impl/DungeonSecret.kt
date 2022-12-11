package kr.syeyoung.dungeonsguide.dungeon.mechanics.impl

import kr.syeyoung.dungeonsguide.dungeon.DungeonFacade
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.*
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic
import kr.syeyoung.dungeonsguide.dungeon.mechanics.MechanicType
import kr.syeyoung.dungeonsguide.utils.BlockCache
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import kr.syeyoung.dungeonsguide.utils.VectorUtils
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.passive.EntityBat
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntityChest
import org.joml.Vector3d
import org.joml.Vector3i
import java.awt.Color
import java.util.function.Predicate

class DungeonSecret : DungeonMechanic(), Cloneable {
    override val mechType: MechanicType = MechanicType.Secret

    var secretPoint = OffsetPoint(0, 0, 0)
    var secretType = SecretType.CHEST
    var preRequisite: List<String> = ArrayList()

    public override fun clone(): Any {
        return DungeonSecret().also {
            it.secretPoint = secretPoint
            it.secretType = secretType
            it.preRequisite = preRequisite
        }
    }

    fun tick(dungeonRoom: DungeonRoom) {
        when (secretType) {
            SecretType.CHEST -> {
                val pos = secretPoint.getBlockPos(dungeonRoom)
                val blockState = BlockCache.getBlockState(pos)
                if (blockState.block == Blocks.chest || blockState.block == Blocks.trapped_chest) {
                    val chest = Minecraft.getMinecraft().theWorld.getTileEntity(pos) as TileEntityChest
                    if (chest.numPlayersUsing > 0) {
                        dungeonRoom.discoveredChests[VectorUtils.BlockPosToVec3i(pos)] = 2
                    } else {
                        dungeonRoom.discoveredChests[VectorUtils.BlockPosToVec3i(pos)] = 1
                    }
                }
            }

            SecretType.ESSENCE -> {
                val pos: Vector3i = secretPoint.getVector3i(dungeonRoom)
                if (BlockCache.getBlock(pos) == Blocks.skull) {
                    dungeonRoom.discoveredEssence[pos] = true
                }
            }

            SecretType.ITEM_DROP -> {
                val secretPos = secretPoint.getVector3d(dungeonRoom)
                val playerPos = VectorUtils.getPlayerVector3d()
                if (playerPos.distanceSquared(secretPos) < 16) {
                    val vec3 = secretPos.sub(playerPos).normalize()
                    var i = 0
                    while (i < playerPos.distance(secretPos)) {
                        val vec = playerPos.add(vec3.x * i, vec3.y * i, vec3.z * i)
                        val blockState = BlockCache.getBlockState(vec)
                        if (!DungeonRoom.isValidBlock(blockState)) return
                        i++
                    }
                    dungeonRoom.discoveredItemDrops[secretPos] = true
                }
            }

            SecretType.BAT -> {
                // the compiler yelled at me,
                // so here it is
                // a useless branch cuz "when branches need to be exhaustive"
            }
        }
    }

    fun getSecretStatus(dungeonRoom: DungeonRoom): SecretStatus {
        val pos: Vector3i = secretPoint.getVector3i(dungeonRoom)
        val secretBlock = BlockCache.getBlock(pos)

        return when (secretType) {
            SecretType.ESSENCE -> {
                if (secretBlock == Blocks.skull) {
                    dungeonRoom.discoveredEssence[pos] = true
                    SecretStatus.DEFINITELY_NOT
                } else {
                    if (dungeonRoom.discoveredEssence.containsKey(pos)) {
                        SecretStatus.FOUND
                    } else {
                        SecretStatus.NOT_SURE
                    }
                }
            }

            SecretType.BAT -> {
                val context = DungeonFacade.context ?: return SecretStatus.NOT_SURE

                context.killedBats.forEach { killedBat ->
                    context.batSpawnedLocations[killedBat]?.let { killedBatSpawn ->
                        if (killedBatSpawn.distanceSquared(pos) < 100) {
                            return SecretStatus.FOUND
                        }
                    }
                }

                SecretStatus.NOT_SURE
            }

            SecretType.CHEST -> {
                if (dungeonRoom.discoveredChests.containsKey(pos)) {
                    return if (dungeonRoom.discoveredChests[pos] as Int == 2 || secretBlock == Blocks.air) {
                        SecretStatus.FOUND
                    } else {
                        SecretStatus.CREATED
                    }
                }
                when {
                    secretBlock == Blocks.air -> {
                        SecretStatus.DEFINITELY_NOT
                    }

                    secretBlock != Blocks.chest && secretBlock != Blocks.trapped_chest -> {
                        SecretStatus.ERROR
                    }

                    else -> {
                        val chest =
                            Minecraft.getMinecraft().theWorld.getTileEntity(VectorUtils.Vec3iToBlockPos(pos)) as TileEntityChest
                        if (chest.numPlayersUsing > 0) {
                            SecretStatus.FOUND
                        } else {
                            SecretStatus.CREATED
                        }
                    }
                }
            }

            SecretType.ITEM_DROP -> {
                if (dungeonRoom.discoveredItemDrops.containsKey(Vector3d(pos))) {
                    return SecretStatus.FOUND
                }
                val secretPos = secretPoint.getVector3d(dungeonRoom)
                val playerPos = VectorUtils.getPlayerVector3d()
                if (playerPos.distance(secretPos) < 16) {
                    secretPos.sub(playerPos).normalize()
                    var i = 0
                    while (i < playerPos.distance(secretPos)) {
                        playerPos.add(secretPos.x * i, secretPos.y * i, secretPos.z * i)
                        val blockState = BlockCache.getBlockState(playerPos)
                        if (!DungeonRoom.isValidBlock(blockState)) return SecretStatus.NOT_SURE
                        i++
                    }
                    dungeonRoom.discoveredItemDrops[secretPos] = true
                }
                SecretStatus.NOT_SURE
            }
        }
    }

    override fun getAction(state: String, dungeonRoom: DungeonRoom): Set<AbstractAction> {
        return HashSet<AbstractAction>().also { base ->
            when (state.lowercase()) {
                "navigate" -> {
                    base.add(ActionMoveNearestAir(getRepresentingPoint(dungeonRoom)))
                }

                "found" -> {
                    if (getSecretStatus(dungeonRoom) == SecretStatus.FOUND) {
                        return emptySet()
                    }


                    base.add(ActionMove(secretPoint))

                    when (secretType) {
                        SecretType.CHEST, SecretType.ESSENCE -> {
                            base.add(ActionClick(secretPoint))
                        }

                        SecretType.BAT -> {
                            base.add(ActionKill(secretPoint).apply {
                                predicate = Predicate { obj: Entity? -> obj is EntityBat }
                                radius = 10
                            })
                        }

                        SecretType.ITEM_DROP -> {
                            // nothing since we be near it to pick it up
                            // not like we need to click it or something
                        }
                    }

                }

                else -> throw IllegalArgumentException("$state is not valid state for secret")
            }
            preRequisite.forEach { str ->
                Companion.disassemblePreRequisite(str)?.let { (name, state) ->
                    base.add(ActionChangeState(name, state))
                }
            }
        }
    }

    override fun highlight(color: Color, name: String, dungeonRoom: DungeonRoom, partialTicks: Float) {
        val pos = secretPoint.getVector3i(dungeonRoom)
        RenderUtils.highlightBlock(pos, color, partialTicks)
        RenderUtils.drawTextAtWorld(
            secretType.name, pos.x + 0.5f, pos.x + 0.75f, pos.z + 0.5f, -0x1, 0.03f, false, true, partialTicks
        )
        RenderUtils.drawTextAtWorld(
            name, pos.x + 0.5f, pos.x + 0.375f, pos.z + 0.5f, -0x1, 0.03f, false, true, partialTicks
        )
        RenderUtils.drawTextAtWorld(
            getCurrentState(dungeonRoom), pos.x + 0.5f, pos.x + 0f, pos.z + 0.5f, -0x1, 0.03f, false, true, partialTicks
        )
    }

    override fun getCurrentState(dungeonRoom: DungeonRoom): String {
        return getSecretStatus(dungeonRoom).name
    }

    override fun getPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        return if (getSecretStatus(dungeonRoom) == SecretStatus.FOUND) {
            hashSetOf(
                "navigate"
            )
        } else {
            hashSetOf("found", "navigate")
        }
    }

    override fun getTotalPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        return hashSetOf("found")
    }

    override fun getRepresentingPoint(dungeonRoom: DungeonRoom): OffsetPoint {
        return secretPoint
    }

    enum class SecretType {
        BAT, CHEST, ITEM_DROP, ESSENCE
    }

    enum class SecretStatus {
        DEFINITELY_NOT, NOT_SURE, CREATED, FOUND, ERROR
    }

}
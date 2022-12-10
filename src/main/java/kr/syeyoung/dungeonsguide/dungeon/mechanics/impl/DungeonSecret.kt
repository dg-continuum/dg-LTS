package kr.syeyoung.dungeonsguide.dungeon.mechanics.impl

import com.google.common.collect.Sets
import kr.syeyoung.dungeonsguide.DungeonsGuide
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionState
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.*
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic
import kr.syeyoung.dungeonsguide.dungeon.mechanics.MechanicType
import kr.syeyoung.dungeonsguide.utils.BlockCache
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import kr.syeyoung.dungeonsguide.utils.VectorUtils
import net.minecraft.block.Block
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
        return super.clone()
    }

    fun tick(dungeonRoom: DungeonRoom) {
        if (secretType == SecretType.CHEST) {
            val pos = secretPoint.getBlockPos(dungeonRoom)
            val blockState = BlockCache.getBlockState(pos)
            if (blockState.block === Blocks.chest || blockState.block === Blocks.trapped_chest) {
                val chest = dungeonRoom.context.world.getTileEntity(pos) as TileEntityChest
                if (chest.numPlayersUsing > 0) {
                    dungeonRoom.roomContext["c-$pos"] = 2
                } else {
                    dungeonRoom.roomContext["c-$pos"] = 1
                }
            }
        } else if (secretType == SecretType.ESSENCE) {
            val pos: Vector3i = secretPoint.getVector3i(dungeonRoom)
            if (BlockCache.getBlock(pos) === Blocks.skull) {
                dungeonRoom.roomContext["e-$pos"] = true
            }
        } else if (secretType == SecretType.ITEM_DROP) {
            val pos = Vector3d(secretPoint.getVector3i(dungeonRoom))
            val player = VectorUtils.getPlayerVector3d()
            if (player.distanceSquared(pos) < 16) {
                val vec3 = pos.sub(player).normalize()
                var i = 0
                while (i < player.distance(pos)) {
                    val vec = player.add(vec3.x * i, vec3.y * i, vec3.z * i)
                    val blockState = BlockCache.getBlockState(vec)
                    if (!DungeonRoom.isValidBlock(blockState)) return
                    i++
                }
                dungeonRoom.roomContext["i-$pos"] = true
            }
        }
    }

    fun getSecretStatus(dungeonRoom: DungeonRoom): SecretStatus {
        val pos: Vector3i = secretPoint.getVector3i(dungeonRoom)
        val block: Block = BlockCache.getBlock(pos)
        return if (secretType == SecretType.CHEST) {
            if (dungeonRoom.roomContext.containsKey("c-$pos")) return if (dungeonRoom.roomContext["c-$pos"] as Int == 2 || block === Blocks.air) SecretStatus.FOUND else SecretStatus.CREATED
            if (block === Blocks.air) {
                SecretStatus.DEFINITELY_NOT
            } else if (block !== Blocks.chest && block !== Blocks.trapped_chest) {
                SecretStatus.ERROR
            } else {
                val chest = dungeonRoom.context.world.getTileEntity(VectorUtils.Vec3iToBlockPos(pos)) as TileEntityChest
                if (chest.numPlayersUsing > 0) {
                    SecretStatus.FOUND
                } else {
                    SecretStatus.CREATED
                }
            }
        } else if (secretType == SecretType.ESSENCE) {
            if (block === Blocks.skull) {
                dungeonRoom.roomContext["e-$pos"] = true
                SecretStatus.DEFINITELY_NOT
            } else {
                if (dungeonRoom.roomContext.containsKey("e-$pos")) SecretStatus.FOUND else SecretStatus.NOT_SURE
            }
        } else if (secretType == SecretType.BAT) {
            val context = DungeonsGuide.getDungeonsGuide().dungeonFacade.context
            for (killed in context.killedBats) {
                if (context.batSpawnedLocations[killed] == null) continue
                if (context.batSpawnedLocations[killed]!!.distanceSquared(pos) < 100) {
                    return SecretStatus.FOUND
                }
            }
            SecretStatus.NOT_SURE
        } else {
            if (dungeonRoom.roomContext.containsKey("i-$pos")) return SecretStatus.FOUND
            val secret = secretPoint.getVector3d(dungeonRoom)
            val player = VectorUtils.getPlayerVector3d()
            if (player.distance(secret) < 16) {
                secret.sub(player).normalize()
                var i = 0
                while (i < player.distance(secret)) {
                    player.add(secret.x * i, secret.y * i, secret.z * i)
                    val blockState = BlockCache.getBlockState(player)
                    if (!DungeonRoom.isValidBlock(blockState)) return SecretStatus.NOT_SURE
                    i++
                }
                dungeonRoom.roomContext["i-$secret"] = true
            }
            SecretStatus.NOT_SURE
        }
    }

    override fun getAction(state: String, dungeonRoom: DungeonRoom): Set<AbstractAction> {
        if (state.equals("navigate", ignoreCase = true)) {
            val base: Set<AbstractAction>
            base = HashSet()
            var preRequisites = base
            val actionMove = ActionMoveNearestAir(getRepresentingPoint(dungeonRoom))
            preRequisites.add(actionMove)
            preRequisites = actionMove.getPreRequisites(null)
            for (str in preRequisite) {
                if (str.isNotEmpty()) {
                    val toTypedArray = str.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val actionChangeState = ActionChangeState(toTypedArray[0], ActionState.valueOf(toTypedArray[1]))

                    preRequisites.add(actionChangeState)
                }
            }
            return base
        }
        require("found".equals(state, ignoreCase = true)) { "$state is not valid state for secret" }
        if (state == "found" && getSecretStatus(dungeonRoom) == SecretStatus.FOUND) return HashSet()
        val base: MutableSet<AbstractAction>
        base = HashSet()
        var preRequisites = base
        if (secretType == SecretType.CHEST || secretType == SecretType.ESSENCE) {
            val actionClick = ActionClick(secretPoint)
            preRequisites.add(actionClick)
            preRequisites = actionClick.getPreRequisites(dungeonRoom)
        } else if (secretType == SecretType.BAT) {
            val actionKill = ActionKill(secretPoint)
            preRequisites.add(actionKill)
            actionKill.predicate = Predicate { obj: Entity? -> EntityBat::class.java.isInstance(obj) }
            actionKill.radius = 10
            preRequisites = actionKill.getPreRequisites(null)
        }
        val actionMove = ActionMove(secretPoint)
        preRequisites.add(actionMove)
        preRequisites = actionMove.getPreRequisites(null)
        for (str in preRequisite) {
            if (str.isNotEmpty()) {
                val toTypedArray = str.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val actionChangeState = ActionChangeState(toTypedArray[0], ActionState.valueOf(toTypedArray[1]))
                preRequisites.add(actionChangeState)
            }
        }
        return base
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
        val status = getSecretStatus(dungeonRoom)
        return if (status == SecretStatus.FOUND) Sets.newHashSet(
            "navigate"
        ) else Sets.newHashSet("found", "navigate")
    }

    override fun getTotalPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        return Sets.newHashSet("found" /*, "definitely_not", "not_sure", "created", "error"*/)
    }

    override fun getRepresentingPoint(dungeonRoom: DungeonRoom): OffsetPoint {
        return secretPoint
    }

    enum class SecretType {
        BAT, CHEST, ITEM_DROP, ESSENCE
    }

    enum class SecretStatus(stateName: String) {
        DEFINITELY_NOT("definitely_not"), NOT_SURE("not_sure"), CREATED("created"), FOUND("found"), ERROR("error");
    }

    companion object {
        private const val serialVersionUID = 8784808599222706537L
    }
}
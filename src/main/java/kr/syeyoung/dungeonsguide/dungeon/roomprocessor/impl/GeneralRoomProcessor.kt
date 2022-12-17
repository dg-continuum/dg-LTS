package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl

import kr.syeyoung.dungeonsguide.chat.ChatTransmitter
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionPlan
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionMove
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.dungeon.mechanics.impl.DungeonSecret
import kr.syeyoung.dungeonsguide.dungeon.roomedit.EditingContext
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonAddSet
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonRoomEdit
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.RoomProcessor
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.secretfinderstrategies.AutoFinderStrategy
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.secretfinderstrategies.SecretGuideStrategy
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.secretfinderstrategies.buildSecretStrategy
import kr.syeyoung.dungeonsguide.events.impl.BlockUpdateEvent
import kr.syeyoung.dungeonsguide.events.impl.KeyBindPressedEvent
import kr.syeyoung.dungeonsguide.events.impl.PlayerInteractEntityEvent
import kr.syeyoung.dungeonsguide.features.FeatureRegistry
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig
import kr.syeyoung.dungeonsguide.oneconfig.secrets.AutoPathfindPage
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus
import kr.syeyoung.dungeonsguide.utils.VectorUtils
import kr.syeyoung.dungeonsguide.utils.simple.SimpleFuse
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.passive.EntityBat
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.util.BlockPos
import net.minecraft.util.IChatComponent
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector3d
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14
import java.awt.Color
import java.util.*

open class GeneralRoomProcessor(val dungeonRoom: DungeonRoom) : RoomProcessor {
    private var stackSize = 0
    private var numSecrets = 0
    private var isLast = false
    private var previousChest: BlockPos? = null

    val context: DungeonContext = dungeonRoom.context

    var strategy: SecretGuideStrategy = buildSecretStrategy(DgOneCongifConfig.secretFindMode, dungeonRoom)

    private val tickedFuse = SimpleFuse()
    override fun tick() {
        if (tickedFuse.checkAndBlow()) {
            logger.info("Creating Pathfinding lines")
            strategy.init()
        }
        strategy.update()
    }

    override fun drawScreen(partialTicks: Float) {

        strategy.actionPath.values.forEach {
            it.onRenderScreen(partialTicks)
        }

        if (DgOneCongifConfig.debugRoomEdit && DgOneCongifConfig.debugMode) {
            Minecraft.getMinecraft().objectMouseOver ?: return

            val en = Minecraft.getMinecraft().objectMouseOver.entityHit ?: return
            val sr = ScaledResolution(Minecraft.getMinecraft())

            if (context.batSpawnedLocations.containsKey(en.entityId)) {
                GlStateManager.enableBlend()
                GL14.glBlendFuncSeparate(
                    GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA
                )
                GlStateManager.tryBlendFuncSeparate(
                    GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA
                )
                Minecraft.getMinecraft().fontRendererObj.drawString(
                    "Spawned at " + context.batSpawnedLocations[en.entityId],
                    sr.scaledWidth / 2,
                    sr.scaledHeight / 2,
                    -0x1
                )
            }
        }
    }

    override fun drawWorld(partialTicks: Float) {
        strategy.draw(partialTicks)
        val finalSmallest = findClosestActionRoute(partialTicks)
        strategy.actionPath.forEach { (_, actionRoute) ->
            actionRoute.onRenderWorld(partialTicks, finalSmallest == actionRoute)
        }
        if (DgOneCongifConfig.debugMode &&
            EditingContext.getEditingContext() != null &&
            EditingContext.getEditingContext().current is GuiDungeonRoomEdit) {
            dungeonRoom.mechanics.forEach { (key, dungeonMechanic) ->
                dungeonMechanic?.highlight(Color(0, 255, 255, 50), key, dungeonRoom, partialTicks)
            }
        }
    }

    override fun chatReceived(chat: IChatComponent) {
        if (previousChest != null) {
            if (chat.formattedText == "§r§cThis chest has already been searched!§r") {
                dungeonRoom.discoveredChests[VectorUtils.BlockPosToVec3i(previousChest!!)] = 2
                previousChest = null
            }
        }
    }

    override fun actionbarReceived(chat: IChatComponent) {
        if (!SkyblockStatus.isOnDungeon()) return
        if (dungeonRoom.totalSecrets == -1) {
            ChatTransmitter.sendDebugChat(chat.formattedText.replace('§', '&') + " - received")
        }
        if (!chat.formattedText.contains("/")) return
        val pos = Minecraft.getMinecraft().thePlayer.position
        val pt1 = context.mapProcessor.worldPointToRoomPoint(VectorUtils.BlockPosToVec3i(pos.add(2, 0, 2)))
        val pt2 = context.mapProcessor.worldPointToRoomPoint(VectorUtils.BlockPosToVec3i(pos.add(-2, 0, -2)))
        if (pt1 != pt2) {
            stackSize = 0
            numSecrets = -1
            return
        }
        val text = chat.formattedText
        val secretsIndex = text.indexOf("Secrets")
        var secrets = 0
        if (secretsIndex != -1) {
            var theindex = 0
            for (i in secretsIndex downTo 0) {
                if (text.startsWith("§7", i)) {
                    theindex = i
                }
            }
            val it = text.substring(theindex + 2, secretsIndex - 1)
            secrets = it.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].toInt()
        }
        if (numSecrets == secrets) {
            stackSize++
        } else {
            stackSize = 0
            numSecrets = secrets
        }
        if (stackSize == 4 && dungeonRoom.totalSecrets != secrets) {
            dungeonRoom.totalSecrets = secrets
        }
    }

    override fun readGlobalChat(): Boolean {
        return false
    }

    override fun onPostGuiRender(event: DrawScreenEvent.Post) {}
    override fun onEntityUpdate(updateEvent: LivingUpdateEvent) {
        if (updateEvent.entityLiving is EntityArmorStand && updateEvent.entityLiving.name != null && updateEvent.entityLiving.name.contains(
                "Mimic"
            ) && !dungeonRoom.context.isGotMimic
        ) {
            dungeonRoom.context.isGotMimic = true
        }
    }

    override fun onKeybindPress(keyInputEvent: KeyBindPressedEvent) {
        if (AutoPathfindPage.keybind.keyBinds[0] == keyInputEvent.key) {
            if (AutoPathfindPage.autoBrowseToNext && strategy is AutoFinderStrategy) {
                (strategy as AutoFinderStrategy).searchForNextTarget()
            }
            return
        }
        if (FeatureRegistry.SECRET_CREATE_REFRESH_LINE.keybind == keyInputEvent.key) {
            if (!FeatureRegistry.SECRET_CREATE_REFRESH_LINE.isEnabled) return
            val actionRoute = findClosestActionRoute(0f)
            if (actionRoute == null) {
                logger.error("actionRoute was null after SECRET_CREATE_REFRESH_LINE keypress")
                return
            }

            val currentAction = actionRoute.currentAction

            if (currentAction is ActionMove) {
                currentAction.forceRefresh(dungeonRoom)
                return
            }
            if (actionRoute.current >= 1) {

                actionRoute.actions[actionRoute.current - 1].let {
                    if (it is ActionMove) {
                        it.forceRefresh(dungeonRoom)
                    }
                }

            }
            if (FeatureRegistry.SECRET_CREATE_REFRESH_LINE.isPathfind && !actionRoute.actionPlanProperties.isPathfind) {
                actionRoute.actionPlanProperties.isPathfind = true
                actionRoute.actionPlanProperties.lineRefreshRate =
                    FeatureRegistry.SECRET_CREATE_REFRESH_LINE.refreshRate
            }
        }
    }

    override fun onInteract(event: PlayerInteractEntityEvent) {
        strategy.actionPath.values.forEach {
            it.onLivingInteract(event)
        }
    }

    override fun onInteractBlock(event: PlayerInteractEvent) {
        strategy.actionPath.values.forEach {
            it.onPlayerInteract(event)
        }

        event.pos?.let {
            val block = event.world.getBlockState(it).block
            if (block == Blocks.chest || block == Blocks.trapped_chest) {
                previousChest = it
            }
        }

        if (event.entityPlayer.heldItem != null && event.entityPlayer.heldItem.item == Items.stick && DgOneCongifConfig.debugRoomEdit && DgOneCongifConfig.debugMode) {
            val ec = EditingContext.getEditingContext() ?: return
            if (ec.current !is GuiDungeonAddSet) return
            val gdas = ec.current as GuiDungeonAddSet
            if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                if (isLast) gdas.end.setPosInWorld(
                    dungeonRoom, VectorUtils.BlockPosToVec3i(event.pos)
                ) else gdas.start.setPosInWorld(
                    dungeonRoom, VectorUtils.BlockPosToVec3i(event.pos)
                )
                isLast = !isLast
            }
        }
    }

    private fun findClosestActionRoute(partialTicks: Float): ActionPlan? {
        var closest: ActionPlan? = null
        var closestDistance = 0.002
        strategy.actionPath.forEach { (_, actionRoute) ->
            val currentAction = actionRoute.currentAction

            if (currentAction is ActionMove) {
                val renderViewEntity = Minecraft.getMinecraft().renderViewEntity
                val distance = VectorUtils.distSquared(
                    VectorUtils.vec3ToVec3d(renderViewEntity.getLook(partialTicks)),
                    VectorUtils.vec3ToVec3d(renderViewEntity.getPositionEyes(partialTicks)),
                    Vector3d(currentAction.target.getVector3i(dungeonRoom)).add(0.5, 0.5, 0.5)
                )
                if (distance < closestDistance) {
                    closest = actionRoute
                    closestDistance = distance
                }
            }
        }
        return closest
    }

    override fun onEntityDeath(deathEvent: LivingDeathEvent) {

        strategy.actionPath.values.forEach {
            it.onLivingDeath(deathEvent)
        }


        EditingContext.getEditingContext()?.let { editingContext ->
            if (editingContext.room === dungeonRoom && deathEvent.entity is EntityBat) {
                for (screen in editingContext.guiStack) {
                    if (screen is GuiDungeonRoomEdit) {
                        context.batSpawnedLocations[deathEvent.entity.entityId]?.let {
                            val secret = DungeonSecret()
                            secret.secretType = DungeonSecret.SecretType.BAT
                            secret.secretPoint = OffsetPoint(
                                dungeonRoom, it
                            )
                            screen.sep.createNewMechanic(
                                "BAT-" + UUID.randomUUID(), secret
                            )
                        }
                        return
                    }
                }
                if (editingContext.current is GuiDungeonRoomEdit) {
                    context.batSpawnedLocations[deathEvent.entity.entityId]?.let {
                        val secret = DungeonSecret()
                        secret.secretType = DungeonSecret.SecretType.BAT
                        secret.secretPoint = OffsetPoint(
                            dungeonRoom, it
                        )
                        (editingContext.current as GuiDungeonRoomEdit).sep.createNewMechanic(
                            "BAT-" + UUID.randomUUID(), secret
                        )
                    }
                }

            }
        }
    }

    override fun onBlockUpdate(blockUpdateEvent: BlockUpdateEvent) {
        blockUpdateEvent.updatedBlocks.forEach { updatedBlock ->
            if (updatedBlock.second != DungeonRoom.preBuilt) {
                dungeonRoom.resetBlock(updatedBlock.first)
            }
        }
    }

    fun updateStrategy() {
        strategy = buildSecretStrategy(DgOneCongifConfig.secretFindMode, dungeonRoom)
    }

    companion object {
        val logger: Logger = LogManager.getLogger("GeneralRoomProcessor")
    }
}
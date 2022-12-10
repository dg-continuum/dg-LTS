package kr.syeyoung.dungeonsguide.dungeon

import kr.syeyoung.dungeonsguide.DungeonsGuide
import kr.syeyoung.dungeonsguide.chat.ChatTransmitter
import kr.syeyoung.dungeonsguide.config.Config
import kr.syeyoung.dungeonsguide.dungeon.roomedit.EditingContext
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonAddSet
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonParameterEdit
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonRoomEdit
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonValueEdit
import kr.syeyoung.dungeonsguide.events.impl.*
import kr.syeyoung.dungeonsguide.features.FeatureRegistry
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig
import kr.syeyoung.dungeonsguide.utils.*
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.passive.EntityBat
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.ExplosionEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import net.minecraftforge.fml.relauncher.Side
import org.joml.Vector3i
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14
import java.awt.Color
import java.io.IOException

class DungeonListener {
    @SubscribeEvent
    fun onExplosion(event: ExplosionEvent) {
        DungeonsGuide.getDungeonsGuide().dungeonFacade.context?.let {
            val pos = event.explosion.position
            it.expositions.add(Vector3i(pos.xCoord.toInt(), pos.yCoord.toInt(), pos.zCoord.toInt()))
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Unload?) {
        try {
            Config.saveConfig()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @SubscribeEvent
    fun onPostDraw(e: DrawScreenEvent.Post?) {
        if (!SkyblockStatus.isOnDungeon()) return

        DungeonsGuide.getDungeonsGuide().dungeonFacade.context?.let {
            Minecraft.getMinecraft().thePlayer ?: return
            it.bossfightProcessor?.onPostGuiRender(e)
            it.currentRoomProcessor?.onPostGuiRender(e)

            GlStateManager.enableBlend()
            GlStateManager.color(1f, 1f, 1f, 1f)
            GL14.glBlendFuncSeparate(
                GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA
            )
            GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA
            )
            GlStateManager.enableAlpha()
        }
    }

    @SubscribeEvent
    fun onEntityUpdate(e: LivingUpdateEvent?) {
        if (!SkyblockStatus.isOnDungeon()) return
        Minecraft.getMinecraft().thePlayer ?: return

        DungeonsGuide.getDungeonsGuide().dungeonFacade.context?.let {
            it.bossfightProcessor?.onEntityUpdate(e)
            it.currentRoomProcessor?.onEntityUpdate(e)
        }
    }

    @SubscribeEvent
    fun onDungeonLeave(ev: DungeonLeftEvent?) {
        DungeonsGuide.getDungeonsGuide().dungeonFacade.context?.let {
            it.batSpawnedLocations.clear()
            it.killedBats.clear()
            DungeonsGuide.getDungeonsGuide().dungeonFacade.context = null
            if (!FeatureRegistry.ADVANCED_DEBUGGABLE_MAP.isEnabled) {
                MapUtils.clearMap()
            }
        }
    }

    fun createDungeonContext() {
        val context = DungeonContext(Minecraft.getMinecraft().thePlayer.worldObj)
        context.started = System.currentTimeMillis()
        DungeonsGuide.getDungeonsGuide().dungeonFacade.context = context
        MinecraftForge.EVENT_BUS.post(DungeonStartedEvent())
    }

    @SubscribeEvent
    fun onTick(ev: ClientTickEvent) {
        if (ev.side == Side.SERVER || ev.phase != TickEvent.Phase.START) return
        val thePlayer = Minecraft.getMinecraft().thePlayer ?: return
        if (!SkyblockStatus.isOnDungeon()) return

        val context = DungeonsGuide.getDungeonsGuide().dungeonFacade.context
        if (context == null) {
            createDungeonContext()
            return
        }

        context.mapProcessor.tick()

        context.mapProcessor.worldPointToMapPoint(thePlayer.positionVector)?.let {
            if (!DungeonContext.roomBoundary.contains(
                    it.x, it.y
                ) && context.mapProcessor.isInitialized && context.bossRoomEnterSeconds == -1L
            ) {
                context.bossRoomEnterSeconds = (DungeonUtil.getTimeElapsed() / 1000).toLong()
                context.bossroomSpawnPos = thePlayer.position
                MinecraftForge.EVENT_BUS.post(BossroomEnterEvent())
                if (context.dataProvider != null) {
                    context.bossfightProcessor = context.dataProvider.createBossfightProcessor(
                        Minecraft.getMinecraft().theWorld, DungeonContext.getDungeonName()
                    )
                } else {
                    ChatTransmitter.sendDebugChat(ChatComponentText("Error:: Null Data Providier"))
                }
            }
        }


        context.players.clear()
        context.players.addAll(TabListUtil.getPlayersInDungeon())
        val secretsFound = DungeonUtil.getSecretsFound()
        if (context.latestSecretCnt != secretsFound) {
            context.latestSecretCnt = secretsFound
        }
        val latestTotalSecret = context.latestTotalSecret
        if (latestTotalSecret != DungeonUtil.getTotalSecretsInt()) {
            context.latestTotalSecret = DungeonUtil.getTotalSecretsInt()
        }
        val latestCrypts = context.latestCrypts
        val tombsFound = DungeonUtil.getTombsFound()
        if (latestCrypts != tombsFound) {
            context.latestCrypts = tombsFound
        }
        context.bossfightProcessor?.tick()
        context.currentRoomProcessor?.tick()
    }

    @SubscribeEvent
    fun onRender(postRender: RenderGameOverlayEvent.Post) {
        if (!(postRender.type == RenderGameOverlayEvent.ElementType.EXPERIENCE || postRender.type == RenderGameOverlayEvent.ElementType.JUMPBAR)) return
        if (!SkyblockStatus.isOnDungeon()) return
        val context = DungeonsGuide.getDungeonsGuide().dungeonFacade.context ?: return

        context.bossfightProcessor?.drawScreen(postRender.partialTicks)
        context.currentRoomProcessor?.drawScreen(postRender.partialTicks)

        GlStateManager.enableBlend()
        GlStateManager.color(1f, 1f, 1f, 1f)
        GL14.glBlendFuncSeparate(
            GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA
        )
        GlStateManager.tryBlendFuncSeparate(
            GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA
        )
        Minecraft.getMinecraft().entityRenderer.setupOverlayRendering()
        GlStateManager.enableAlpha()
    }

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    fun onChatReceived(clientChatReceivedEvent: ClientChatReceivedEvent) {
        if (!SkyblockStatus.isOnDungeon()) {
            return
        }
        if (clientChatReceivedEvent.type.toInt() == 1) {
            processSystemChatMessage(clientChatReceivedEvent)
        } else {
            processActionBarMessage(clientChatReceivedEvent)
        }

        DungeonsGuide.getDungeonsGuide().dungeonFacade.context?.let {
            val component = clientChatReceivedEvent.message
            val formatted = component.formattedText
            if (formatted.contains("§6> §e§lEXTRA STATS §6<")) {
                it.isEnded = true
            } else if (formatted.contains("§r§c☠ §r§eDefeated ")) {
                it.isDefeated = true
            }
        }
    }

    fun processActionBarMessage(e: ClientChatReceivedEvent) {
        DungeonsGuide.getDungeonsGuide().dungeonFacade.context?.let {
            it.bossfightProcessor?.actionbarReceived(e.message)
        }
    }

    fun processSystemChatMessage(e: ClientChatReceivedEvent) {
        val message = e.message
        if (message.formattedText.contains("§6> §e§lEXTRA STATS §6<")) {
            MinecraftForge.EVENT_BUS.post(DungeonEndedEvent())
        }

        DungeonsGuide.getDungeonsGuide().dungeonFacade.context?.let { context ->

            context.bossfightProcessor?.chatReceived(message)

            val roomPt = context.mapProcessor.worldPointToRoomPoint(VectorUtils.getPlayerVector3i())

            context.roomMapper[roomPt]?.let { dungeonRoom ->
                dungeonRoom.roomProcessor?.let { roomProcessor ->
                    roomProcessor.chatReceived(message)
                    for (globalRoomProcessor in context.globalRoomProcessors) {
                        if (globalRoomProcessor !== roomProcessor) {
                            globalRoomProcessor.chatReceived(message)
                        }
                    }
                }
            }

        }
    }

    @SubscribeEvent
    fun onWorldRender(renderWorldLastEvent: RenderWorldLastEvent) {
        if (!SkyblockStatus.isOnDungeon()) return
        val context = DungeonsGuide.getDungeonsGuide().dungeonFacade.context ?: return
        if (DgOneCongifConfig.debugMode) {
            val rooms: List<DungeonRoom> = ArrayList(context.dungeonRoomList)
            for (dungeonRoom in rooms) {
                for (door in dungeonRoom.doors) {
                    RenderUtils.renderDoor(door, renderWorldLastEvent.partialTicks)
                }
            }
        }
        context.bossfightProcessor?.drawWorld(renderWorldLastEvent.partialTicks)
        context.currentRoom?.let { dungeonRoom ->
            dungeonRoom.roomProcessor?.drawWorld(renderWorldLastEvent.partialTicks)
            if (DgOneCongifConfig.debugMode) {
                val player = Minecraft.getMinecraft().thePlayer.positionVector
                val real = BlockPos(player.xCoord * 2, player.yCoord * 2, player.zCoord * 2)
                for (allInBox in BlockPos.getAllInBox(real.add(-1, -1, -1), real.add(1, 1, 1))) {
                    val blocked = dungeonRoom.isBlocked(allInBox.x, allInBox.y, allInBox.z)
                    RenderUtils.highlightBox(
                        AxisAlignedBB.fromBounds(
                            allInBox.x / 2.0 - 0.1,
                            allInBox.y / 2.0 - 0.1,
                            allInBox.z / 2.0 - 0.1,
                            allInBox.x / 2.0 + 0.1,
                            allInBox.y / 2.0 + 0.1,
                            allInBox.z / 2.0 + 0.1
                        ),
                        if (blocked) Color(0x55FF0000, true) else Color(0x3300FF00, true),
                        renderWorldLastEvent.partialTicks,
                        false
                    )
                }
            }
        }

        if (EditingContext.getEditingContext() != null) {
            when (val guiScreen = EditingContext.getEditingContext().current) {
                is GuiDungeonParameterEdit -> {
                    val valueEdit = guiScreen.valueEdit
                    valueEdit?.renderWorld(renderWorldLastEvent.partialTicks)
                }

                is GuiDungeonValueEdit -> {
                    val valueEdit = guiScreen.valueEdit
                    valueEdit?.renderWorld(renderWorldLastEvent.partialTicks)
                }

                is GuiDungeonAddSet -> {
                    guiScreen.onWorldRender(renderWorldLastEvent.partialTicks)
                }
            }
        }
    }

    @SubscribeEvent
    fun onKey2(keyInputEvent: KeyBindPressedEvent?) {
        if (!SkyblockStatus.isOnDungeon()) return

        DungeonsGuide.getDungeonsGuide().dungeonFacade.context?.let { context ->
            context.bossfightProcessor?.onKeybindPress(keyInputEvent)
            context.currentRoomProcessor?.onKeybindPress(keyInputEvent)
        }
    }

    @SubscribeEvent
    fun onInteract(interact: PlayerInteractEntityEvent?) {
        if (!SkyblockStatus.isOnDungeon()) return

        DungeonsGuide.getDungeonsGuide().dungeonFacade.context?.let { context ->
            context.bossfightProcessor?.onInteract(interact)
            context.currentRoomProcessor?.onInteract(interact)
        }
    }

    val currentRoomName: String
        get() {
            val context = DungeonsGuide.getDungeonsGuide().dungeonFacade.context
            val roomPt = context.mapProcessor.worldPointToRoomPoint(VectorUtils.getPlayerVector3i())
            val dungeonRoom = context.roomMapper[roomPt]
            var `in` = "unknown"
            if (dungeonRoom != null) {
                `in` = dungeonRoom.dungeonRoomInfo.name
            }
            return `in`
        }

    @SubscribeEvent
    fun onBlockChange(blockUpdateEventPost: BlockUpdateEvent.Post?) {
        if (!SkyblockStatus.isOnDungeon()) return

        DungeonsGuide.getDungeonsGuide().dungeonFacade.context?.let { context ->
            context.bossfightProcessor?.onBlockUpdate(blockUpdateEventPost)
            context.currentRoomProcessor?.onBlockUpdate(blockUpdateEventPost)
        }

    }

    @SubscribeEvent
    fun onKeyInput(keyInputEvent: KeyBindPressedEvent) {
        if (DgOneCongifConfig.debugMode && DgOneCongifConfig.debugRoomEdit && keyInputEvent.key == DgOneCongifConfig.debugRoomeditKeybind.keyBinds[0]) {
            val ec = EditingContext.getEditingContext()
            if (ec == null) {
                val context = DungeonsGuide.getDungeonsGuide().dungeonFacade.context
                if (context == null) {
                    ChatTransmitter.addToQueue(ChatComponentText("Not in dungeons"))
                    return
                }
                val dungeonRoom = context.currentRoom
                if (dungeonRoom == null) {
                    ChatTransmitter.addToQueue(ChatComponentText("Can't determine the dungeon room you're in"))
                    return
                }
                if (EditingContext.getEditingContext() != null) {
                    ChatTransmitter.addToQueue(ChatComponentText("There is an editing session currently open."))
                    return
                }
                EditingContext.createEditingContext(dungeonRoom)
                EditingContext.getEditingContext().openGui(GuiDungeonRoomEdit(dungeonRoom))
            } else ec.reopen()
        }
    }

    @SubscribeEvent
    fun onInteract(playerInteractEvent: PlayerInteractEvent) {
        if (!playerInteractEvent.world.isRemote) return
        if (!SkyblockStatus.isOnDungeon()) return

        DungeonsGuide.getDungeonsGuide().dungeonFacade.context?.let { context ->
            context.bossfightProcessor?.onInteractBlock(playerInteractEvent)
            context.currentRoomProcessor?.onInteractBlock(playerInteractEvent)
        }
    }

    @SubscribeEvent
    fun onEntitySpawn(spawn: EntityJoinWorldEvent) {
        DungeonsGuide.getDungeonsGuide().dungeonFacade.context?.let { context ->
            context.batSpawnedLocations[spawn.entity.entityId] =
                Vector3i(spawn.entity.posX.toInt(), spawn.entity.posY.toInt(), spawn.entity.posZ.toInt())

        }
    }

    @SubscribeEvent
    fun onEntityDeSpawn(deathEvent: LivingDeathEvent) {
        if (!SkyblockStatus.isOnDungeon()) return
        DungeonsGuide.getDungeonsGuide().dungeonFacade.context?.let { context ->
            if (deathEvent.entityLiving is EntityBat) {
                context.killedBats.add(deathEvent.entity.entityId)
            } else {
                context.batSpawnedLocations.remove(deathEvent.entity.entityId)
            }
            context.bossfightProcessor?.onEntityDeath(deathEvent)
            context.currentRoomProcessor?.onEntityDeath(deathEvent)
        }
    }
}
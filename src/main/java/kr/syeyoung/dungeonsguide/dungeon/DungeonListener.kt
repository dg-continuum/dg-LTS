package kr.syeyoung.dungeonsguide.dungeon

import com.google.common.base.Throwables
import kr.syeyoung.dungeonsguide.chat.ChatTransmitter
import kr.syeyoung.dungeonsguide.config.Config
import kr.syeyoung.dungeonsguide.dungeon.room.NewDungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.roomdetection.NewDungeonRoomBuilder
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
import java.util.*

class DungeonListener {
    @SubscribeEvent
    fun onExplosion(event: ExplosionEvent) {
        DungeonFacade.context?.let {
            val pos = event.explosion.position
            it.expositions.add(Vector3i(pos.xCoord.toInt(), pos.yCoord.toInt(), pos.zCoord.toInt()))
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Unload) {
        try {
            Config.saveConfig()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @SubscribeEvent
    fun onPostDraw(e: DrawScreenEvent.Post) {
        if (!SkyblockStatus.isOnDungeon()) return

        Minecraft.getMinecraft().thePlayer ?: return

        DungeonFacade.context?.let {
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
    fun onEntityUpdate(e: LivingUpdateEvent) {
        if (!SkyblockStatus.isOnDungeon()) return
        Minecraft.getMinecraft().thePlayer ?: return

        DungeonFacade.context?.let {
            it.bossfightProcessor?.onEntityUpdate(e)
            it.currentRoomProcessor?.onEntityUpdate(e)
        }
    }

    @SubscribeEvent
    fun onDungeonLeave(ev: DungeonLeftEvent) {
        DungeonFacade.context?.let {
            it.batSpawnedLocations.clear()
            it.killedBats.clear()
            DungeonFacade.context = null
            if (!FeatureRegistry.ADVANCED_DEBUGGABLE_MAP.isEnabled) {
                MapUtils.clearMap()
            }
        }
    }

    @SubscribeEvent
    fun onTick(ev: ClientTickEvent) {
        if (ev.side == Side.SERVER || ev.phase != TickEvent.Phase.START) return
        val thePlayer = Minecraft.getMinecraft().thePlayer ?: return
        if (!SkyblockStatus.isOnDungeon()) return

        val context = DungeonFacade.context
        if (context == null) {
            val newContext = DungeonContext()
            newContext.started = System.currentTimeMillis()
            DungeonFacade.context = newContext
            MinecraftForge.EVENT_BUS.post(DungeonStartedEvent())
            return
        }

        // this is in a try-catch since creating a room might fail
        val currentRoom: NewDungeonRoom? = try {
            val playerPos = VectorUtils.getPlayerVector3i()
            // check if we are in an existing room
            val anRoom: NewDungeonRoom? = context.rooms.values
                .firstOrNull { it.isInRoom(playerPos) }

            // if we don't find one, create it
            anRoom ?: NewDungeonRoomBuilder.build(context).also {
                // put itself in the context
                context.rooms[UUID.randomUUID()] = it
            }

        } catch (e: Exception) {
            println("failed to find/create a room with: ${Throwables.getRootCause(e)}")
            null
        }

        // if we found/created a room we set it as current room
        if(currentRoom != null){
            context.currentNewRoom = currentRoom
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
                        Minecraft.getMinecraft().theWorld, SkyblockStatus.dungeonNameStrriped
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
        val context = DungeonFacade.context ?: return

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

        DungeonFacade.context?.let {
            val component = clientChatReceivedEvent.message
            val formatted = component.formattedText
            if (formatted.contains("§6> §e§lEXTRA STATS §6<")) {
                it.isEnded = true
            } else if (formatted.contains("§r§c☠ §r§eDefeated ")) {
                it.isDefeated = true
            }
        }
    }

    private fun processActionBarMessage(clientChatReceivedEvent: ClientChatReceivedEvent) {
        DungeonFacade.context?.let {
            it.bossfightProcessor?.actionbarReceived(clientChatReceivedEvent.message)
        }
    }

    private fun processSystemChatMessage(clientChatReceivedEvent: ClientChatReceivedEvent) {
        val message = clientChatReceivedEvent.message
        if (message.formattedText.contains("§6> §e§lEXTRA STATS §6<")) {
            MinecraftForge.EVENT_BUS.post(DungeonEndedEvent())
        }

        DungeonFacade.context?.let { context ->

            context.bossfightProcessor?.chatReceived(message)

            context.currentRoom?.let { dungeonRoom ->
                dungeonRoom.roomProcessor?.let { roomProcessor ->
                    roomProcessor.chatReceived(message)
                    for (globalRoomProcessor in context.globalRoomProcessors) {
                        if (globalRoomProcessor != roomProcessor) {
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
        val context = DungeonFacade.context ?: return

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
                val playerPos = Minecraft.getMinecraft().thePlayer.positionVector
                val real = BlockPos(playerPos.xCoord * 2, playerPos.yCoord * 2, playerPos.zCoord * 2)
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
    fun onKey2(keyInputEvent: KeyBindPressedEvent) {
        if (!SkyblockStatus.isOnDungeon()) return

        DungeonFacade.context?.let { context ->
            context.bossfightProcessor?.onKeybindPress(keyInputEvent)
            context.currentRoomProcessor?.onKeybindPress(keyInputEvent)
        }
    }

    @SubscribeEvent
    fun onInteract(interact: PlayerInteractEntityEvent) {
        if (!SkyblockStatus.isOnDungeon()) return

        DungeonFacade.context?.let { context ->
            context.bossfightProcessor?.onInteract(interact)
            context.currentRoomProcessor?.onInteract(interact)
        }
    }

    val currentRoomName: String
        get() {
            val dungeonRoom = DungeonFacade.context?.currentRoom
            var `in` = "unknown"
            if (dungeonRoom != null) {
                `in` = dungeonRoom.dungeonRoomInfo.name
            }
            return `in`
        }

    @SubscribeEvent
    fun onBlockChange(blockUpdateEventPost: BlockUpdateEvent.Post) {
        if (!SkyblockStatus.isOnDungeon()) return

        DungeonFacade.context?.let { context ->
            context.bossfightProcessor?.onBlockUpdate(blockUpdateEventPost)
            context.currentRoomProcessor?.onBlockUpdate(blockUpdateEventPost)
        }

    }

    @SubscribeEvent
    fun onKeyInput(keyInputEvent: KeyBindPressedEvent) {
        if (DgOneCongifConfig.debugMode && DgOneCongifConfig.debugRoomEdit && keyInputEvent.key == DgOneCongifConfig.debugRoomeditKeybind.keyBinds[0]) {
            val ec = EditingContext.getEditingContext()
            if (ec == null) {
                val context = DungeonFacade.context
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

        DungeonFacade.context?.let { context ->
            context.bossfightProcessor?.onInteractBlock(playerInteractEvent)
            context.currentRoomProcessor?.onInteractBlock(playerInteractEvent)
        }
    }

    @SubscribeEvent
    fun onEntitySpawn(entityJoinWorldEvent: EntityJoinWorldEvent) {
        DungeonFacade.context?.let { context ->
            if(entityJoinWorldEvent.entity is EntityBat){
                context.batSpawnedLocations[entityJoinWorldEvent.entity.entityId] =
                    Vector3i(entityJoinWorldEvent.entity.posX.toInt(), entityJoinWorldEvent.entity.posY.toInt(), entityJoinWorldEvent.entity.posZ.toInt())
            }

        }
    }

    @SubscribeEvent
    fun onEntityDeSpawn(deathEvent: LivingDeathEvent) {
        if (!SkyblockStatus.isOnDungeon()) return
        DungeonFacade.context?.let { context ->
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
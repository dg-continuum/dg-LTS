/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package kr.syeyoung.dungeonsguide.dungeon.roomprocessor

import kr.syeyoung.dungeonsguide.DungeonsGuide
import kr.syeyoung.dungeonsguide.chat.ChatTransmitter
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionMove
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRoute
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonSecret
import kr.syeyoung.dungeonsguide.dungeon.roomedit.EditingContext
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonAddSet
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonRoomEdit
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom
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
import org.joml.Vector3d
import org.joml.Vector3i
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14
import java.awt.Color
import java.util.*

open class GeneralRoomProcessor(val dungeonRoom: DungeonRoom) : RoomProcessor {
    private var stack = 0
    private var secrets2: Long = 0
    private var last = false
    private var lastChest: BlockPos? = null

    val context: DungeonContext = DungeonsGuide.getDungeonsGuide().dungeonFacade.context

    var strategy: SecretGuideStrategy = buildSecretStrategy(DgOneCongifConfig.secretFindMode, this)

    private val tickedFuse = SimpleFuse()
    override fun tick() {
        if (!tickedFuse.isBlown) {
            tickedFuse.blow()
            logger.info("Creating Pathfinding lines")
            strategy.init()
        }
        strategy.update()
    }

    override fun drawScreen(partialTicks: Float) {
        for (a in strategy.actionPath.values) {
            a.onRenderScreen(partialTicks)
        }
        if (DgOneCongifConfig.debugRoomEdit && DgOneCongifConfig.debugMode) {
            if (Minecraft.getMinecraft().objectMouseOver == null) return
            val en = Minecraft.getMinecraft().objectMouseOver.entityHit ?: return
            val sr = ScaledResolution(Minecraft.getMinecraft())

            if (context.batSpawnedLocations.containsKey(en.entityId)) {
                GlStateManager.enableBlend()
                GL14.glBlendFuncSeparate(
                    GL11.GL_SRC_ALPHA,
                    GL11.GL_ONE_MINUS_SRC_ALPHA,
                    GL11.GL_ONE,
                    GL11.GL_ONE_MINUS_SRC_ALPHA
                )
                GlStateManager.tryBlendFuncSeparate(
                    GL11.GL_SRC_ALPHA,
                    GL11.GL_ONE_MINUS_SRC_ALPHA,
                    GL11.GL_ONE,
                    GL11.GL_ONE_MINUS_SRC_ALPHA
                )
                val fr = Minecraft.getMinecraft().fontRendererObj
                fr.drawString(
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
        val finalSmallest = getBestFit(partialTicks)
        for ((_, value) in strategy.actionPath) {
            value.onRenderWorld(partialTicks, finalSmallest === value)
        }
        if (DgOneCongifConfig.debugMode && EditingContext.getEditingContext() != null && EditingContext.getEditingContext().current is GuiDungeonRoomEdit) {
            for ((key, value1) in dungeonRoom.mechanics) {
                value1?.highlight(Color(0, 255, 255, 50), key, dungeonRoom, partialTicks)
            }
        }
    }

    override fun chatReceived(chat: IChatComponent) {
        if (lastChest != null && chat.formattedText == "§r§cThis chest has already been searched!§r") {
            dungeonRoom.roomContext["c-" + lastChest.toString()] = 2
            lastChest = null
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
            stack = 0
            secrets2 = -1
            return
        }
        val pos2 = dungeonRoom.min.add(5, 0, 5)
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
        if (secrets2 == secrets.toLong()) stack++ else {
            stack = 0
            secrets2 = secrets.toLong()
        }
        if (stack == 4 && dungeonRoom.totalSecrets != secrets) {
            dungeonRoom.totalSecrets = secrets
            if (FeatureRegistry.DUNGEON_INTERMODCOMM.isEnabled) Minecraft.getMinecraft().thePlayer.sendChatMessage("/pchat \$DG-Comm " + pos2.x + "/" + pos2.z + " " + secrets)
        }
    }

    override fun readGlobalChat(): Boolean {
        return false
    }

    override fun onPostGuiRender(event: DrawScreenEvent.Post) {}
    override fun onEntityUpdate(updateEvent: LivingUpdateEvent) {
        if (updateEvent.entityLiving is EntityArmorStand && updateEvent.entityLiving.name != null &&
            updateEvent.entityLiving.name.contains("Mimic") &&
            !dungeonRoom.context.isGotMimic
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
            val actionRoute = getBestFit(0f)
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
                    if (it is ActionMove){
                        it.forceRefresh(dungeonRoom)
                    }
                }

            }
            if (FeatureRegistry.SECRET_CREATE_REFRESH_LINE.isPathfind && !actionRoute.actionRouteProperties.isPathfind) {
                actionRoute.actionRouteProperties.isPathfind = true
                actionRoute.actionRouteProperties.lineRefreshRate =
                    FeatureRegistry.SECRET_CREATE_REFRESH_LINE.refreshRate
            }
        }
    }

    override fun onInteract(event: PlayerInteractEntityEvent) {
        for (a in strategy.actionPath.values) {
            a.onLivingInteract(event)
        }
    }

    override fun onInteractBlock(event: PlayerInteractEvent) {
        for (a in strategy.actionPath.values) {
            a.onPlayerInteract(event)
        }
        if (event.pos != null) {
            val iBlockState = event.world.getBlockState(event.pos)
            if (iBlockState.block === Blocks.chest || iBlockState.block === Blocks.trapped_chest) lastChest = event.pos
        }
        if (event.entityPlayer.heldItem != null &&
            event.entityPlayer.heldItem.item === Items.stick &&
            DgOneCongifConfig.debugRoomEdit &&
            DgOneCongifConfig.debugMode
        ) {
            val ec = EditingContext.getEditingContext() ?: return
            if (ec.current !is GuiDungeonAddSet) return
            val gdas = ec.current as GuiDungeonAddSet
            if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                if (last) gdas.end.setPosInWorld(
                    dungeonRoom,
                    VectorUtils.BlockPosToVec3i(event.pos)
                ) else gdas.start.setPosInWorld(
                    dungeonRoom,
                    VectorUtils.BlockPosToVec3i(event.pos)
                )
                last = !last
            }
        }
    }

    private fun getBestFit(partialTicks: Float): ActionRoute? {
        if (!DgOneCongifConfig.freezePathfindingStatus) {
            return null
        }
        var smallest: ActionRoute? = null
        var smallestTan = 0.002
        for ((_, value) in strategy.actionPath) {
            if (value.actionRouteProperties.lineRefreshRate != -1 && value.actionRouteProperties.isPathfind) {
                continue
            }
            val currentAction = value.currentAction
            val target: Vector3i? = if (currentAction is ActionMove) {
                currentAction.target.getVector3i(dungeonRoom)
            } else {
                if (value.current >= 1) {
                    val abstractAction = value.actions[value.current - 1]
                    if (abstractAction is ActionMove) {
                        abstractAction.target.getVector3i(dungeonRoom)
                    } else {
                        continue
                    }
                } else {
                    continue
                }
            }
            val vectorV = VectorUtils.distSquared(
                VectorUtils.vec3ToVec3d(Minecraft.getMinecraft().renderViewEntity.getLook(partialTicks)),
                VectorUtils.vec3ToVec3d(Minecraft.getMinecraft().renderViewEntity.getPositionEyes(partialTicks)),
                Vector3d(target).add(0.5, 0.5, 0.5)
            )
            if (vectorV < smallestTan) {
                smallest = value
                smallestTan = vectorV
            }
        }
        return smallest
    }

    override fun onEntityDeath(deathEvent: LivingDeathEvent) {
        for (a in strategy.actionPath.values) {
            a.onLivingDeath(deathEvent)
        }
        if (EditingContext.getEditingContext() != null && EditingContext.getEditingContext().room === dungeonRoom) {
            if (deathEvent.entity is EntityBat) {
                for (screen in EditingContext.getEditingContext().guiStack) {
                    if (screen is GuiDungeonRoomEdit) {
                        val secret = DungeonSecret()
                        secret.secretType = DungeonSecret.SecretType.BAT
                        secret.secretPoint = OffsetPoint(
                            dungeonRoom,
                            context.batSpawnedLocations[deathEvent.entity.entityId]
                        )
                        screen.sep.createNewMechanic(
                            "BAT-" + UUID.randomUUID(),
                            secret
                        )
                        return
                    }
                }
                if (EditingContext.getEditingContext().current is GuiDungeonRoomEdit) {
                    val secret = DungeonSecret()
                    secret.secretType = DungeonSecret.SecretType.BAT
                    secret.secretPoint = OffsetPoint(
                        dungeonRoom,
                        context.batSpawnedLocations[deathEvent.entity.entityId]
                    )
                    (EditingContext.getEditingContext().current as GuiDungeonRoomEdit).sep.createNewMechanic(
                        "BAT-" + UUID.randomUUID(),
                        secret
                    )
                }
            }
        }
    }

    override fun onBlockUpdate(blockUpdateEvent: BlockUpdateEvent) {
        for (updatedBlock in blockUpdateEvent.updatedBlocks) {
            if (updatedBlock.second == DungeonRoom.preBuilt) continue
            dungeonRoom.resetBlock(updatedBlock.first)
        }
    }

    fun updateStrategy() {
        strategy = buildSecretStrategy(DgOneCongifConfig.secretFindMode, this)
    }

    companion object {
        val logger = LogManager.getLogger("GeneralRoomProcessor")
    }
}
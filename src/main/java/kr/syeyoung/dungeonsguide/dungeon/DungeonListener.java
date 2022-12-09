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

package kr.syeyoung.dungeonsguide.dungeon;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.config.Config;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonDoor;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonAddSet;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonParameterEdit;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonRoomEdit;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonValueEdit;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.solvers.bossfight.BossfightProcessor;
import kr.syeyoung.dungeonsguide.events.impl.*;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.utils.*;
import lombok.Getter;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DungeonListener {

    @SubscribeEvent
    public void onExplosion(ExplosionEvent event) {
        DungeonContext ctx = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (ctx != null) {
            Vec3 pos = event.explosion.getPosition();
            ctx.getExpositions().add(new Vector3i((int) pos.xCoord, (int) pos.yCoord, (int) pos.zCoord));
        }
    }

    @Getter
    private final Map<Integer, Vec3> entityIdToPosMap = new HashMap<>();

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Unload event) {
        try {
            Config.saveConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @SubscribeEvent
    public void onPostDraw(GuiScreenEvent.DrawScreenEvent.Post e) {
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {

            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            if (thePlayer == null) {
                return;
            }
            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onPostGuiRender(e);
            }

            RoomProcessor roomProcessor = context.getCurrentRoomProcessor();
            if (roomProcessor != null) {
                roomProcessor.onPostGuiRender(e);
            }

        }

        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
    }

    @SubscribeEvent
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent e) {
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context != null) {
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            if (thePlayer == null) return;
            BossfightProcessor bossfightProcessor = context.getBossfightProcessor();
            if (bossfightProcessor != null) bossfightProcessor.onEntityUpdate(e);

            RoomProcessor roomProcessor = context.getCurrentRoomProcessor();
            if (roomProcessor != null) {
                roomProcessor.onEntityUpdate(e);
            }
        }
    }

    @SubscribeEvent
    public void onDungeonLeave(DungeonLeftEvent ev) {
        DungeonContext ctx = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        ctx.getBatSpawnedLocations().clear();
        ctx.getKilledBats().clear();
        DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(null);
        if (!FeatureRegistry.ADVANCED_DEBUGGABLE_MAP.isEnabled()) {
            MapUtils.clearMap();
        }
    }

    void createDungeonContext() {
        DungeonContext context = new DungeonContext(Minecraft.getMinecraft().thePlayer.worldObj);
        context.setStarted(System.currentTimeMillis());
        DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(context);
        MinecraftForge.EVENT_BUS.post(new DungeonStartedEvent());
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent ev) {
        if (ev.side == Side.SERVER || ev.phase != TickEvent.Phase.START) return;
        val thePlayer = Minecraft.getMinecraft().thePlayer;
        if (thePlayer == null) return;
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) {
            createDungeonContext();
            return;
        }
        context.getMapProcessor().tick();

        Vector2i vector2i = context.getMapProcessor().worldPointToMapPoint(thePlayer.getPositionVector());
        if (vector2i != null) {
            if (!DungeonContext.roomBoundary.contains(vector2i.x, vector2i.y) && context.getMapProcessor().isInitialized() && context.getBossRoomEnterSeconds() == -1) {
                context.setBossRoomEnterSeconds(DungeonUtil.getTimeElapsed() / 1000);
                context.setBossroomSpawnPos(thePlayer.getPosition());
                MinecraftForge.EVENT_BUS.post(new BossroomEnterEvent());
                if (context.dataProvider != null) {
                    context.setBossfightProcessor(context.dataProvider.createBossfightProcessor(Minecraft.getMinecraft().theWorld, DungeonContext.getDungeonName()));
                } else {
                    ChatTransmitter.sendDebugChat(new ChatComponentText("Error:: Null Data Providier"));
                }
            }
        }

        context.getPlayers().clear();
        context.getPlayers().addAll(TabListUtil.getPlayersInDungeon());


        int secretsFound = DungeonUtil.getSecretsFound();
        if (context.getLatestSecretCnt() != secretsFound) {
            context.setLatestSecretCnt(secretsFound);
        }

        int latestTotalSecret = context.getLatestTotalSecret();
        if (latestTotalSecret != DungeonUtil.getTotalSecretsInt()) {
            context.setLatestTotalSecret(DungeonUtil.getTotalSecretsInt());
        }

        int latestCrypts = context.getLatestCrypts();
        int tombsFound = DungeonUtil.getTombsFound();
        if (latestCrypts != tombsFound) {
            context.setLatestCrypts(tombsFound);
        }

        if (context.getBossfightProcessor() != null) {
            context.getBossfightProcessor().tick();
        }


        RoomProcessor currentRoomProcessor = context.getCurrentRoomProcessor();

        if (currentRoomProcessor != null) {
            currentRoomProcessor.tick();
        }

    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post postRender) {
        if (!(postRender.type == RenderGameOverlayEvent.ElementType.EXPERIENCE || postRender.type == RenderGameOverlayEvent.ElementType.JUMPBAR))
            return;

        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) {
            return;
        }
        if (context.getBossfightProcessor() != null) {
            context.getBossfightProcessor().drawScreen(postRender.partialTicks);
        }


        RoomProcessor currentRoomProcessor = context.getCurrentRoomProcessor();

        if (currentRoomProcessor != null) {
            currentRoomProcessor.drawScreen(postRender.partialTicks);
        }

        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Minecraft.getMinecraft().entityRenderer.setupOverlayRendering();
        GlStateManager.enableAlpha();
    }

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    public void onChatReceived(ClientChatReceivedEvent clientChatReceivedEvent) {
        if (!SkyblockStatus.isOnDungeon()) {
            return;
        }
        if (clientChatReceivedEvent.type == 1) {
            processSystemChatMessage(clientChatReceivedEvent);
        } else {
            processActionBarMessage(clientChatReceivedEvent);
        }

        DungeonContext ctx = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (ctx != null) {
            IChatComponent component = clientChatReceivedEvent.message;
            String formatted = component.getFormattedText();
            if (formatted.contains("§6> §e§lEXTRA STATS §6<")) {
                ctx.setEnded(true);
            } else if (formatted.contains("§r§c☠ §r§eDefeated ")) {
                ctx.setDefeated(true);
            }
        }
    }

    void processActionBarMessage(ClientChatReceivedEvent e) {
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context == null) {
            return;
        }

        if (context.getBossfightProcessor() != null) {
            context.getBossfightProcessor().actionbarReceived(e.message);
        }
    }

    void processSystemChatMessage(ClientChatReceivedEvent e) {
        IChatComponent message = e.message;
        if (message.getFormattedText().contains("§6> §e§lEXTRA STATS §6<")) {
            MinecraftForge.EVENT_BUS.post(new DungeonEndedEvent());
        }
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {
            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().chatReceived(message);
            }

            Vector2i roomPt = context.getMapProcessor().worldPointToRoomPoint(VectorUtils.getPlayerVector3i());

            DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
            if (dungeonRoom != null && dungeonRoom.getRoomProcessor() != null) {
                RoomProcessor roomProcessor = dungeonRoom.getRoomProcessor();
                roomProcessor.chatReceived(message);

                for (RoomProcessor globalRoomProcessor : context.getGlobalRoomProcessors()) {
                    if (globalRoomProcessor != roomProcessor) {
                        globalRoomProcessor.chatReceived(message);
                    }
                }
            }

        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent renderWorldLastEvent) {
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) {
            return;
        }

        if (DgOneCongifConfig.debugMode) {
            List<DungeonRoom> rooms = new ArrayList<>(context.getDungeonRoomList());
            for (DungeonRoom dungeonRoom : rooms) {
                for (DungeonDoor door : dungeonRoom.getDoors()) {
                    RenderUtils.renderDoor(door, renderWorldLastEvent.partialTicks);
                }
            }
        }


        BossfightProcessor bossfightProcessor = context.getBossfightProcessor();
        if (bossfightProcessor != null) {
            bossfightProcessor.drawWorld(renderWorldLastEvent.partialTicks);
        }


        DungeonRoom dungeonRoom = context.getCurrentRoom();
        if (dungeonRoom != null) {
            RoomProcessor currentRoomProcessor = dungeonRoom.getRoomProcessor();
            if (currentRoomProcessor != null){
                currentRoomProcessor.drawWorld(renderWorldLastEvent.partialTicks);
            }
            if (DgOneCongifConfig.debugMode) {

                Vec3 player = Minecraft.getMinecraft().thePlayer.getPositionVector();
                BlockPos real = new BlockPos(player.xCoord * 2, player.yCoord * 2, player.zCoord * 2);
                for (BlockPos allInBox : BlockPos.getAllInBox(real.add(-1, -1, -1), real.add(1, 1, 1))) {
                    boolean blocked = dungeonRoom.isBlocked(allInBox.getX(), allInBox.getY(), allInBox.getZ());

                    RenderUtils.highlightBox(AxisAlignedBB.fromBounds(allInBox.getX() / 2.0 - 0.1, allInBox.getY() / 2.0 - 0.1, allInBox.getZ() / 2.0 - 0.1, allInBox.getX() / 2.0 + 0.1, allInBox.getY() / 2.0 + 0.1, allInBox.getZ() / 2.0 + 0.1), blocked ? new Color(0x55FF0000, true) : new Color(0x3300FF00, true), renderWorldLastEvent.partialTicks, false);

                }
            }
        }



        if (EditingContext.getEditingContext() != null) {
            GuiScreen guiScreen = EditingContext.getEditingContext().getCurrent();
            if (guiScreen instanceof GuiDungeonParameterEdit) {
                ValueEdit valueEdit = ((GuiDungeonParameterEdit) guiScreen).getValueEdit();
                if (valueEdit != null) {
                    valueEdit.renderWorld(renderWorldLastEvent.partialTicks);
                }
            } else if (guiScreen instanceof GuiDungeonValueEdit) {
                ValueEdit valueEdit = ((GuiDungeonValueEdit) guiScreen).getValueEdit();
                if (valueEdit != null) {
                    valueEdit.renderWorld(renderWorldLastEvent.partialTicks);
                }
            } else if (guiScreen instanceof GuiDungeonAddSet) {
                ((GuiDungeonAddSet) guiScreen).onWorldRender(renderWorldLastEvent.partialTicks);
            }
        }
    }

    @SubscribeEvent
    public void onKey2(KeyBindPressedEvent keyInputEvent) {
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null) {

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onKeybindPress(keyInputEvent);
            }
            RoomProcessor roomProcessor = context.getCurrentRoomProcessor();
            if (roomProcessor != null) {
                roomProcessor.onKeybindPress(keyInputEvent);
            }
        }
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEntityEvent interact) {
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onInteract(interact);
            }

            RoomProcessor roomProcessor = context.getCurrentRoomProcessor();
            if (roomProcessor != null) {
                roomProcessor.onInteract(interact);
            }

        }
    }

    String getCurrentRoomName() {

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        Vector2i roomPt = context.getMapProcessor().worldPointToRoomPoint(VectorUtils.getPlayerVector3i());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        String in = "unknown";
        if (dungeonRoom != null) {
            in = dungeonRoom.getDungeonRoomInfo().getName();
        }

        return in;
    }

    @SubscribeEvent
    public void onBlockChange(BlockUpdateEvent.Post postInteract) {
        if (!SkyblockStatus.isOnDungeon()) return;


        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null) {

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onBlockUpdate(postInteract);
            }

            RoomProcessor roomProcessor = context.getCurrentRoomProcessor();
            if (roomProcessor != null) {
                roomProcessor.onBlockUpdate(postInteract);
            }

        }
    }

    @SubscribeEvent
    public void onKeyInput(KeyBindPressedEvent keyInputEvent) {
        if (DgOneCongifConfig.debugMode && DgOneCongifConfig.debugRoomEdit && keyInputEvent.getKey() == DgOneCongifConfig.debugRoomeditKeybind.getKeyBinds().get(0)) {
            EditingContext ec = EditingContext.getEditingContext();
            if (ec == null) {
                DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
                if (context == null) {
                    ChatTransmitter.addToQueue(new ChatComponentText("Not in dungeons"));
                    return;
                }
                DungeonRoom dungeonRoom = context.getCurrentRoom();

                if (dungeonRoom == null) {
                    ChatTransmitter.addToQueue(new ChatComponentText("Can't determine the dungeon room you're in"));
                    return;
                }

                if (EditingContext.getEditingContext() != null) {
                    ChatTransmitter.addToQueue(new ChatComponentText("There is an editing session currently open."));
                    return;
                }

                EditingContext.createEditingContext(dungeonRoom);
                EditingContext.getEditingContext().openGui(new GuiDungeonRoomEdit(dungeonRoom));
            } else ec.reopen();
        }
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent keyInputEvent) {
        if (!keyInputEvent.world.isRemote) return;
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onInteractBlock(keyInputEvent);
            }

            RoomProcessor roomProcessor = context.getCurrentRoomProcessor();
            if (roomProcessor != null) {
                roomProcessor.onInteractBlock(keyInputEvent);
            }
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent spawn) {
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context != null) {
            context.getBatSpawnedLocations().put(spawn.entity.getEntityId(), new Vector3i((int) spawn.entity.posX, (int) spawn.entity.posY, (int) spawn.entity.posZ));
        }
    }


    @SubscribeEvent
    public void onEntityDeSpawn(LivingDeathEvent deathEvent) {
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        if (context != null) {
            if (deathEvent.entityLiving instanceof EntityBat) {
                context.getKilledBats().add(deathEvent.entity.getEntityId());
            } else {
                context.getBatSpawnedLocations().remove(deathEvent.entity.getEntityId());
            }

            if (context.getBossfightProcessor() != null) {
                context.getBossfightProcessor().onEntityDeath(deathEvent);
            }

            RoomProcessor roomProcessor = context.getCurrentRoomProcessor();
            if (roomProcessor != null) {
                roomProcessor.onEntityDeath(deathEvent);
            }
        }
    }

}

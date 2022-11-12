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

package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.general;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonSecret;
import kr.syeyoung.dungeonsguide.dungeon.pathfinding.NodeProcessorDungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonAddSet;
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonRoomEdit;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.events.impl.BlockUpdateEvent;
import kr.syeyoung.dungeonsguide.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.events.impl.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Tuple;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.UUID;

public class GeneralRoomProcessor implements RoomProcessor {
    @Getter
    private final SecretGuider secretGuider;
    @Getter
    @Setter
    private DungeonRoom dungeonRoom;
    private int stack = 0;
    private long secrets2 = 0;
    private boolean last = false;
    private BlockPos lastChest;

    public GeneralRoomProcessor(DungeonRoom dungeonRoom) {
        this.dungeonRoom = dungeonRoom;
        this.secretGuider = new SecretGuider(dungeonRoom);
    }

    @Override
    public void tick() {
        secretGuider.tick();
    }


    @Override
    public void drawScreen(float partialTicks) {
        secretGuider.drawScreen(partialTicks);

        if (DgOneCongifConfig.DEBUG_ROOM_EDIT && DgOneCongifConfig.DEBUG_MODE) {

            if (Minecraft.getMinecraft().objectMouseOver == null) return;
            Entity en = Minecraft.getMinecraft().objectMouseOver.entityHit;
            if (en == null) return;

            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBatSpawnedLocations().containsKey(en.getEntityId())) {
                GlStateManager.enableBlend();
                GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
                fr.drawString("Spawned at " + DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBatSpawnedLocations().get(en.getEntityId()), sr.getScaledWidth() / 2, sr.getScaledHeight() / 2, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public void drawWorld(float partialTicks) {
        secretGuider.drawWorld(partialTicks);

        if (DgOneCongifConfig.DEBUG_MODE && (EditingContext.getEditingContext() != null && EditingContext.getEditingContext().getCurrent() instanceof GuiDungeonRoomEdit)) {
            for (val value : dungeonRoom.getMechanics().entrySet()) {
                if (value.getValue() != null) {
                    value.getValue().highlight(new Color(0, 255, 255, 50), value.getKey(), dungeonRoom, partialTicks);
                }
            }
        }
    }


    @Override
    public void chatReceived(IChatComponent chat) {
        if (lastChest != null && chat.getFormattedText().equals("§r§cThis chest has already been searched!§r")) {
            getDungeonRoom().getRoomContext().put("c-" + lastChest.toString(), 2);
            lastChest = null;
        }
    }

    @Override
    public void actionbarReceived(IChatComponent chat) {
        if (!SkyblockStatus.isOnDungeon()) return;
        if (dungeonRoom.getTotalSecrets() == -1) {
            ChatTransmitter.sendDebugChat(chat.getFormattedText().replace('§', '&') + " - received");
        }
        if (!chat.getFormattedText().contains("/")) return;
        BlockPos pos = Minecraft.getMinecraft().thePlayer.getPosition();

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        Point pt1 = context.getMapProcessor().worldPointToRoomPoint(pos.add(2, 0, 2));
        Point pt2 = context.getMapProcessor().worldPointToRoomPoint(pos.add(-2, 0, -2));
        if (!pt1.equals(pt2)) {
            stack = 0;
            secrets2 = -1;
            return;
        }
        BlockPos pos2 = dungeonRoom.getMin().add(5, 0, 5);

        String text = chat.getFormattedText();
        int secretsIndex = text.indexOf("Secrets");
        int secrets = 0;
        if (secretsIndex != -1) {
            int theindex = 0;
            for (int i = secretsIndex; i >= 0; i--) {
                if (text.startsWith("§7", i)) {
                    theindex = i;
                }
            }
            String it = text.substring(theindex + 2, secretsIndex - 1);

            secrets = Integer.parseInt(it.split("/")[1]);
        }

        if (secrets2 == secrets) stack++;
        else {
            stack = 0;
            secrets2 = secrets;
        }

        if (stack == 4 && dungeonRoom.getTotalSecrets() != secrets) {
            dungeonRoom.setTotalSecrets(secrets);
            if (FeatureRegistry.DUNGEON_INTERMODCOMM.isEnabled())
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/pchat $DG-Comm " + pos2.getX() + "/" + pos2.getZ() + " " + secrets);
        }
    }

    @Override
    public boolean readGlobalChat() {
        return false;
    }


    @Override
    public void onPostGuiRender(GuiScreenEvent.DrawScreenEvent.Post event) {

    }

    @Override
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent updateEvent) {
        if (updateEvent.entityLiving instanceof EntityArmorStand &&
                updateEvent.entityLiving.getName() != null &&
                updateEvent.entityLiving.getName().contains("Mimic") &&
                !dungeonRoom.getContext().isGotMimic()) {
            dungeonRoom.getContext().setGotMimic(true);
        }
    }

    @Override
    public void onKeybindPress(KeyBindPressedEvent keyInputEvent) {
        secretGuider.onKeyPress(keyInputEvent);
    }

    @Override
    public void onInteract(PlayerInteractEntityEvent event) {
        secretGuider.onIteract(event);
    }

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {
        secretGuider.onIteractBlock(event);


        if (event.pos != null) {
            IBlockState iBlockState = event.world.getBlockState(event.pos);
            if (iBlockState.getBlock() == Blocks.chest || iBlockState.getBlock() == Blocks.trapped_chest)
                lastChest = event.pos;
        }

        if (event.entityPlayer.getHeldItem() != null &&
                event.entityPlayer.getHeldItem().getItem() == Items.stick &&
                DgOneCongifConfig.DEBUG_ROOM_EDIT &&
                DgOneCongifConfig.DEBUG_MODE) {
            EditingContext ec = EditingContext.getEditingContext();
            if (ec == null) return;
            if (!(ec.getCurrent() instanceof GuiDungeonAddSet)) return;
            GuiDungeonAddSet gdas = (GuiDungeonAddSet) ec.getCurrent();
            if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                if (last)
                    gdas.getEnd().setPosInWorld(getDungeonRoom(), event.pos);
                else
                    gdas.getStart().setPosInWorld(getDungeonRoom(), event.pos);

                last = !last;
            }
        }
    }

    @Override
    public void onEntityDeath(LivingDeathEvent deathEvent) {

        secretGuider.onEntityDeath(deathEvent);
        if (EditingContext.getEditingContext() != null && EditingContext.getEditingContext().getRoom() == getDungeonRoom()) {
            if (deathEvent.entity instanceof EntityBat) {
                for (GuiScreen screen : EditingContext.getEditingContext().getGuiStack()) {
                    if (screen instanceof GuiDungeonRoomEdit) {
                        DungeonSecret secret = new DungeonSecret();
                        secret.setSecretType(DungeonSecret.SecretType.BAT);
                        secret.setSecretPoint(new OffsetPoint(dungeonRoom,
                                DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBatSpawnedLocations().get(deathEvent.entity.getEntityId())
                        ));
                        ((GuiDungeonRoomEdit) screen).getSep().createNewMechanic("BAT-" + UUID.randomUUID(),
                                secret);
                        return;
                    }
                }
                if (EditingContext.getEditingContext().getCurrent() instanceof GuiDungeonRoomEdit) {
                    DungeonSecret secret = new DungeonSecret();
                    secret.setSecretType(DungeonSecret.SecretType.BAT);
                    secret.setSecretPoint(new OffsetPoint(dungeonRoom,
                            DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBatSpawnedLocations().get(deathEvent.entity.getEntityId())
                    ));
                    ((GuiDungeonRoomEdit) EditingContext.getEditingContext().getCurrent()).getSep().createNewMechanic("BAT-" + UUID.randomUUID(),
                            secret);
                }
            }
        }
    }

    @Override
    public void onBlockUpdate(BlockUpdateEvent blockUpdateEvent) {
        for (Tuple<BlockPos, IBlockState> updatedBlock : blockUpdateEvent.getUpdatedBlocks()) {
            if (updatedBlock.getSecond().equals(NodeProcessorDungeonRoom.preBuilt)) continue;
            dungeonRoom.resetBlock(updatedBlock.getFirst());
        }
    }
}

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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonActionContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.AbstractAction;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionComplete;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionMove;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionMoveNearestAir;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionRouteProperties;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonRoomDoor;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonSecret;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.impl.BlockUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.NodeProcessorDungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonAddSet;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonRoomEdit;
import kr.syeyoung.dungeonsguide.mod.onconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.mod.onconfig.secrets.AutoPathfindPage;
import kr.syeyoung.dungeonsguide.mod.onconfig.secrets.PathfindToALlPage;
import kr.syeyoung.dungeonsguide.mod.utils.SimpleFuse;
import kr.syeyoung.dungeonsguide.mod.utils.VectorUtils;
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
import net.minecraft.util.*;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.*;

public class GeneralRoomProcessor implements RoomProcessor {

    @Getter
    @Setter
    private DungeonRoom dungeonRoom;
    public GeneralRoomProcessor(DungeonRoom dungeonRoom) {
        this.dungeonRoom = dungeonRoom;
    }
    private final SimpleFuse tickedFuse = new SimpleFuse();


    @Override
    public void tick() {
        if(!tickedFuse.isBlown()){
            switch (DgOneCongifConfig.secretFindMode){
                case 0:
                    for (Map.Entry<String, DungeonMechanic> value : getDungeonRoom().getDungeonRoomInfo().getMechanics().entrySet()) {
                        if (value.getValue() instanceof DungeonSecret && ((DungeonSecret) value.getValue()).getSecretStatus(dungeonRoom) != DungeonSecret.SecretStatus.FOUND) {
                            DungeonSecret dungeonSecret = (DungeonSecret) value.getValue();
                            if (PathfindToALlPage.pfTaBAT && dungeonSecret.getSecretType() == DungeonSecret.SecretType.BAT)
                                pathfind(value.getKey(), "found", FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_BAT.getRouteProperties());
                            if (PathfindToALlPage.pfTaCHEST && dungeonSecret.getSecretType() == DungeonSecret.SecretType.CHEST)
                                pathfind(value.getKey(), "found", FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_CHEST.getRouteProperties());
                            if (PathfindToALlPage.pfTaESSENCE && dungeonSecret.getSecretType() == DungeonSecret.SecretType.ESSENCE)
                                pathfind(value.getKey(), "found", FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_ESSENCE.getRouteProperties());
                            if (PathfindToALlPage.pfTaITEMDROP && dungeonSecret.getSecretType() == DungeonSecret.SecretType.ITEM_DROP)
                                pathfind(value.getKey(), "found", FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_ITEM_DROP.getRouteProperties());
                        }
                    }
                    break;
                case 1:
                    DgOneCongifConfig.bloodRush = true;
                    for (Map.Entry<String, DungeonMechanic> value : getDungeonRoom().getMechanics().entrySet()) {
                        if (value.getValue() instanceof DungeonRoomDoor) {
                            DungeonRoomDoor dungeonDoor = (DungeonRoomDoor) value.getValue();
                            if (dungeonDoor.getDoorfinder().getType().isHeadToBlood()) {
                                pathfind(value.getKey(), "navigate", FeatureRegistry.SECRET_BLOOD_RUSH_LINE_PROPERTIES.getRouteProperties());
                            }
                        }
                    }
                    break;
                case 2:
                    searchForNextTarget();
                    break;
            }
        }
        tickedFuse.blow();

        Set<String> toRemove = new HashSet<>();
        path.forEach((key, value) -> {
            value.onTick();
            if (value.getCurrentAction() instanceof ActionComplete)
                toRemove.add(key);
        });
        toRemove.forEach(path::remove);


        for (DungeonMechanic value : dungeonRoom.getMechanics().values()) {
            if (value instanceof DungeonSecret) ((DungeonSecret) value).tick(dungeonRoom);
        }

        if (toRemove.contains("AUTO-BROWSE") && AutoPathfindPage.autoBrowseToNext) {
            searchForNextTarget();
        }
    }
    private final Set<String> visited = new HashSet<>();

    public void searchForNextTarget() {
        if (getDungeonRoom().getCurrentState() == DungeonRoom.RoomState.FINISHED) {
            cancelAll();
            return;
        }

        val lowestWeightMechanic = getSimplesMechanic();

        if (lowestWeightMechanic.isPresent()) {
            visited.add(lowestWeightMechanic.get());
            pathfind("AUTO-BROWSE",
                    lowestWeightMechanic.get(),
                    "found",
                    FeatureRegistry.SECRET_LINE_PROPERTIES_AUTOPATHFIND.getRouteProperties());
        } else {
            visited.clear();
        }
    }

    private Optional<String> getSimplesMechanic(){
        BlockPos pos = Minecraft.getMinecraft().thePlayer.getPosition();
        Map.Entry<String, DungeonMechanic> lowestWeightMechanic = null;
        double lowestCost = Float.MAX_VALUE;
        for (val mechanic : dungeonRoom.getMechanics().entrySet()) {

            if (mechanic.getValue() instanceof DungeonSecret) {
                DungeonSecret secret = (DungeonSecret) mechanic.getValue();

                if (!visited.contains(mechanic.getKey())) {
                    if (secret.getSecretStatus(getDungeonRoom()) != DungeonSecret.SecretStatus.FOUND) {
                        double cost = 0;
                        if (secret.getSecretType() == DungeonSecret.SecretType.BAT) {
                            if (secret.getPreRequisite().size() == 0) {
                                cost -= 100000000;
                            }
                        }
                        if (secret.getRepresentingPoint(getDungeonRoom()) != null) {
                            BlockPos blockpos = secret.getRepresentingPoint(getDungeonRoom()).getBlockPos(getDungeonRoom());

                            cost += blockpos.distanceSq(pos);
                            cost += secret.getPreRequisite().size() * 100;
                        }
                        if (cost < lowestCost) {
                            lowestCost = cost;
                            lowestWeightMechanic = mechanic;
                        }
                    }
                }
            }
        }

        return lowestWeightMechanic == null ? Optional.empty() : Optional.of(lowestWeightMechanic.getKey());
    }


    @Override
    public void drawScreen(float partialTicks) {
        path.values().forEach(a -> {
            a.onRenderScreen(partialTicks);
        });

        if (DgOneCongifConfig.DEBUG_ROOM_EDIT && DgOneCongifConfig.DEBUG_MODE) {

            if (Minecraft.getMinecraft().objectMouseOver == null) return;
            Entity en = Minecraft.getMinecraft().objectMouseOver.entityHit;
            if (en == null) return;

            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            if (DungeonActionContext.getSpawnLocation().containsKey(en.getEntityId())) {
                GlStateManager.enableBlend();
                GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
                fr.drawString("Spawned at " + DungeonActionContext.getSpawnLocation().get(en.getEntityId()), sr.getScaledWidth() / 2, sr.getScaledHeight() / 2, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public void drawWorld(float partialTicks) {
        if (DgOneCongifConfig.DEBUG_MODE && (EditingContext.getEditingContext() != null && EditingContext.getEditingContext().getCurrent() instanceof GuiDungeonRoomEdit)) {
            for (val value : dungeonRoom.getMechanics().entrySet()) {
                if (value.getValue() != null) {
                    value.getValue().highlight(new Color(0, 255, 255, 50), value.getKey(), dungeonRoom, partialTicks);
                }
            }
        }


        ActionRoute finalSmallest = getBestFit(partialTicks);
        path.values().forEach(a -> a.onRenderWorld(partialTicks, finalSmallest == a));
    }

    private ActionRoute getBestFit(float partialTicks) {

        ActionRoute smallest = null;
        double smallestTan = 0.002;
        for (ActionRoute value : path.values()) {
            BlockPos target;
            AbstractAction currentAction = value.getCurrentAction();
            if (currentAction instanceof ActionMove) {
                target = ((ActionMove) currentAction).getTarget().getBlockPos(dungeonRoom);
            } else if (currentAction instanceof ActionMoveNearestAir) {
                target = ((ActionMoveNearestAir) currentAction).getTarget().getBlockPos(dungeonRoom);
            } else {

                if(value.getCurrent() >= 1){
                    AbstractAction abstractAction = value.getActions().get(value.getCurrent() - 1);

                    if (abstractAction instanceof ActionMove) {
                        val actionMove = (ActionMove) abstractAction;
                        target = actionMove.getTarget().getBlockPos(dungeonRoom);
                    } else if (abstractAction instanceof ActionMoveNearestAir) {
                        val moveNearestAir = (ActionMoveNearestAir) abstractAction;
                        target = moveNearestAir.getTarget().getBlockPos(dungeonRoom);
                    } else {
                        continue;
                    }

                } else {
                    continue;
                }

            }

            if (value.getActionRouteProperties().getLineRefreshRate() != -1 && value.getActionRouteProperties().isPathfind() && !DgOneCongifConfig.freezePathfindingStatus) continue;

            Entity e = Minecraft.getMinecraft().getRenderViewEntity();

            double vectorV = VectorUtils.distSquared(e.getLook(partialTicks), e.getPositionEyes(partialTicks), new Vec3(target).addVector(0.5,0.5,0.5));

            if (vectorV < smallestTan) {
                smallest = value;
                smallestTan = vectorV;
            }
        }
        return smallest;
    }

    @Override
    public void chatReceived(IChatComponent chat) {
        if (lastChest != null && chat.getFormattedText().equals("§r§cThis chest has already been searched!§r")) {
            getDungeonRoom().getRoomContext().put("c-"+lastChest.toString(), 2);
            lastChest = null;
        }
    }

    private int stack = 0;
    private long secrets2 = 0;
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

    @Getter
    private Map<String, ActionRoute> path = new HashMap<>();

    public ActionRoute getPath(String id){
        return path.get(id);
    }

    public String pathfind(String mechanic, String state, ActionRouteProperties actionRouteProperties) {
        String str = UUID.randomUUID().toString();
        pathfind(str, mechanic, state, actionRouteProperties);
        return str;
    }
    public void pathfind(String id, String mechanic, String state, ActionRouteProperties actionRouteProperties) {
        path.put(id, new ActionRoute(getDungeonRoom(), mechanic, state, actionRouteProperties));
    }
    public void cancelAll() {
        path.clear();
    }
    public void cancel(String id) {
        path.remove(id);
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
//            Minecraft.getMinecraft().thePlayer.sendChatMessage("/pc $DG-Mimic");
        }
    }

    Logger logger = LogManager.getLogger("GeneralRoomProcessor");

    @Override
    public void onKeybindPress(KeyBindPressedEvent keyInputEvent) {

        if (AutoPathfindPage.keybind.getKeyBinds().get(0) == keyInputEvent.getKey()) {
            if (AutoPathfindPage.autoBrowseToNext) {
                searchForNextTarget();
            }
            return;
        }

        if (FeatureRegistry.SECRET_CREATE_REFRESH_LINE.getKeybind() == keyInputEvent.getKey()) {
            if(!FeatureRegistry.SECRET_CREATE_REFRESH_LINE.isEnabled()) return;

            ActionRoute actionRoute = getBestFit(0);
            AbstractAction currentAction = actionRoute.getCurrentAction();
            if (currentAction == null) {
                logger.error("currentAction was null after SECRET_CREATE_REFRESH_LINE keypress");
                return;
            }

            if (currentAction instanceof ActionMove) {
                ActionMove ac = (ActionMove) currentAction;
                ac.forceRefresh(getDungeonRoom());
            } else if (currentAction instanceof ActionMoveNearestAir) {
                ActionMoveNearestAir ac = (ActionMoveNearestAir) currentAction;
                ac.forceRefresh(getDungeonRoom());
            } else if (actionRoute.getCurrent() >= 1) {
                AbstractAction abstractAction = actionRoute.getActions().get(actionRoute.getCurrent() - 1);

                if (abstractAction instanceof ActionMove) {
                    ((ActionMove) abstractAction).forceRefresh(dungeonRoom);
                }

                if ( abstractAction instanceof ActionMoveNearestAir) {
                    ((ActionMoveNearestAir) abstractAction).forceRefresh(dungeonRoom);
                }
            }
            if (FeatureRegistry.SECRET_CREATE_REFRESH_LINE.isPathfind() && !actionRoute.getActionRouteProperties().isPathfind()) {
                actionRoute.getActionRouteProperties().setPathfind(true);
                actionRoute.getActionRouteProperties().setLineRefreshRate(FeatureRegistry.SECRET_CREATE_REFRESH_LINE.getRefreshRate());
            }

        }
    }

    @Override
    public void onInteract(PlayerInteractEntityEvent event) {
        path.values().forEach(a -> {
            a.onLivingInteract(event);
        });
    }

    private boolean last = false;
    private BlockPos lastChest;
    @Override
    public void onInteractBlock(PlayerInteractEvent event) {
        path.values().forEach(a -> {
            a.onPlayerInteract(event);
        });

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
        path.values().forEach(a -> {
            a.onLivingDeath(deathEvent);
        });
        if (EditingContext.getEditingContext() != null && EditingContext.getEditingContext().getRoom() == getDungeonRoom()) {
            if (deathEvent.entity instanceof EntityBat) {
                for (GuiScreen screen : EditingContext.getEditingContext().getGuiStack()) {
                    if (screen instanceof GuiDungeonRoomEdit) {
                        DungeonSecret secret = new DungeonSecret();
                        secret.setSecretType(DungeonSecret.SecretType.BAT);
                        secret.setSecretPoint(new OffsetPoint(dungeonRoom,
                                DungeonActionContext.getSpawnLocation().get(deathEvent.entity.getEntityId())
                        ));
                        ((GuiDungeonRoomEdit) screen).getSep().createNewMechanic("BAT-"+ UUID.randomUUID(),
                                secret);
                        return;
                    }
                }
                if (EditingContext.getEditingContext().getCurrent() instanceof GuiDungeonRoomEdit) {
                    DungeonSecret secret = new DungeonSecret();
                    secret.setSecretType(DungeonSecret.SecretType.BAT);
                    secret.setSecretPoint(new OffsetPoint(dungeonRoom,
                            DungeonActionContext.getSpawnLocation().get(deathEvent.entity.getEntityId())
                    ));
                    ((GuiDungeonRoomEdit) EditingContext.getEditingContext().getCurrent()).getSep().createNewMechanic("BAT-"+ UUID.randomUUID(),
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

    public static class Generator implements RoomProcessorGenerator<GeneralRoomProcessor> {
        @Override
        public GeneralRoomProcessor createNew(DungeonRoom dungeonRoom) {
            GeneralRoomProcessor defaultRoomProcessor = new GeneralRoomProcessor(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}

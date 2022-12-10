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

package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.*;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.BlockCache;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.VectorUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class DungeonSecret implements DungeonMechanic {

    private static final long serialVersionUID = 8784808599222706537L;


    public OffsetPoint getSecretPoint() {
        return secretPoint;
    }

    public void setSecretPoint(OffsetPoint secretPoint) {
        this.secretPoint = secretPoint;
    }

    private OffsetPoint secretPoint = new OffsetPoint(0, 0, 0);

    public SecretType getSecretType() {
        return secretType;
    }

    public void setSecretType(SecretType secretType) {
        this.secretType = secretType;
    }

    private SecretType secretType = SecretType.CHEST;

    public List<String> getPreRequisite() {
        return preRequisite;
    }

    private List<String> preRequisite = new ArrayList<>();

    public void tick(DungeonRoom dungeonRoom) {
        if (secretType == SecretType.CHEST) {
            BlockPos pos = secretPoint.getBlockPos(dungeonRoom);
            IBlockState blockState = BlockCache.getBlockState(pos);
            if (blockState.getBlock() == Blocks.chest || blockState.getBlock() == Blocks.trapped_chest) {
                TileEntityChest chest = (TileEntityChest) dungeonRoom.getContext().getWorld().getTileEntity(pos);
                if (chest != null) {
                    if (chest.numPlayersUsing > 0) {
                        dungeonRoom.getRoomContext().put("c-" + pos, 2);
                    } else {
                        dungeonRoom.getRoomContext().put("c-" + pos, 1);
                    }
                } else {
                    System.out.println("Expected TileEntityChest at " + pos + " to not be null");
                }
            }
        } else if (secretType == SecretType.ESSENCE) {
            Vector3i pos = secretPoint.getVector3i(dungeonRoom);
            if (BlockCache.getBlockState(pos).getBlock() == Blocks.skull) {
                dungeonRoom.getRoomContext().put("e-" + pos, true);
            }
        } else if (secretType == SecretType.ITEM_DROP) {
            Vector3d pos = new Vector3d(secretPoint.getVector3i(dungeonRoom));
            Vector3d player = VectorUtils.getPlayerVector3d();
            if (player.distanceSquared(pos) < 16) {
                Vector3d vec3 = pos.sub(player).normalize();
                for (int i = 0; i < player.distance(pos); i++) {
                    Vector3d vec = player.add(vec3.x * i, vec3.y * i, vec3.z * i);

                    IBlockState blockState = BlockCache.getBlockState(vec);
                    if (!DungeonRoom.isValidBlock(blockState))
                        return;
                }
                dungeonRoom.getRoomContext().put("i-" + pos, true);
            }
        }
    }

    public SecretStatus getSecretStatus(DungeonRoom dungeonRoom) {
        Vector3i pos = secretPoint.getVector3i(dungeonRoom);
        if (secretType == SecretType.CHEST) {

            IBlockState blockState = BlockCache.getBlockState(pos);
            if (dungeonRoom.getRoomContext().containsKey("c-" + pos))
                return ((int) dungeonRoom.getRoomContext().get("c-" + pos) == 2 || blockState.getBlock() == Blocks.air) ? SecretStatus.FOUND : SecretStatus.CREATED;

            if (blockState.getBlock() == Blocks.air) {
                return SecretStatus.DEFINITELY_NOT;
            } else if (blockState.getBlock() != Blocks.chest && blockState.getBlock() != Blocks.trapped_chest) {
                return SecretStatus.ERROR;
            } else {
                TileEntityChest chest = (TileEntityChest) dungeonRoom.getContext().getWorld().getTileEntity(VectorUtils.Vec3iToBlockPos(pos));
                if (chest != null && chest.numPlayersUsing > 0 ) {
                    return SecretStatus.FOUND;
                } else {
                    return SecretStatus.CREATED;
                }
            }
        } else if (secretType == SecretType.ESSENCE) {
            IBlockState blockState = BlockCache.getBlockState(pos);
            if (blockState.getBlock() == Blocks.skull) {
                dungeonRoom.getRoomContext().put("e-" + pos, true);
                return SecretStatus.DEFINITELY_NOT;
            } else {
                if (dungeonRoom.getRoomContext().containsKey("e-" + pos))
                    return SecretStatus.FOUND;
                return SecretStatus.NOT_SURE;
            }
        } else if (secretType == SecretType.BAT) {
            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            for (int killed : context.getKilledBats()) {
                if (context.getBatSpawnedLocations().get(killed) == null) continue;
                if (context.getBatSpawnedLocations().get(killed).distanceSquared(pos) < 100) {
                    return SecretStatus.FOUND;
                }
            }
            return SecretStatus.NOT_SURE;
        } else {
            if (dungeonRoom.getRoomContext().containsKey("i-" + pos))
                return SecretStatus.FOUND;

            Vector3d secret = secretPoint.getVector3d(dungeonRoom);
            Vector3d player = VectorUtils.getPlayerVector3d();


            if (player.distance(secret) < 16) {
                secret.sub(player).normalize();
                for (int i = 0; i < player.distance(secret); i++) {

                    player.add(secret.x * i, secret.y * i, secret.z * i);

                    IBlockState blockState = BlockCache.getBlockState(player);
                    if (!DungeonRoom.isValidBlock(blockState))
                        return SecretStatus.NOT_SURE;
                }
                dungeonRoom.getRoomContext().put("i-" + secret, true);
            }
            return SecretStatus.NOT_SURE;
        }
    }

    @Override
    public Set<AbstractAction> getAction(String state, DungeonRoom dungeonRoom) {
        if (state.equalsIgnoreCase("navigate")) {
            Set<AbstractAction> base;
            Set<AbstractAction> preRequisites = base = new HashSet<>();
            ActionMoveNearestAir actionMove = new ActionMoveNearestAir(getRepresentingPoint(dungeonRoom));
            preRequisites.add(actionMove);
            preRequisites = actionMove.getPreRequisites(null);
            for (String str : preRequisite) {
                if (!str.isEmpty()) {
                    String[] split = str.split(":");
                    ActionChangeState actionChangeState = new ActionChangeState(split[0], ActionState.valueOf(split[1]));
                    preRequisites.add(actionChangeState);
                }
            }
            return base;
        }
        if (!"found".equalsIgnoreCase(state)) {
            throw new IllegalArgumentException(state + " is not valid state for secret");
        }
        if (state.equals("found") && getSecretStatus(dungeonRoom) == SecretStatus.FOUND) return new HashSet<>();
        Set<AbstractAction> base;
        Set<AbstractAction> preRequisites = base = new HashSet<>();
        if (secretType == SecretType.CHEST || secretType == SecretType.ESSENCE) {
            ActionClick actionClick = new ActionClick(secretPoint);
            preRequisites.add(actionClick);
            preRequisites = actionClick.getPreRequisites(dungeonRoom);
        } else if (secretType == SecretType.BAT) {
            ActionKill actionKill = new ActionKill(secretPoint);
            preRequisites.add(actionKill);
            actionKill.setPredicate(EntityBat.class::isInstance);
            actionKill.setRadius(10);
            preRequisites = actionKill.getPreRequisites(null);
        }

        ActionMove actionMove = new ActionMove(secretPoint);
        preRequisites.add(actionMove);
        preRequisites = actionMove.getPreRequisites(null);

        for (String str : preRequisite) {
            if (str.isEmpty()) continue;
            ActionChangeState actionChangeState = new ActionChangeState(str.split(":")[0], ActionState.valueOf(str.split(":")[1]));
            preRequisites.add(actionChangeState);
        }

        return base;
    }

    @Override
    public void highlight(Color color, String name, DungeonRoom dungeonRoom, float partialTicks) {
        Vector3i pos = getSecretPoint().getVector3i(dungeonRoom);
        RenderUtils.highlightBlock(pos, color, partialTicks);
        RenderUtils.drawTextAtWorld(getSecretType().name(), pos.x + 0.5f, pos.x + 0.75f, pos.z + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(name, pos.x + 0.5f, pos.x + 0.375f, pos.z + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(dungeonRoom), pos.x + 0.5f, pos.x + 0f, pos.z + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
    }

    public DungeonSecret clone() throws CloneNotSupportedException {
        DungeonSecret dungeonSecret = new DungeonSecret();
        dungeonSecret.secretPoint = (OffsetPoint) secretPoint.clone();
        dungeonSecret.secretType = secretType;
        dungeonSecret.preRequisite = new ArrayList<>(preRequisite);
        return dungeonSecret;
    }

    @Override
    public String getCurrentState(DungeonRoom dungeonRoom) {
        return getSecretStatus(dungeonRoom).getStateName();
    }

    @Override
    public Set<String> getPossibleStates(DungeonRoom dungeonRoom) {
        SecretStatus status = getSecretStatus(dungeonRoom);
        if (status == SecretStatus.FOUND) return Sets.newHashSet("navigate");
        else return Sets.newHashSet("found", "navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates(DungeonRoom dungeonRoom) {
        return Sets.newHashSet("found"/*, "definitely_not", "not_sure", "created", "error"*/);
    }

    @Override
    public OffsetPoint getRepresentingPoint(DungeonRoom dungeonRoom) {
        return secretPoint;
    }

    public enum SecretType {
        BAT, CHEST, ITEM_DROP, ESSENCE
    }

    @AllArgsConstructor
    @Getter
    public enum SecretStatus {
        DEFINITELY_NOT("definitely_not"), NOT_SURE("not_sure"), CREATED("created"), FOUND("found"), ERROR("error");

        private final String stateName;
    }
}

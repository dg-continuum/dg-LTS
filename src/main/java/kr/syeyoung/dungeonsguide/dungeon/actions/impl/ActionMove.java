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

package kr.syeyoung.dungeonsguide.dungeon.actions.impl;

import cc.polyfrost.oneconfig.libs.universal.UKeyboard;
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction;
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRouteProperties;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.VectorUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Data
@EqualsAndHashCode(callSuper = false)
public class ActionMove extends AbstractAction {
    private Set<AbstractAction> preRequisite = new HashSet<>();
    private OffsetPoint target;
    private int tick = -1;
    private List<Vector3d> poses;
    private Future<List<Vector3d>> latestFuture;
    public ActionMove(OffsetPoint target) {
        this.target = target;
    }

    static void draw(DungeonRoom dungeonRoom, float partialTicks, ActionRouteProperties actionRouteProperties, boolean flag, OffsetPoint target, List<Vector3d> poses) {
        Vector3i pos = target.getVector3i(dungeonRoom);

        float distance = (float) pos.distance(VectorUtils.getPlayerVector3i());
        float multiplier = distance / 120f; //mobs only render ~120 blocks away
        if (flag) multiplier *= 2.0f;
        float scale = 0.45f * multiplier;
        scale *= 25.0 / 6.0;
        if (actionRouteProperties.isBeacon()) {
            if (DgOneCongifConfig.renderSecretBeacons) {
                RenderUtils.renderBeaconBeam(pos.x, pos.y, pos.z, actionRouteProperties.getBeaconBeamColor(), partialTicks);
            }
            RenderUtils.highlightBlock(pos, actionRouteProperties.getBeaconColor(), partialTicks);
        }
        if (DgOneCongifConfig.renderSecretDestText) {
            RenderUtils.drawTextAtWorld("Destination", pos.x + 0.5f, pos.y + 0.5f + scale, pos.z + 0.5f, 0xFF00FF00, flag ? 2f : 1f, true, false, partialTicks);
        }
        RenderUtils.drawTextAtWorld(String.format("%.2f", MathHelper.sqrt_double(pos.distance(VectorUtils.getPlayerVector3i()))) + "m", pos.x + 0.5f, pos.y + 0.5f - scale, pos.z + 0.5f, 0xFFFFFF00, flag ? 2f : 1f, true, false, partialTicks);

        if ((!DgOneCongifConfig.togglePathfindKeybind.getKeyBinds().isEmpty() && DgOneCongifConfig.togglePathfindKeybind.getKeyBinds().get(0) != UKeyboard.KEY_NONE) || !DgOneCongifConfig.togglePathfindStatus) {
            RenderUtils.drawLinesVec3(poses, actionRouteProperties.getLineColor(), actionRouteProperties.getLineWidth(), partialTicks, true);
        }

    }

    public OffsetPoint getTarget() {
        return target;
    }

    @Override
    public Set<AbstractAction> getPreRequisites(DungeonRoom dungeonRoom) {
        return preRequisite;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return target.getVector3i(dungeonRoom).distance(VectorUtils.getPlayerVector3i()) < 25;
    }

    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks, ActionRouteProperties actionRouteProperties, boolean flag) {
        draw(dungeonRoom, partialTicks, actionRouteProperties, flag, target, poses);
    }

    @Override
    public void onTick(DungeonRoom dungeonRoom, ActionRouteProperties actionRouteProperties) {
        tick = (tick + 1) % Math.max(1, actionRouteProperties.getLineRefreshRate());
        if (latestFuture != null && latestFuture.isDone()) {
            try {
                poses = latestFuture.get();
                latestFuture = null;
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        if (tick == 0 && actionRouteProperties.isPathfind() && latestFuture == null) {
            if (!DgOneCongifConfig.freezePathfindingStatus || poses == null) {
                latestFuture = dungeonRoom.createEntityPathTo(Minecraft.getMinecraft().thePlayer, target.getVector3i(dungeonRoom));
            }
        }
    }

    public void forceRefresh(DungeonRoom dungeonRoom) {
        if (latestFuture == null) {
            latestFuture = dungeonRoom.createEntityPathTo(Minecraft.getMinecraft().thePlayer, target.getVector3i(dungeonRoom));
        }
    }

    @Override
    public String toString() {
        return "Move\n- target: " + target.toString();
    }
}

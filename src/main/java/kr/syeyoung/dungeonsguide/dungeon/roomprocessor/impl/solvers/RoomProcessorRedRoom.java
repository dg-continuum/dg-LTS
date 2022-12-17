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

package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.solvers;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonDoor;
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.FeatureWarningOnPortal;
import kr.syeyoung.dungeonsguide.features.text.StyledTextRenderer;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.MathHelper;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3f;

public class RoomProcessorRedRoom extends GeneralRoomProcessor {
    Vector3d basePt;
    int dir = 0;
    public RoomProcessorRedRoom(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
        Vector3i basePt = dungeonRoom.getMin().add(dungeonRoom.getMax());
        this.basePt = new Vector3d(basePt.x / 2.0f, basePt.y / 2.0f, basePt.z / 2.0f);
    }

    @Override
    public void tick() {
        Vector3i basePt = getDungeonRoom().getMin().add(getDungeonRoom().getMax());
        this.basePt = new Vector3d(basePt.x / 2.0f, basePt.y / 2.0f + 4, basePt.z / 2.0f);
        DungeonDoor real = null;
        for (DungeonDoor door : getDungeonRoom().getDoors()) {
            if (door.getType().isExist()) {
                real = door;
                break;
            }
        }
        if (real != null) {
            OffsetPoint offsetPoint = new OffsetPoint(getDungeonRoom(), new Vector3i(real.getPosition()));
            offsetPoint = new OffsetPoint(33 - offsetPoint.getX(), offsetPoint.getY(), 33 - offsetPoint.getZ());
            Vector3i opposite = offsetPoint.getVector3i(getDungeonRoom());
            Vector3i dir = new Vector3i(real.getPosition()).sub(opposite);
            dir = new Vector3i(MathHelper.clamp_int(dir.x / 10, -1, 1), 0, MathHelper.clamp_int(dir.z / 10, -1, 1));

            this.basePt = new Vector3d(opposite.add(dir.x * 6 + dir.z, 3, dir.z * 6 - dir.x));

            if (dir.x > 0) this.dir = 270;
            else if (dir.x < 0) this.dir = 90;
            else if (dir.z < 0) this.dir = 0;
            else if (dir.z > 0) this.dir = 180;
            else this.dir = Integer.MIN_VALUE;
        } else {
            dir = Integer.MIN_VALUE;
        }
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!FeatureRegistry.BOSSFIGHT_WARNING_ON_PORTAL.isEnabled()) return;


        FeatureWarningOnPortal featureWarningOnPortal = FeatureRegistry.BOSSFIGHT_WARNING_ON_PORTAL;
        {
            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

            Vector3f renderPos = RenderUtils.getRenderPos((float) basePt.x, (float) basePt.y, (float) basePt.z, partialTicks);

            GlStateManager.color(1f, 1f, 1f, 0.5f);
            GlStateManager.pushMatrix();
            GlStateManager.translate(renderPos.x, renderPos.y, renderPos.z);
            if (dir == Integer.MIN_VALUE)
                GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
            else
                GlStateManager.rotate(dir, 0.0f, 1.0f, 0.0f);
            GlStateManager.scale(-0.05f, -0.05f, 0.05f);
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);


            StyledTextRenderer.drawTextWithStylesAssociated(featureWarningOnPortal.getText(), 0, 0, 0, featureWarningOnPortal.getStylesMap(), StyledTextRenderer.Alignment.LEFT);

            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }
    }
}

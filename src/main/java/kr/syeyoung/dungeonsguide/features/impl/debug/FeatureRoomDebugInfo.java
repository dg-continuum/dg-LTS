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

package kr.syeyoung.dungeonsguide.features.impl.debug;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.utils.VectorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.apache.commons.lang3.StringUtils;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public class FeatureRoomDebugInfo extends GuiFeature {
    public FeatureRoomDebugInfo() {
        super("Debug", "Display Room Debug Info", "ONLY WORKS WITH SECRET SETTING", "advanced.debug.roominfo", false, getFontRenderer().getStringWidth("longestplayernamepos: 100"), getFontRenderer().FONT_HEIGHT * 6);
//        this.setEnabled(false);
//        addParameter("color", new FeatureParameter<Color>("color", "Color", "Color of text", Color.white, "color", nval ->));
    }


    @Override
    public boolean isEnabled() {
        return DgOneCongifConfig.debugRoomInfo;
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
    @Override
    public void drawHUD(float partialTicks) {
        if (!skyblockStatus.isOnDungeon()) return;
        if (!DgOneCongifConfig.debugMode) return;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return;
        Vector2i roomPt = context.getMapProcessor().worldPointToRoomPoint(VectorUtils.getPlayerVector3i());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        if (dungeonRoom == null) {
            if (context.getBossfightProcessor() == null) {
                fontRenderer.drawString("Where are you?!", 0, 0, 0xFFFFFF);
            } else {
                fontRenderer.drawString("You're prob in bossfight", 0, 0, DgOneCongifConfig.dungeonRoominfoColor.getRGB());
                fontRenderer.drawString("processor: "+context.getBossfightProcessor(), 0, 10, DgOneCongifConfig.dungeonRoominfoColor.getRGB());
                fontRenderer.drawString("phase: "+context.getBossfightProcessor().getCurrentPhase(), 0, 20, DgOneCongifConfig.dungeonRoominfoColor.getRGB());
                fontRenderer.drawString("nextPhase: "+ StringUtils.join(context.getBossfightProcessor().getNextPhases(), ","), 0, 30, DgOneCongifConfig.dungeonRoominfoColor.getRGB());
                fontRenderer.drawString("phases: "+ StringUtils.join(context.getBossfightProcessor().getPhases(), ","), 0, 40, DgOneCongifConfig.dungeonRoominfoColor.getRGB());
            }
        } else {
                fontRenderer.drawString("you're in the room... color/shape/rot " + dungeonRoom.getColor() + " / " + dungeonRoom.getShape() + " / "+dungeonRoom.getRoomMatcher().getRotation(), 0, 0, DgOneCongifConfig.dungeonRoominfoColor.getRGB());
                fontRenderer.drawString("room uuid: " + dungeonRoom.getDungeonRoomInfo().getUuid() + (dungeonRoom.getDungeonRoomInfo().isRegistered() ? "" : " (not registered)"), 0, 10, DgOneCongifConfig.dungeonRoominfoColor.getRGB());
                fontRenderer.drawString("room name: " + dungeonRoom.getDungeonRoomInfo().getName(), 0, 20, DgOneCongifConfig.dungeonRoominfoColor.getRGB());
                fontRenderer.drawString("room state / max secret: " + dungeonRoom.getCurrentState() + " / "+dungeonRoom.getTotalSecrets(), 0, 30, DgOneCongifConfig.dungeonRoominfoColor.getRGB());

        }
    }

    @Override
    public void drawDemo(float partialTicks) {
        FontRenderer fr = getFontRenderer();

        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        fr.drawString("Line 1", 0,0, DgOneCongifConfig.dungeonRoominfoColor.getRGB());
        fr.drawString("Line 2", 0,10, DgOneCongifConfig.dungeonRoominfoColor.getRGB());
        fr.drawString("Line 3", 0,20, DgOneCongifConfig.dungeonRoominfoColor.getRGB());
        fr.drawString("Line 4", 0,30, DgOneCongifConfig.dungeonRoominfoColor.getRGB());
        fr.drawString("Line 5", 0,40, DgOneCongifConfig.dungeonRoominfoColor.getRGB());
    }

}

/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.features.impl.dungeon.secret.mechanicbrowser;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.config.guiconfig.location.GuiGuiLocationConfig;
import kr.syeyoung.dungeonsguide.config.types.GUIRectangle;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MFloatSelectionButton;
import kr.syeyoung.dungeonsguide.gui.elements.MPassiveLabelAndElement;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.utils.VectorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.joml.Vector2i;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class FeatureMechanicBrowse extends GuiFeature {
    private MGuiMechanicBrowser mGuiMechanicBrowser;
    private int lastWidth, lastHeight;

    public FeatureMechanicBrowse() {
        super("Dungeon.Secrets.Secret Browser", "Secret Browser", "Browse and Pathfind secrets and mechanics in the current room", "secret.mechanicbrowse", false, 100, 300);
        addParameter("scale", new FeatureParameter<Float>("scale", "Scale", "Scale", 1.0f, "float"));
        mGuiMechanicBrowser = new MGuiMechanicBrowser(this);
        mGuiMechanicBrowser.setWorldAndResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        lastWidth = Minecraft.getMinecraft().displayWidth;
        lastHeight = Minecraft.getMinecraft().displayHeight;
        MinecraftForge.EVENT_BUS.register(this);
    }

    public double getScale() {
        return this.<Float>getParameter("scale").getValue();
    }

    @Override
    public void drawDemo(float partialTicks) {
        super.drawDemo(partialTicks);
        double scale = FeatureMechanicBrowse.this.<Float>getParameter("scale").getValue();
        GlStateManager.scale(scale, scale, 1.0);

        Dimension bigDim = getFeatureRect().getRectangleNoScale().getSize();
        Dimension effectiveDim = new Dimension((int) (bigDim.width / scale), (int) (bigDim.height / scale));

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        Gui.drawRect(0, 0, effectiveDim.width, fr.FONT_HEIGHT + 4, 0xFF444444);
        Gui.drawRect(1, 1, effectiveDim.width - 1, fr.FONT_HEIGHT + 3, 0xFF262626);
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        fr.drawString("Selected: ", 2, 2, 0xFFAAAAAA);
        fr.drawString("Nothing", fr.getStringWidth("Selected: ") + 2, 2, 0xFFAA0000);
        fr.drawString("Open Chat to Select Secrets", 2, fr.FONT_HEIGHT + 5, 0xFFAAAAAA);
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post postRender) {
        if (!(postRender.type == RenderGameOverlayEvent.ElementType.EXPERIENCE || postRender.type == RenderGameOverlayEvent.ElementType.JUMPBAR)) return;

        if (!SkyblockStatus.isOnSkyblock()) return;

        if (!isEnabled()) return;
        int i = Mouse.getEventX();
        int j = Minecraft.getMinecraft().displayHeight - Mouse.getEventY();
        if (Minecraft.getMinecraft().displayWidth != lastWidth || Minecraft.getMinecraft().displayHeight != lastHeight)
            mGuiMechanicBrowser.initGui();
        lastWidth = Minecraft.getMinecraft().displayWidth;
        lastHeight = Minecraft.getMinecraft().displayHeight;
        mGuiMechanicBrowser.drawScreen(i, j, postRender.partialTicks);

        GlStateManager.enableBlend();
    }


    @Override
    public void setFeatureRect(GUIRectangle featureRect) {
        super.setFeatureRect(featureRect);
        mGuiMechanicBrowser.initGui();
    }

    @Override
    public void drawHUD(float partialTicks) {
        // TODO document why this method is empty
    }


    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGH)
    public void onGuiEvent(GuiScreenEvent.MouseInputEvent.Pre input) {

        if (!isEnabled()) return;
        try {
            mGuiMechanicBrowser.handleMouseInput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @SubscribeEvent
    public void onGuiRender(GuiScreenEvent.DrawScreenEvent.Pre render) {
        if (!SkyblockStatus.isOnSkyblock()) return;

        if (!isEnabled()) return;
        int i = Mouse.getEventX();
        int j = Minecraft.getMinecraft().displayHeight - Mouse.getEventY();
        mGuiMechanicBrowser.drawScreen(i, j, render.renderPartialTicks);
        GlStateManager.enableBlend();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent postRender) {
        if (!SkyblockStatus.isOnSkyblock()) return;
        if (!isEnabled()) return;
        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
        if (!skyblockStatus.isOnDungeon()) return;
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null || !DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getMapProcessor().isInitialized())
            return;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        Vector2i roomPt = context.getMapProcessor().worldPointToRoomPoint(VectorUtils.getPlayerVector3i());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        if (dungeonRoom == null) return;
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;
        String id = mGuiMechanicBrowser.getPanelMechanicBrowser().getSelectedID();
        if (id != null) {
            Optional.ofNullable(dungeonRoom.getMechanics().get(mGuiMechanicBrowser.getPanelMechanicBrowser().getSelectedID()))
                    .ifPresent(a -> {
                        a.highlight(new Color(0, 255, 255, 50), id + " (" + (
                                dungeonRoom.getMechanics().get(id).getRepresentingPoint(dungeonRoom) != null ?
                                        String.format("%.1f", MathHelper.sqrt_double((dungeonRoom.getMechanics().get(id)).getRepresentingPoint(dungeonRoom).getVector3i(dungeonRoom).distanceSquared(VectorUtils.getPlayerVector3i()))) : "")
                                + "m)", dungeonRoom, postRender.partialTicks);
                    });
        }
    }

    @Override
    public List<MPanel> getTooltipForEditor(GuiGuiLocationConfig guiGuiLocationConfig) {
        List<MPanel> mPanels = super.getTooltipForEditor(guiGuiLocationConfig);

        mPanels.add(new MPassiveLabelAndElement("Scale", new MFloatSelectionButton(FeatureMechanicBrowse.this.<Float>getParameter("scale").getValue()) {
            {
                setOnUpdate(() -> {
                    FeatureMechanicBrowse.this.<Float>getParameter("scale").setValue(this.getData());
                    mGuiMechanicBrowser.initGui();
                });
            }
        }));

        return mPanels;
    }
}

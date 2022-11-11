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

package kr.syeyoung.dungeonsguide.mod.features.impl.boss.terminal;

import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeatureV2;
import kr.syeyoung.dungeonsguide.mod.onconfig.DgOneCongifConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

public class FeatureTerminalSolvers extends SimpleFeatureV2 {
    public static final List<TerminalSolutionProvider> solutionProviders = new ArrayList<>();

    static {
        solutionProviders.add(new WhatStartsWithSolutionProvider());
        solutionProviders.add(new SelectAllColorSolutionProivider());
        solutionProviders.add(new SelectInOrderSolutionProvider());
        solutionProviders.add(new NavigateMazeSolutionProvider());
        solutionProviders.add(new CorrectThePaneSolutionProvider());
    }

    private final List<Slot> clicked = new ArrayList<>();
    private TerminalSolutionProvider solutionProvider;
    private TerminalSolution solution;

    public FeatureTerminalSolvers() {
        super("bossfight.terminals");
    }

    @SubscribeEvent
    public void dungeonTooltip(ItemTooltipEvent event) {
        if (!SkyblockStatus.isOnSkyblock()) return;
        if (!DgOneCongifConfig.terminalSolver) return;
        if (solutionProvider == null) return;
        event.toolTip.clear();

    }

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGH)
    public void onPreMouse(GuiScreenEvent.MouseInputEvent.Pre e) {
        if (!SkyblockStatus.isOnSkyblock()) return;
        if (!DgOneCongifConfig.terminalSolver) return;
        if (Mouse.getEventButton() == -1) return;
        if (solutionProvider == null) return;
        if (solution == null) return;
        if (solution.getCurrSlots() == null) {
            return;
        }
        Slot s = ((GuiChest) Minecraft.getMinecraft().currentScreen).getSlotUnderMouse();
        if (solution.getCurrSlots().contains(s)) {
            clicked.add(s);
        }

    }

    @SubscribeEvent
    public void onGuiRender(GuiScreenEvent.DrawScreenEvent.Post render) {
        if (stateChecks()) return;

        if (solution != null) {
            prepareDrawing(render);
            if (solution.getCurrSlots() != null) {
                for (Slot currSlot : solution.getCurrSlots()) {
                    int x = currSlot.xDisplayPosition;
                    int y = currSlot.yDisplayPosition;
                    Gui.drawRect(x, y, x + 16, y + 16, 0x7700FFFF);
                }
            }
            if (solution.getNextSlots() != null) {
                for (Slot nextSlot : solution.getNextSlots()) {
                    int x = nextSlot.xDisplayPosition;
                    int y = nextSlot.yDisplayPosition;
                    Gui.drawRect(x, y, x + 16, y + 16, 0x77FFFF00);
                }
            }
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.popMatrix();
        }
        GlStateManager.enableBlend();
        GlStateManager.enableLighting();
    }

    public static void prepareDrawing(GuiScreenEvent.DrawScreenEvent.Post render) {
        int i = 222;
        int j = i - 108;
        int ySize = j + (((ContainerChest) (((GuiChest) Minecraft.getMinecraft().currentScreen).inventorySlots)).getLowerChestInventory().getSizeInventory() / 9) * 18;
        int left = (render.gui.width - 176) / 2;
        int top = (render.gui.height - ySize) / 2;
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.colorMask(true, true, true, false);
        GlStateManager.translate(left, top, 0);
    }

    private boolean stateChecks() {
        if (!SkyblockStatus.isOnSkyblock()) return true;

        if (!DgOneCongifConfig.terminalSolver) return true;
        if (solutionProvider == null) return true;
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
            solution = null;
            solutionProvider = null;
            clicked.clear();
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onGuiOpenn(GuiOpenEvent tick) {
        if (!SkyblockStatus.isOnSkyblock()) return;
        if (!DgOneCongifConfig.terminalSolver) return;
        solution = null;
        solutionProvider = null;
        clicked.clear();
        if (tick.gui instanceof GuiChest) {
            ContainerChest cc = (ContainerChest) ((GuiChest) tick.gui).inventorySlots;
            for (TerminalSolutionProvider solutionProvider1 : solutionProviders) {
                if (solutionProvider1.isApplicable(cc)) {
                    solution = solutionProvider1.provideSolution(cc, clicked);
                    this.solutionProvider = solutionProvider1;
                }
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick) {
        if (tick.phase == TickEvent.Phase.END && tick.type == TickEvent.Type.CLIENT) {
            if (stateChecks()) return;
            ContainerChest cc = (ContainerChest) ((GuiChest) Minecraft.getMinecraft().currentScreen).inventorySlots;

            solution = solutionProvider.provideSolution(cc, clicked);

        }
    }


}

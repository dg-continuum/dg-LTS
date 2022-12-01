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

package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.features.SimpleFeatureV2;
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

public class FeaturePressAnyKeyToCloseChest extends SimpleFeatureV2 {
    public FeaturePressAnyKeyToCloseChest() {
        super("dungeon.presskeytoclose");
    }

    @SubscribeEvent
    public void onKey(GuiScreenEvent.KeyboardInputEvent event) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (!DgOneCongifConfig.closeChestHelper) return;
        if (!SkyblockStatus.isOnDungeon()) return;

        a(screen);
    }

    private void a(GuiScreen screen) {
        if (screen instanceof GuiChest) {
            ContainerChest ch = (ContainerChest) ((GuiChest) screen).inventorySlots;
            if (!("Large Chest".equals(ch.getLowerChestInventory().getName())
                    || "Chest".equals(ch.getLowerChestInventory().getName()))) return;

            Minecraft.getMinecraft().thePlayer.closeScreen();
        }
    }

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGH)
    public void onGuiEvent(GuiScreenEvent.MouseInputEvent.Pre input) {
        if (!SkyblockStatus.isOnSkyblock()) return;

        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (!DgOneCongifConfig.closeChestHelper) return;
        if (!SkyblockStatus.isOnDungeon()) return;
        if (Mouse.getEventButton() == -1) return;

        a(screen);
    }

}

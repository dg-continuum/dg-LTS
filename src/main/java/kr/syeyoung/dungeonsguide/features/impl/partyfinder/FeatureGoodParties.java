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

package kr.syeyoung.dungeonsguide.features.impl.partyfinder;

import kr.syeyoung.dungeonsguide.features.SimpleFeatureV2;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.boss.f7.FeatureTerminalSolvers;
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public class FeatureGoodParties extends SimpleFeatureV2 {
    public FeatureGoodParties() {
        super("partykicker.goodparty");
    }

    @SubscribeEvent
    public void onGuiRender(GuiScreenEvent.DrawScreenEvent.Post render) {
        if (!SkyblockStatus.isOnSkyblock()) return;
        if (!DgOneCongifConfig.featureGoodParties) return;
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;
        GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
        ContainerChest cont = (ContainerChest) chest.inventorySlots;
        String name = cont.getLowerChestInventory().getName();
        if (!"Party Finder".equals(name)) return;


        FeatureTerminalSolvers.prepareDrawing(render);
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        try {

            for (int i1 = 0; i1 < Integer.min(54, cont.inventorySlots.size()); i1++) {
                Slot s = cont.inventorySlots.get(i1);
                if (s.getStack() == null) continue;
                if (s.getStack().getItem() != Items.skull) continue;
                NBTTagCompound nbt = s.getStack().getTagCompound();
                if (nbt == null || nbt.hasNoTags()) continue;
                NBTTagCompound display = nbt.getCompoundTag("display");
                if (display.hasNoTags()) return;
                NBTTagList lore = display.getTagList("Lore", 8);
                int classLvReq = 0;
                int cataLvReq = 0;
                boolean Req = false;
                String note = "";
                for (int n = 0; n < lore.tagCount(); n++) {
                    String str = lore.getStringTagAt(n);
                    if (str.startsWith("§7Dungeon Level Required: §b")) cataLvReq = Integer.parseInt(str.substring(28));
                    if (str.startsWith("§7Class Level Required: §b")) classLvReq = Integer.parseInt(str.substring(26));
                    if (str.startsWith("§7§7Note:")) note = TextUtils.stripColor(str.substring(10));
                    if (str.startsWith("§cRequires")) Req = true;
                }

                int x = s.xDisplayPosition;
                int y = s.yDisplayPosition;
                if (Req) {
                    Gui.drawRect(x, y, x + 16, y + 16, 0x77AA0000);
                } else {

                    GlStateManager.enableBlend();
                    GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    String s1 = note.toLowerCase();

                    x += 1;
                    y += 1;

                    if (s1.contains("car")) {
                        fr.drawStringWithShadow("C", x, y, 0xFFFF0000);
                    } else if (s1.replace(" ", "").contains("s/s+")) {
                        fr.drawStringWithShadow("S+", x, y, 0xFFFFFF00);
                    } else if (s1.contains("s+")) {
                        fr.drawStringWithShadow("S+", x, y, 0xFF00FF00);
                    } else if (s1.contains(" s") || s1.contains(" s ")) {
                        fr.drawStringWithShadow("S", x, y, 0xFFFFFF00);
                    } else if (s1.contains("rush")) {
                        fr.drawStringWithShadow("R", x, y, 0xFFFF0000);
                    }
                    fr.drawStringWithShadow("§e" + Integer.max(classLvReq, cataLvReq), x, y + fr.FONT_HEIGHT, 0xFFFFFFFF);
                }


            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableLighting();
    }

}

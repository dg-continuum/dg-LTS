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

package kr.syeyoung.dungeonsguide.gui.elements;

import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;

@Getter
@Setter
public class MButton extends MPanel {
    private String text;

    private Color foreground = Color.white;
    private int background = RenderUtils.blendAlpha(0xFF141414, 0.08f);
    private int hover =  RenderUtils.blendAlpha(0xFF141414, 0.14f);
    private int clicked =  RenderUtils.blendAlpha(0xFF141414, 0.16f);
    private int border = 0x0;
    private int disabled =0xFF141414;
    private int roundness = 0;

    private boolean enabled = true;

    private Runnable onActionPerformed;

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle clip) {
        Dimension bounds = getSize();

        int bg = background;
        if (!enabled) {
            bg = disabled;
        } else if (getTooltipsOpen() > 0) {
        } else if (isclicked) {
            bg = clicked;
        } else if (new Rectangle(new Point(0,0),bounds).contains(relMousex0, relMousey0)) {
            bg = hover;
        }
        if (roundness == 0) {
            if (((border >> 24) & 0xFF) == 0)
                Gui.drawRect(0, 0, getBounds().width, getBounds().height, bg);
            else {
                Gui.drawRect(0, 0, getBounds().width, getBounds().height, border);
                Gui.drawRect(1, 1, getBounds().width - 1, getBounds().height - 1, bg);
            }
        } else {
            if (((border >> 24) & 0xFF) == 0)
                RenderUtils.drawRoundedRectangle(0, 0, getBounds().width, getBounds().height, roundness, Math.PI/8, bg);
            else {
                RenderUtils.drawRoundedRectangle(0, 0, getBounds().width, getBounds().height, roundness, Math.PI/8, border);
                RenderUtils.drawRoundedRectangle(1, 1, getBounds().width-2, getBounds().height-2, roundness, Math.PI/8,  bg);
            }
            GlStateManager.enableTexture2D();
        }
        FontRenderer renderer = Minecraft.getMinecraft().fontRendererObj;
        int width = renderer.getStringWidth(getText());
        int x = (getBounds().width - width) / 2;
        int y = (getBounds().height - renderer.FONT_HEIGHT) / 2 + 1;

        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        renderer.drawString(getText(), x,y, foreground.getRGB());
    }

    boolean isclicked = false;
    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (lastAbsClip.contains(absMouseX, absMouseY) && getTooltipsOpen() == 0) {
            isclicked = true;
            if (onActionPerformed != null)
                onActionPerformed.run();
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        }
    }

    @Override
    public void mouseReleased(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int state) {
        isclicked = false;
    }


    @Override
    public void mouseMoved(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0) {
        if (lastAbsClip.contains(absMouseX, absMouseY) && enabled){

        }
    }

}

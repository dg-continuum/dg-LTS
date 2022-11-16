package kr.syeyoung.dungeonsguide.features.impl.debug;

import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.hud.BasicHud;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public class TestMap extends BasicHud {
    @Exclude
    transient static final Logger logger = LogManager.getLogger("TestMap");

    @Override
    protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {

        GlStateManager.pushMatrix();

        GlStateManager.translate(position.getX(), position.getY(), 0);

//        // this line (is taking) -took- 6 hours of hair silvering trial and error btw
        int height = ((int) (getHeight(scale, false) + y) * 2);


//        int width = (int) ((int) ((getWidth(scale, false) + x)) * 1.5);
        int width = (int) ((int) (((getWidth(scale, false)) + x)) );
        GL11.glScissor((int) x, (int) (Minecraft.getMinecraft().displayHeight - y - height), width, height);

        if(!example){
            logger.info("X: {}, MOD WIDTH: {}, CALC WIDHT: {}", x, getWidth(scale, false), width);

            logger.info("Y: {}, MOD HEIGHT: {}, CALC HEIGH: {}, Y GIVEN: {}", y, getHeight(scale, false), height, Minecraft.getMinecraft().displayHeight - y - height);
        }


//        GL11.glScissor((int) x, (int) (Minecraft.getMinecraft().displayHeight - y - getHeight(scale, false)), (int) ((int) ((getWidth(scale, false) * 2) + x)), (int) ((int) ((getHeight(scale, false) * 2) + y) + (getHeight(scale, false) / 2)));

        GL11.glEnable(GL11.GL_SCISSOR_TEST);


        GlStateManager.color(1,1,1,1);
        Gui.drawRect(0,0, (int) getHeight(scale, false) + 200, (int) getWidth(scale, false),0xFFFFFFFF);
        Gui.drawRect((int) getHeight(scale, false) / 2,(int) getWidth(scale, false) / 2, ((int) getHeight(scale, false) / 2) + 2, ((int) getWidth(scale, false) / 2) + 2,0xFF0000FF);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GlStateManager.popMatrix();


    }

    @Override
    protected float getWidth(float scale, boolean example) {
        return 128 * scale;
    }

    @Override
    protected float getHeight(float scale, boolean example) {
        return 128 * scale;
    }
}

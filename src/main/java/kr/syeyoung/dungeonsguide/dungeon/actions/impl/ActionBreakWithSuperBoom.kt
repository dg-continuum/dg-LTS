package kr.syeyoung.dungeonsguide.dungeon.actions.impl

import kr.syeyoung.dungeonsguide.dungeon.DungeonFacade
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionPlanProperties
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import kr.syeyoung.dungeonsguide.utils.VectorUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.init.Blocks
import java.awt.Color


class ActionBreakWithSuperBoom(private val target: OffsetPoint) : AbstractAction() {

    override fun isComplete(dungeonRoom: DungeonRoom): Boolean {
        DungeonFacade.context?.let {
            val thing = target.getVector3i(dungeonRoom)
            for (el in it.expositions) {
                if (thing.distance(el) < 5) {
                    return true
                }
            }
        }

        return false
    }

    override fun onRenderWorld(
        dungeonRoom: DungeonRoom,
        partialTicks: Float,
        actionPlanProperties: ActionPlanProperties?,
        flag: Boolean
    ) {
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture)
        val blockpos = target.getVector3i(dungeonRoom)
        val viewingFrom = Minecraft.getMinecraft().renderViewEntity
        val x = viewingFrom.lastTickPosX + (viewingFrom.posX - viewingFrom.lastTickPosX) * partialTicks
        val y = viewingFrom.lastTickPosY + (viewingFrom.posY - viewingFrom.lastTickPosY) * partialTicks
        val z = viewingFrom.lastTickPosZ + (viewingFrom.posZ - viewingFrom.lastTickPosZ) * partialTicks
        GlStateManager.pushMatrix()
        GlStateManager.translate(-x, -y, -z)
        GlStateManager.disableLighting()
        GlStateManager.enableAlpha()
        GlStateManager.disableDepth()
        GlStateManager.depthMask(false)
        GlStateManager.enableBlend()
        val tessellator = Tessellator.getInstance()
        val vertexbuffer = tessellator.worldRenderer
        vertexbuffer.begin(7, DefaultVertexFormats.BLOCK)
        val blockrendererdispatcher = Minecraft.getMinecraft().blockRendererDispatcher
        blockrendererdispatcher.blockModelRenderer.renderModel(
            Minecraft.getMinecraft().theWorld,
            blockrendererdispatcher.blockModelShapes.getModelForState(Blocks.tnt.defaultState),
            Blocks.tnt.defaultState, VectorUtils.Vec3iToBlockPos(blockpos), vertexbuffer, false
        )
        tessellator.draw()
        GlStateManager.enableLighting()
        GlStateManager.popMatrix()
        RenderUtils.highlightBlock(blockpos, Color(0, 255, 255, 50), partialTicks, true)
        RenderUtils.drawTextAtWorld(
            "Superboom",
            blockpos.x + 0.5f,
            blockpos.y + 0.5f,
            blockpos.z + 0.5f,
            -0x100,
            0.03f,
            false,
            false,
            partialTicks
        )
    }

    override fun toString(): String {
        return "BreakWithSuperboom\n- target: $target"
    }
}
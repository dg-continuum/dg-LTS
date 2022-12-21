package kr.syeyoung.dungeonsguide.dungeon.test

import kr.syeyoung.dungeonsguide.dungeon.mechanics.impl.DungeonSecret
import kr.syeyoung.dungeonsguide.dungeon.room.NewDungeonRoom
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

class RoomMechanicsDebug {
    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    companion object {
        var visualiseRoom: NewDungeonRoom? = null
        var INSTANCE: RoomMechanicsDebug? = null
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (visualiseRoom == null) return


        for ((name, mech) in visualiseRoom!!.mechanics){
            if (mech is DungeonSecret){
                val pos = mech.secretPoint.getVector3i(visualiseRoom!!)
                RenderUtils.highlightBlock(pos, Color(0, 255, 0, 50), event.partialTicks)

                RenderUtils.drawTextAtWorld(
                    name, pos.x + 0.5f, pos.x + 0.375f, pos.z + 0.5f, -0x1, 0.03f, false, true, event.partialTicks
                )
            }
        }

        GlStateManager.color(1f, 1f, 1f, 1f)
    }

}
package kr.syeyoung.dungeonsguide.dungeon.actions.impl

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionPlanProperties
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.entity.item.EntityItem
import net.minecraft.util.AxisAlignedBB
import java.awt.Color
import java.util.function.Predicate

class ActionDropItem(private val target: OffsetPoint) : AbstractAction() {
    private val predicate = Predicate { _: EntityItem? -> true }

    override fun isComplete(dungeonRoom: DungeonRoom): Boolean {
        val secretLocation = target.getVector3i(dungeonRoom)
        val item = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(
            EntityItem::class.java,
            AxisAlignedBB.fromBounds(
                secretLocation.x.toDouble(),
                secretLocation.y.toDouble(),
                secretLocation.z.toDouble(),
                (
                        secretLocation.x + 1).toDouble(),
                (
                        secretLocation.y + 1).toDouble(),
                (
                        secretLocation.z + 1).toDouble()
            )
        )
        return item.isNotEmpty()

    }

    override fun onRenderWorld(
        dungeonRoom: DungeonRoom,
        partialTicks: Float,
        actionPlanProperties: ActionPlanProperties?,
        flag: Boolean,
    ) {
        val pos = target.getVector3i(dungeonRoom)
        RenderUtils.highlightBlock(pos, Color(0, 255, 255, 50), partialTicks, true)
        RenderUtils.drawTextAtWorld(
            "Drop Item",
            pos.x + 0.5f,
            pos.y + 0.3f,
            pos.z + 0.5f,
            -0x100,
            0.02f,
            false,
            false,
            partialTicks
        )

    }

    override fun toString(): String {
        return """
            DropItem
            - target: $target
            - predicate: ${predicate.javaClass.simpleName}
            """.trimIndent()
    }
}
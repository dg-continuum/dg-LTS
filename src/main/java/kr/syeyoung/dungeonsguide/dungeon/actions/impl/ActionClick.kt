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
package kr.syeyoung.dungeonsguide.dungeon.actions.impl

import kr.syeyoung.dungeonsguide.chat.ChatTransmitter
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRouteProperties
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import kr.syeyoung.dungeonsguide.utils.VectorUtils
import net.minecraft.item.ItemStack
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import java.awt.Color
import java.util.function.Predicate

class ActionClick(private val target: OffsetPoint) : AbstractAction() {
    private val predicate = Predicate { _: ItemStack? -> true }
    private var clicked = false

    override fun isComplete(dungeonRoom: DungeonRoom?): Boolean {
        return clicked
    }

    override fun onPlayerInteract(
        dungeonRoom: DungeonRoom?,
        event: PlayerInteractEvent?,
        actionRouteProperties: ActionRouteProperties?
    ) {
        if (clicked) return
        if (event?.pos != null){
            if (target.getVector3i(dungeonRoom) == VectorUtils.BlockPosToVec3i(event.pos)) {
                clicked = true
                ChatTransmitter.sendDebugChat("ACTION FINISHED: CLICK")
            }
        }
    }

    override fun onRenderWorld(
        dungeonRoom: DungeonRoom?,
        partialTicks: Float,
        actionRouteProperties: ActionRouteProperties?,
        flag: Boolean
    ) {
        val pos = target.getVector3i(dungeonRoom)
        RenderUtils.highlightBlock(pos, Color(0, 255, 255, 50), partialTicks, true)
        RenderUtils.drawTextAtWorld(
            "Click",
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
            Click
            - target: $target
            - predicate: ${predicate.javaClass.simpleName}
            """.trimIndent()
    }
}
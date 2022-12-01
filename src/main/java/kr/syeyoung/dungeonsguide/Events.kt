package kr.syeyoung.dungeonsguide.events.impl

import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.network.play.server.S30PacketWindowItems
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S45PacketTitle
import net.minecraft.util.BlockPos
import net.minecraft.util.Tuple
import net.minecraftforge.fml.common.eventhandler.Event

class DungeonStartedEvent : Event()
class BossroomEnterEvent : Event()
class DungeonEndedEvent : Event()
class DungeonLeftEvent : Event()
class HypixelJoinedEvent : Event()
class SkyblockJoinedEvent : Event()
class SkyblockLeftEvent : Event()
class DungeonContextInitializationEvent : Event()
class KeyBindPressedEvent(val key: Int) : Event()
class PlayerListItemPacketEvent(val packetPlayerListItem: S38PacketPlayerListItem?) : Event()
class TitleEvent(var packetTitle: S45PacketTitle?) : Event()
class WindowUpdateEvent(var windowItems: S30PacketWindowItems?, var packetSetSlot: S2FPacketSetSlot?) : Event()
class PlayerInteractEntityEvent(var attack:Boolean = false, var entity: Entity?) : Event() {
    override fun isCancelable(): Boolean {
        return true
    }
}
abstract class BlockUpdateEvent(val updatedBlocks: Set<Tuple<BlockPos, IBlockState>> = HashSet()) : Event() {
    class Pre : BlockUpdateEvent()
    class Post : BlockUpdateEvent()
}

class StompDiedEvent(var code: Int = 0, var reason: String? = null, var remote: Boolean = false) : Event()
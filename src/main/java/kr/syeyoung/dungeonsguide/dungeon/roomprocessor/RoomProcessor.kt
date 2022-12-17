package kr.syeyoung.dungeonsguide.dungeon.roomprocessor

import kr.syeyoung.dungeonsguide.events.impl.BlockUpdateEvent
import kr.syeyoung.dungeonsguide.events.impl.KeyBindPressedEvent
import kr.syeyoung.dungeonsguide.events.impl.PlayerInteractEntityEvent
import net.minecraft.util.IChatComponent
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent

interface RoomProcessor {
    fun tick()
    fun drawScreen(partialTicks: Float)
    fun drawWorld(partialTicks: Float)
    fun chatReceived(chat: IChatComponent)
    fun actionbarReceived(chat: IChatComponent)
    fun readGlobalChat(): Boolean
    fun onPostGuiRender(event: DrawScreenEvent.Post)
    fun onEntityUpdate(updateEvent: LivingUpdateEvent)
    fun onEntityDeath(deathEvent: LivingDeathEvent)
    fun onKeybindPress(keyInputEvent: KeyBindPressedEvent)
    fun onInteract(event: PlayerInteractEntityEvent)
    fun onInteractBlock(event: PlayerInteractEvent)
    fun onBlockUpdate(blockUpdateEvent: BlockUpdateEvent)
}
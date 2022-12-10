package kr.syeyoung.dungeonsguide.dungeon.roomprocessor;

import kr.syeyoung.dungeonsguide.events.impl.BlockUpdateEvent;
import kr.syeyoung.dungeonsguide.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.events.impl.PlayerInteractEntityEvent;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public interface RoomProcessor {
    void tick();

    void drawScreen(float partialTicks);

    void drawWorld(float partialTicks);

    void chatReceived(IChatComponent chat);

    void actionbarReceived(IChatComponent chat);

    boolean readGlobalChat();

    void onPostGuiRender(GuiScreenEvent.DrawScreenEvent.Post event);

    void onEntityUpdate(LivingEvent.LivingUpdateEvent updateEvent);

    void onEntityDeath(LivingDeathEvent deathEvent);

    void onKeybindPress(KeyBindPressedEvent keyInputEvent);

    void onInteract(PlayerInteractEntityEvent event);

    void onInteractBlock(PlayerInteractEvent event);

    void onBlockUpdate(BlockUpdateEvent blockUpdateEvent);
}
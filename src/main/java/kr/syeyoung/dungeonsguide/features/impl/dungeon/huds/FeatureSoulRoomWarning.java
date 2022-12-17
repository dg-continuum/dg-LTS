package kr.syeyoung.dungeonsguide.features.impl.dungeon.huds;

import cc.polyfrost.oneconfig.hud.SingleTextHud;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.DungeonFacade;
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.impl.DungeonFairySoul;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.UUID;

public class FeatureSoulRoomWarning extends SingleTextHud {

    public FeatureSoulRoomWarning() {
        super("There is a fairy soul in this room!", true);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick) {
        if (tick.phase == TickEvent.Phase.END && tick.type == TickEvent.Type.CLIENT ) {
            if (!SkyblockStatus.isOnDungeon()) return;
            DungeonContext context = DungeonFacade.context;

            if (context == null || !context.mapProcessor.isInitialized()) return;

            if (Minecraft.getMinecraft().thePlayer == null) return;
            DungeonRoom dungeonRoom = context.getCurrentRoom();
            if (dungeonRoom == null) return;
            if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;

            if (!dungeonRoom.getDungeonRoomInfo().getUuid().equals(lastRoomUID)) {
                for (DungeonMechanic value : dungeonRoom.getMechanics().values()) {
                    if (value instanceof DungeonFairySoul)
                        warning = System.currentTimeMillis() + 2500;
                }
                lastRoomUID = dungeonRoom.getDungeonRoomInfo().getUuid();
            }
        }
    }

    @Override
    protected boolean shouldShow() {
        return warning > System.currentTimeMillis();
    }

    transient private UUID lastRoomUID = UUID.randomUUID();
    transient private long warning = 0;

    @Override
    protected String getText(boolean example) {
        return "";
    }
}

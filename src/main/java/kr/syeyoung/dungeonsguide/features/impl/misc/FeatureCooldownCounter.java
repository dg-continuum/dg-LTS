package kr.syeyoung.dungeonsguide.features.impl.misc;

import cc.polyfrost.oneconfig.hud.SingleTextHud;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.events.impl.DungeonLeftEvent;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FeatureCooldownCounter extends SingleTextHud {
    public FeatureCooldownCounter() {
        super("cooldown", true);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private long leftDungeonTime = 0L;
    @Override
    protected boolean shouldShow() {
        return System.currentTimeMillis() - leftDungeonTime < 20000;
    }


    @Override
    protected String getText(boolean example) {
        return example ? "60s" : (20 - (System.currentTimeMillis() - leftDungeonTime) / 1000)+"s";
    }


    @SubscribeEvent
    public void onDungeonLeft(DungeonLeftEvent leftEvent) {
        leftDungeonTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onGuiOpenn(GuiOpenEvent tick) {
        if (!SkyblockStatus.isOnSkyblock()) return;
        if (!(tick.gui instanceof GuiChest)) return;
        ContainerChest chest = (ContainerChest) ((GuiChest) tick.gui).inventorySlots;
        if (chest.getLowerChestInventory().getName().contains("On cooldown!") || chest.getLowerChestInventory().getName().contains("Error")) {
            leftDungeonTime = System.currentTimeMillis();
        }
    }
}

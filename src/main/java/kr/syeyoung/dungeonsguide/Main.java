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

package kr.syeyoung.dungeonsguide;

import cc.polyfrost.oneconfig.events.EventManager;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Mod(modid = Main.MOD_ID, version = Main.VERSION)
public class Main {

    @Mod.Instance(MOD_ID)
    public Main INSTANCE;

    public static final String MOD_ID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VER@";

    final Logger logger = LogManager.getLogger("DG-main");


    static final DungeonsGuide dgInstance = new DungeonsGuide();

    private boolean isLoaded = false;

    @EventHandler
    public void initEvent(final FMLInitializationEvent initializationEvent) {
        try {
            logger.info("init-ing DungeonsGuide");
            dgInstance.init();
        } catch (Exception e) {
            handleException(e, null);
        }
    }



    private boolean showedError = false;
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiOpen(GuiOpenEvent guiOpenEvent) {
        if (!showedError && !isLoaded && guiOpenEvent.gui instanceof GuiMainMenu) {
            guiOpenEvent.gui = new GuiLoadingError(guiOpenEvent.gui);
            showedError = true;
        }

    }

    public static File getConfigDir() {
        return configDir;
    }

    static File configDir;

    @EventHandler
    public void preInit(final FMLPreInitializationEvent preInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(new YoMamaOutdated());
        MinecraftForge.EVENT_BUS.register(this);

        ProgressManager.ProgressBar progressBar = ProgressManager.push("DungeonsGuide", 2);


        configDir = new File(preInitializationEvent.getModConfigurationDirectory(), "dungeonsguide");


        progressBar.step("Initializing");

        EventManager.INSTANCE.register(dgInstance);
        MinecraftForge.EVENT_BUS.register(dgInstance);

        dgInstance.preinit();

        finishUpProgressBar(progressBar);
        isLoaded = true;

    }


    public void handleException(@NotNull final Throwable e, ProgressManager.ProgressBar progressBar) {
        GuiLoadingError.cause = e;

        finishUpProgressBar(progressBar);

        e.printStackTrace();
    }

    public static void finishUpProgressBar(final ProgressManager.ProgressBar progressBar) {
        if(progressBar == null) return;
        while (progressBar.getStep() < progressBar.getSteps())
            progressBar.step("random-" + progressBar.getStep());
        ProgressManager.pop(progressBar);
    }
}

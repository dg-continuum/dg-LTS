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

import cc.polyfrost.oneconfig.events.event.LocrawEvent;
import cc.polyfrost.oneconfig.libs.eventbus.Subscribe;
import kr.syeyoung.dungeonsguide.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.commands.CommandDgDebug;
import kr.syeyoung.dungeonsguide.commands.CommandDungeonsGuide;
import kr.syeyoung.dungeonsguide.commands.CommandReparty;
import kr.syeyoung.dungeonsguide.config.Config;
import kr.syeyoung.dungeonsguide.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.dungeon.DungeonFacade;
import kr.syeyoung.dungeonsguide.events.PacketListener;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.FeatureRegistryV2;
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.party.PartyManager;
import kr.syeyoung.dungeonsguide.utils.BlockCache;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.utils.TimeScoreUtil;
import kr.syeyoung.dungeonsguide.utils.TitleRender;
import kr.syeyoung.dungeonsguide.whosonline.WhosOnlineManager;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.IOException;

public class DungeonsGuide {

    public static DgOneCongifConfig config;
    @Getter
    private static boolean firstTimeUsingDG = false;

    public boolean verbose = false;
    private SkyblockStatus skyblockStatus;

    @Getter
    private CosmeticsManager cosmeticsManager;

    public DungeonFacade getDungeonFacade() {
        return dungeonFacade;
    }

    private DungeonFacade dungeonFacade;

    @Getter
    private final BlockCache blockCache = new BlockCache();
    @Getter @Setter
    private WhosOnlineManager whosOnlineManager;

    public DungeonsGuide(){
        instance = this;
    }
    private static DungeonsGuide instance;

    public static DungeonsGuide getDungeonsGuide() {
        return instance;
    }

    @Getter
    CommandReparty commandReparty;

    @Subscribe
    public void onLocraw(LocrawEvent event) {
        // print out the location of the player
        System.out.println("got location: " + event.info.toString());

    }

    public void init() {
        ProgressManager.ProgressBar progressbar = ProgressManager.push("DungeonsGuide", 4);
        config = new DgOneCongifConfig();
        this.dungeonFacade = new DungeonFacade();
        dungeonFacade.init();

        progressbar.step("Registering Events & Commands");

        skyblockStatus = new SkyblockStatus();

        MinecraftForge.EVENT_BUS.register(skyblockStatus);

        (new FeatureRegistryV2()).runFeateureDiscovery();
        (new FeatureRegistry()).init();

        this.whosOnlineManager = new WhosOnlineManager("virginity.kokoniara.software");
//        this.whosOnlineManager = new WhosOnlineManager("localhost:3000");

//        this.whosOnlineManager.init();

        new ChatTransmitter();

        TitleRender.getInstance();

        CommandDungeonsGuide commandDungeonsGuide = new CommandDungeonsGuide();
        CommandDgDebug command = new CommandDgDebug();

        ClientCommandHandler.instance.registerCommand(commandDungeonsGuide);
        ClientCommandHandler.instance.registerCommand(command);

        MinecraftForge.EVENT_BUS.register(command);
        MinecraftForge.EVENT_BUS.register(commandDungeonsGuide);

        commandReparty = new CommandReparty();
        MinecraftForge.EVENT_BUS.register(commandReparty);

        MinecraftForge.EVENT_BUS.register(new PacketListener());
        MinecraftForge.EVENT_BUS.register(new Keybinds());

        MinecraftForge.EVENT_BUS.register(PartyManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ChatProcessor.INSTANCE);
//        MinecraftForge.EVENT_BUS.register(StaticResourceCache.INSTANCE);


        progressbar.step("Opening connection");
        cosmeticsManager = new CosmeticsManager();
        MinecraftForge.EVENT_BUS.register(cosmeticsManager);


        progressbar.step("Loading Config");
        try {
            Config.loadConfig(null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (DgOneCongifConfig.repartyCommand) {
            ClientCommandHandler.instance.registerCommand(commandReparty);
        }


        TimeScoreUtil.init();

        Main.finishUpProgressBar(progressbar);

        ProgressManager.pop(progressbar);
    }

    private boolean showedStartUpGuide;
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent guiOpenEvent){
        if(!showedStartUpGuide){
            showedStartUpGuide = true;

            if(isFirstTimeUsingDG()){
                GuiScreen originalGUI = guiOpenEvent.gui;
                guiOpenEvent.gui = new GuiScreen() {
                    final String welcomeText = "Thank you for installing §eDungeonsGuide§f, the most intelligent skyblock dungeon mod!\nThe gui for relocating GUI Elements and enabling or disabling features can be opened by typing §e/dg\nType §e/dg help §fto view full list of commands offered by dungeons guide!";

                    @Override
                    public void initGui() {
                        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
                        this.buttonList.add(new GuiButton(0, sr.getScaledWidth()/2-100,sr.getScaledHeight()-70 ,"Continue"));
                    }

                    @Override
                    protected void actionPerformed(GuiButton button) throws IOException {
                        super.actionPerformed(button);
                        if (button.id == 0) {
                            Minecraft.getMinecraft().displayGuiScreen(originalGUI);
                        }
                    }

                    @Override
                    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
                        super.drawBackground(1);

                        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
                        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
                        fontRenderer.drawString("§eWelcome To DungeonsGuide", (sr.getScaledWidth()-fontRenderer.getStringWidth("Welcome To DungeonsGuide"))/2,40,0xFFFF0000);
                        int tenth = sr.getScaledWidth() / 10;
                        Gui.drawRect(tenth, 70,sr.getScaledWidth()-tenth, sr.getScaledHeight()-80, 0xFF5B5B5B);

                        String[] split = welcomeText.split("\n");
                        for (int i = 0; i < split.length; i++) {
                            fontRenderer.drawString(split[i].replace("\t", "    "), tenth + 2,i*fontRenderer.FONT_HEIGHT + 72, 0xFFFFFFFF);
                        }

                        super.drawScreen(mouseX, mouseY, partialTicks);
                    }

                };
            }

        }
    }


    public void preinit(){

        File configFile = new File(Main.getConfigDir(), "config.json");
        if (!configFile.exists()) {
            Main.getConfigDir().mkdirs();
            firstTimeUsingDG = true;
        }

        Config.f = configFile;
        Minecraft.getMinecraft().getFramebuffer().enableStencil();

    }

    public SkyblockStatus getSkyblockStatus() {
        return skyblockStatus;
    }

}

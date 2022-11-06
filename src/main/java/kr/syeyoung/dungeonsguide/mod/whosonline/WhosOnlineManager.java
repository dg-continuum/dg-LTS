package kr.syeyoung.dungeonsguide.mod.whosonline;

import com.google.gson.Gson;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.WhosOnlineApi;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.WhosOnlineWebSocket;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WhosOnlineManager {

    public static final Gson gson = new Gson();

    @Getter
    private final ExecutorService ex;
    final String remoteHost;
    @Getter
    private WhosOnlineWebSocket websocketManager;
    @Getter
    private WhosOnlineApi whosOnlineApi;

    public WhosOnlineManager(String host) {
        remoteHost = host;
        MinecraftForge.EVENT_BUS.register(this);
        ex = Executors.newCachedThreadPool();
    }

    public void init() {
        this.websocketManager = new WhosOnlineWebSocket(remoteHost, Minecraft.getMinecraft().getSession().getPlayerID());
        websocketManager.connect();
        this.whosOnlineApi = new WhosOnlineApi(websocketManager, ex);

    }

    @SubscribeEvent
    public void websocketdied(WhosOnlineDied e){
        this.init();
    }

    public static class WhosOnlineDied extends Event {

    }

}

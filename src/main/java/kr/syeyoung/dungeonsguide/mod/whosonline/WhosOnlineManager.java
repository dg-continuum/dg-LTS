package kr.syeyoung.dungeonsguide.mod.whosonline;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.WhosOnlineApi;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.WhosOnlineWebSocket;
import lombok.Getter;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

public class WhosOnlineManager {

    public static final Gson gson = new Gson();

    static final Logger logger = LogManager.getLogger("WhosOnlineManager");

    private final ExecutorService ex;
    private final ScheduledExecutorService se;

    final String remoteHost;
    @Getter
    private WhosOnlineWebSocket websocketManager;
    @Getter
    private WhosOnlineApi whosOnlineApi;

    public WhosOnlineManager(String host) {
        remoteHost = host;
        MinecraftForge.EVENT_BUS.register(this);

        val namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Dg WhosOnline pool").build();

        ex = Executors.newCachedThreadPool(namedThreadFactory);
        se = Executors.newSingleThreadScheduledExecutor(namedThreadFactory);
    }

    public void init() {
        this.websocketManager = new WhosOnlineWebSocket(remoteHost, se, Minecraft.getMinecraft().getSession().getPlayerID());
        websocketManager.connect();
        this.whosOnlineApi = new WhosOnlineApi(websocketManager, ex);

    }

    @SubscribeEvent
    public void websocketdied(WhosOnlineDied e){
        logger.info("Who'sOnline websocket died, trying again in 4 seconds");
        se.schedule(this::init, 4, TimeUnit.SECONDS);
    }

    public static class WhosOnlineDied extends Event {

    }

}

package kr.syeyoung.dungeonsguide.mod.whosonline;

import kr.syeyoung.dungeonsguide.mod.whosonline.api.WhosOnlineApi;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.WhosOnlineWebSocket;
import lombok.Getter;
import net.minecraft.client.Minecraft;

public class WhosOnlineManager {
    @Getter
    private WhosOnlineWebSocket websocketManager;
    @Getter
    private WhosOnlineApi whosOnlineApi;
    final String remoteHost;

    public WhosOnlineManager(String host) {
        remoteHost = host;
    }

    public void init(){
        this.websocketManager = new WhosOnlineWebSocket(remoteHost, Minecraft.getMinecraft().getSession().getPlayerID());
        websocketManager.connect();
        this.whosOnlineApi = new WhosOnlineApi(websocketManager);

    }

}

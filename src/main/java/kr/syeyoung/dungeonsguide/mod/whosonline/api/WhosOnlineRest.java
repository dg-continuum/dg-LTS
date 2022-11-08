package kr.syeyoung.dungeonsguide.mod.whosonline.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.data.CosmeticCacheObject;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.data.CosmeticData;
import kr.syeyoung.dungeonsguide.mod.whosonline.api.data.CosmeticsUserPerms;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

public class WhosOnlineRest {
    private final WhosOnlineCache cache;
    private final ExecutorService ex;
    private final String remote;

    private static final Logger logger = LogManager.getLogger("WhosOnlineRest");

    public WhosOnlineRest(WhosOnlineCache cache, ExecutorService ex, String remote) {
        this.cache = cache;
        this.ex = ex;
        this.remote = remote;
    }


    public void fillCosmeticCache(){
        ex.submit(() -> {

            JsonObject apiResponce = null;

            try {
                apiResponce = ApiFetcher.getJson(remote + "/cosmetics");
            } catch (IOException e) {
                logger.error(e);
            }

            if(apiResponce == null) return;

            // extract the cosmetics part
            val c = apiResponce.get("cosmetics").getAsJsonArray();
            ArrayList<kr.syeyoung.dungeonsguide.mod.whosonline.api.data.CosmeticData> cms = new ArrayList<>();

            for (val element : c) {
                val obj = element.getAsJsonObject();

                val dscr = obj.get("description").getAsString();
                val displ = obj.get("display").getAsString();
                val id = obj.get("id").getAsInt();
                val name = obj.get("name").getAsString();
                val reqfgls = obj.get("required_flags").getAsInt();

                cms.add(new kr.syeyoung.dungeonsguide.mod.whosonline.api.data.CosmeticData(id, displ, dscr, name, reqfgls));

            }
            CosmeticData[] cosmetics = new CosmeticData[cms.size()];
            cms.toArray(cosmetics);


            // extract the users part
            val d = apiResponce.get("users").getAsJsonObject();
            ArrayList<CosmeticsUserPerms> ws = new ArrayList<>();

            for (val w : d.entrySet()) {
                ws.add(new CosmeticsUserPerms(UUID.fromString(w.getKey()), w.getValue().getAsInt()));
            }

            CosmeticsUserPerms[] users = new CosmeticsUserPerms[ws.size()];
            ws.toArray(users);

            cache.setCms(new CosmeticCacheObject(cosmetics, users, -1));

        });
    }

}

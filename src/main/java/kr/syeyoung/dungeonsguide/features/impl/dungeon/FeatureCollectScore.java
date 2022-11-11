/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.features.SimpleFeatureV2;
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.stomp.StompManager;
import kr.syeyoung.dungeonsguide.stomp.StompPayload;
import kr.syeyoung.dungeonsguide.utils.DungeonUtil;
import kr.syeyoung.dungeonsguide.utils.MapUtils;
import kr.syeyoung.dungeonsguide.stomp.StaticResourceCache;
import net.minecraft.util.ChatComponentText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class FeatureCollectScore extends SimpleFeatureV2 {
    public FeatureCollectScore() {
        super("misc.gatherscoredata");
    }
    static final Logger logger = LogManager.getLogger("FeatureCollectScore");

    public static void collectDungeonRunData(byte[] mapData, DungeonContext context) {
        int skill = MapUtils.readNumber(mapData, 51, 35, 9);
        int exp = MapUtils.readNumber(mapData, 51, 54, 9);
        int time = MapUtils.readNumber(mapData, 51, 73, 9);
        int bonus = MapUtils.readNumber(mapData, 51, 92, 9);
        ChatTransmitter.sendDebugChat(new ChatComponentText(("skill: " + skill + " / exp: " + exp + " / time: " + time + " / bonus : " + bonus)));
        JSONObject payload = new JSONObject().put("timeSB", DungeonUtil.getTimeElapsed())
                .put("timeR", DungeonContext.getTimeElapsed())
                .put("timeScore", time)
                .put("completionStage", context.getBossRoomEnterSeconds() == -1 ? 0 :
                        context.isDefeated() ? 2 : 1)
                .put("percentage", DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getPercentage() / 100.0)
                .put("floor", DungeonContext.getDungeonName());
        ChatTransmitter.sendDebugChat(new ChatComponentText(payload.toString()));

        if(!StompManager.getInstance().isStompConnected()){
            logger.warn("Error stomp is not connected while trying to send dungeons scored");
            return;
        }

        String target = null;
        try {
            target = StaticResourceCache.INSTANCE.getResource(StaticResourceCache.DATA_COLLECTION).get().getValue();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        if (DgOneCongifConfig.collectSpeedScore && !target.contains("falsefalsefalsefalse")) {
            StompManager.getInstance().send(new StompPayload().payload(payload.toString()).destination(target.replace("false", "").trim()));
        }
    }


}

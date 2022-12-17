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

import kr.syeyoung.dungeonsguide.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.DungeonFacade;
import kr.syeyoung.dungeonsguide.features.SimpleFeatureV2;
import kr.syeyoung.dungeonsguide.utils.DungeonUtil;
import kr.syeyoung.dungeonsguide.utils.MapUtils;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import net.minecraft.util.ChatComponentText;
import org.json.JSONObject;

public class FeatureCollectScore extends SimpleFeatureV2 {
    public FeatureCollectScore() {
        super("misc.gatherscoredata");
    }

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
                .put("percentage", DungeonFacade.context.percentage / 100.0)
                .put("floor", SkyblockStatus.dungeonNameStrriped);
        ChatTransmitter.sendDebugChat(new ChatComponentText(payload.toString()));
    }


}

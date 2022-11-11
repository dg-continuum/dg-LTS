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

package kr.syeyoung.dungeonsguide.mod.features.impl.party;

import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeatureV2;
import kr.syeyoung.dungeonsguide.mod.onconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class APIKey extends SimpleFeatureV2 {
    public APIKey() {
        super("partykicker.apikey");
    }

    @SubscribeEvent
    public void onChatGlobal(ClientChatReceivedEvent postRender) {
        if (postRender.type == 2) return;
        String str = postRender.message.getFormattedText();
        if (str.startsWith("§aYour new API key is §r§b")) {
            String apiKeys = TextUtils.stripColor(str.split(" ")[5]);
            ChatTransmitter.addToQueue(ChatTransmitter.PREFIX + "§fAutomatically Configured Hypixel API Key");
            DgOneCongifConfig.apikey = apiKeys;
        }
    }
}

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

package kr.syeyoung.dungeonsguide.features.impl.misc;

import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.events.impl.StompConnectedEvent;
import kr.syeyoung.dungeonsguide.features.SimpleFeatureV2;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FeatureUpdateAlarm extends SimpleFeatureV2 {
    public FeatureUpdateAlarm() {
        super("etc.updatealarm");
    }

    @SubscribeEvent
    public void onStomp(StompConnectedEvent stompConnectedEvent) {

        stompConnectedEvent.getStompInterface().subscribe("/topic/updates", (stompClient, payload) -> {
            this.stompPayload = payload;
        });

        stompConnectedEvent.getStompInterface().subscribe("/user/queue/messages", (stompClient, payload) -> {
            this.stompPayload = payload;
        });

    }

    private String stompPayload;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick) {
        if (tick.phase == TickEvent.Phase.END && tick.type == TickEvent.Type.CLIENT) {
            if (!SkyblockStatus.isOnSkyblock()) return;
            if (stompPayload != null) {
                ChatTransmitter.addToQueue(stompPayload);
                stompPayload = null;
                Minecraft.getMinecraft().thePlayer.playSound("random.successful_hit", 1f, 1f);
            }
        }
    }

}

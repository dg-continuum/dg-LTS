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

package kr.syeyoung.dungeonsguide.features.impl.dungeon.boss.f4;

import cc.polyfrost.oneconfig.hud.SingleTextHud;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bossfight.BossfightProcessorThorn;
import kr.syeyoung.dungeonsguide.events.impl.TitleEvent;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.stream.Stream;

public class FeatureThornSpiritBowTimer extends SingleTextHud {
    public FeatureThornSpiritBowTimer() {
        super("Spirit Bow Destruction", true);
    }

    @Override
    protected boolean shouldShow() {
        return SkyblockStatus.isOnDungeon() && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor() instanceof BossfightProcessorThorn && time > System.currentTimeMillis();
    }

    @Override
    protected String getText(boolean example) {
        if(example){
            return "1s";
        }
        return TextUtils.formatTime(time - System.currentTimeMillis());
    }

    private long time = 0;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent e) {
        if (!(SkyblockStatus.isOnDungeon() && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor() instanceof BossfightProcessorThorn)) return;
        String text = e.message.getFormattedText();
        if (text.equals("§r§a§lThe §r§5§lSpirit Bow §r§a§lhas dropped!§r")) {
            time = System.currentTimeMillis() + 16000;
        } else if (text.startsWith("§r§c[BOSS] Thorn§r§f: ")) {
            if (text.contains("another wound")
            || text.contains("My energy, it goes away")
            || text.contains("dizzy")
            || text.contains("a delicate feeling")) {
                time = 0;
            }
        } else if (text.startsWith("§r§b[CROWD]")) {
            if (Stream.of("That wasn't fair!!!", "how to damage", "Cheaters!", "BOOOO", "missing easy shots like that", "missed the shot!", "Keep dodging", "no thumbs", "can't aim").anyMatch(text::contains)) {
                time = 0;
            }
        } else if (text.equals("§r§cThe §r§5Spirit Bow§r§c disintegrates as you fire off the shot!§r")) {
            time = 0;
        }
    }

    @SubscribeEvent
    public void onTitle(TitleEvent e) {
        if (!SkyblockStatus.isOnSkyblock()) return;
        if (!(SkyblockStatus.isOnDungeon() && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() != null && DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getBossfightProcessor() instanceof BossfightProcessorThorn)) return;
        if (e.getPacketTitle().getMessage().getFormattedText().contains("picked up")) {
            time = System.currentTimeMillis() + 21000;
        }
    }

}

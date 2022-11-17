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

package kr.syeyoung.dungeonsguide.features.impl.party;

import cc.polyfrost.oneconfig.hud.TextHud;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.events.impl.DungeonStartedEvent;
import kr.syeyoung.dungeonsguide.party.PartyContext;
import kr.syeyoung.dungeonsguide.party.PartyManager;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

public class FeaturePartyReady extends TextHud {
    public FeaturePartyReady() {
        super(true);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onDungeonStart(DungeonStartedEvent leftEvent) {
        ready.clear();
        terminal.clear();
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!SkyblockStatus.isOnSkyblock()) return;
        String txt = event.message.getFormattedText();
        if (!txt.startsWith("§r§9Party §8>")) return;

        String chat = TextUtils.stripColor(txt.substring(txt.indexOf(":")+1)).trim().toLowerCase();


        String usernamearea = TextUtils.stripColor(txt.substring(13, txt.indexOf(":")));
        String username = null;
        for (String s : usernamearea.split(" ")) {
            if (!s.isEmpty() && !s.startsWith("[")) {
                username = s;
                break;
            }
        }


        Boolean status = null;
        String longestMatch = "";
        for (Map.Entry<String, Boolean> stringBooleanEntry : readynessIndicator.entrySet()) {
            if (chat.startsWith(stringBooleanEntry.getKey()) || chat.endsWith(stringBooleanEntry.getKey()) || (stringBooleanEntry.getKey().length()>=3 && chat.contains(stringBooleanEntry.getKey()))) {
                if (stringBooleanEntry.getKey().length() > longestMatch.length()) {
                    longestMatch = stringBooleanEntry.getKey();
                    status = stringBooleanEntry.getValue();
                }
            }
        }
        if (status != null) {
            if (Boolean.TRUE.equals(status)) {
                ready.add(username);
            } else {
                ready.remove(username);
            }
        }


        StringBuilder term = new StringBuilder();
        for (String s : terminalPhrase) {
            if (chat.equals(s) || chat.startsWith(s+" ") || chat.endsWith(" "+s) || chat.contains(" "+s+" ")) {
                term.append(s).append(" ");
            }
        }
        if (term.length() > 0) {
            terminal.put(username, term.toString());
        }
    }

    @Override
    protected boolean shouldShow() {
        return PartyManager.INSTANCE.getPartyContext() != null && PartyManager.INSTANCE.getPartyContext().isPartyExistHypixel() && "Dungeon Hub".equals(DungeonContext.getDungeonName());
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {
        if(example){
            lines.add("syeyoung: Ready");
            lines.add("rioho: Ready");
            lines.add("RaidShadowLegends: Not Ready 2t");
            lines.add("Tricked: Ready ss");
            lines.add("MrPenguin: Not Ready 2b");
            return;
        }


        PartyContext pc = PartyManager.INSTANCE.getPartyContext();
        if(pc == null) return;

        for (String partyRawMember : pc.getPartyRawMembers()) {
            StringBuilder a = new StringBuilder();
            a.append(partyRawMember);
            a.append(": ");
            if (ready.contains(partyRawMember)) {
                a.append("Ready");
            } else {
                a.append("Not Ready");
            }
            if (terminal.get(partyRawMember) != null) {
                a.append(" ");
                a.append(terminal.get(partyRawMember));
            }
            lines.add(a.toString());
        }
    }

    transient private final Set<String> ready = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    transient private final Map<String, String> terminal = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    transient private static final List<String> readyPhrase = Arrays.asList("r", "rdy", "ready");
    transient private static final List<String> negator = Arrays.asList("not ", "not", "n", "n ");
    transient private static final List<String> terminalPhrase = Arrays.asList("ss", "s1", "1", "2b", "2t", "3", "4", "s3", "s4", "s2", "2");
    transient private static final Map<String, Boolean> readynessIndicator = new HashMap<>();

    static {
        readyPhrase.forEach(val -> readynessIndicator.put(val, true));
        for (String s : negator) {
            readyPhrase.forEach(val -> readynessIndicator.put(s+val, false));
        }
        readynessIndicator.put("dont start", false);
        readynessIndicator.put("don't start", false);
        readynessIndicator.put("dont go", false);
        readynessIndicator.put("don't go", false);
        readynessIndicator.put("start", true);
        readynessIndicator.put("go", true);
    }

}

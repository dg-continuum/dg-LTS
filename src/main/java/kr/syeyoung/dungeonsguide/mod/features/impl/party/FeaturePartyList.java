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

package kr.syeyoung.dungeonsguide.mod.features.impl.party;

import cc.polyfrost.oneconfig.hud.TextHud;
import kr.syeyoung.dungeonsguide.mod.party.PartyContext;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;

import java.util.List;

public class FeaturePartyList extends TextHud {
    public FeaturePartyList() {
        super(false);
    }


    @Override
    protected boolean shouldShow() {
        return PartyManager.INSTANCE.getPartyContext() != null;
    }



    @Override
    protected void getLines(List<String> lines, boolean example) {
        if(example){
            lines.add("Leader: RaidShadowLegends");
            lines.add("Moderator: rioho, Tricked");
            lines.add("Member: steve");
            lines.add("All invite Off");
            return;
        }

        PartyContext pc = PartyManager.INSTANCE.getPartyContext();

        if(pc == null) return;

        lines.add("Leader: " + pc.getPartyOwner());
        lines.add("Moderator: " + (pc.getPartyModerator() == null ? "????" : String.join(", ", pc.getPartyModerator()) + (pc.isModeratorComplete() ? "" : " ?")));
        lines.add("Member: " + (pc.getPartyMember() == null ? "????" : String.join(", ", pc.getPartyMember()) + (pc.isMemberComplete() ? "" : " ?")));

        if (pc.getAllInvite() != null && !pc.getAllInvite()) {
            lines.add(" All invite Off");
        } else if (pc.getAllInvite() != null) {
            lines.add(" All invite On");
        } else {
            lines.add(" All invite Unknown");
        }


    }
}

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

package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction;
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionChangeState;
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionClick;
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionMove;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Data;
import org.joml.Vector3i;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class DungeonDummy implements DungeonMechanic {
    private static final long serialVersionUID = -8449664812034435765L;
    private OffsetPoint secretPoint = new OffsetPoint(0, 0, 0);
    private List<String> preRequisite = new ArrayList<>();


    @Override
    public Set<AbstractAction> getAction(String state, DungeonRoom dungeonRoom) {
//        if (!"navigate".equalsIgnoreCase(state)) throw new IllegalArgumentException(state+" is not valid state for secret");
        Set<AbstractAction> base;
        Set<AbstractAction> preRequisites = base = new HashSet<>();
        if (state.equalsIgnoreCase("navigate")) {
            ActionMove actionMove = new ActionMove(secretPoint);
            preRequisites.add(actionMove);
            preRequisites = actionMove.getPreRequisite();
        } else if (state.equalsIgnoreCase("click")) {
            ActionClick actionClick = new ActionClick(secretPoint);
            preRequisites.add(actionClick);
            preRequisites = actionClick.getPreRequisite();

            ActionMove actionMove = new ActionMove(secretPoint);
            preRequisites.add(actionMove);
            preRequisites = actionMove.getPreRequisite();

        }
        {
            for (String str : preRequisite) {
                if (str.isEmpty()) continue;
                ActionChangeState actionChangeState = new ActionChangeState(str.split(":")[0], str.split(":")[1]);
                preRequisites.add(actionChangeState);
            }
        }
        return base;
    }

    @Override
    public void highlight(Color color, String name, DungeonRoom dungeonRoom, float partialTicks) {
        Vector3i pos = getSecretPoint().getVector3i(dungeonRoom);
        RenderUtils.highlightBlock(pos, color, partialTicks);
        RenderUtils.drawTextAtWorld("D-" + name, pos.x + 0.5f, pos.y + 0.375f, pos.z + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(dungeonRoom), pos.x + 0.5f, pos.y + 0f, pos.z + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
    }


    public DungeonDummy clone() throws CloneNotSupportedException {
        DungeonDummy dungeonSecret = new DungeonDummy();
        dungeonSecret.secretPoint = (OffsetPoint) secretPoint.clone();
        dungeonSecret.preRequisite = new ArrayList<>(preRequisite);
        return dungeonSecret;
    }


    @Override
    public String getCurrentState(DungeonRoom dungeonRoom) {
        return "no-state";
    }

    @Override
    public Set<String> getPossibleStates(DungeonRoom dungeonRoom) {
        return Sets.newHashSet("navigate", "click");
    }

    @Override
    public Set<String> getTotalPossibleStates(DungeonRoom dungeonRoom) {
        return Sets.newHashSet("no-state", "navigate,click");
    }

    @Override
    public OffsetPoint getRepresentingPoint(DungeonRoom dungeonRoom) {
        return secretPoint;
    }
}

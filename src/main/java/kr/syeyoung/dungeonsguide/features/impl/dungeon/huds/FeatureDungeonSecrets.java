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

package kr.syeyoung.dungeonsguide.features.impl.dungeon.huds;

import cc.polyfrost.oneconfig.hud.SingleTextHud;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.DungeonFacade;
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.DungeonUtil;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;

public class FeatureDungeonSecrets extends SingleTextHud {

    public FeatureDungeonSecrets() {
        super("Secrets", true);
    }

    public String getTotalSecrets() {
        DungeonContext context = DungeonFacade.context;
        if (context == null) return "?";
        int totalSecrets = 0;
        boolean allknown = true;
        for (DungeonRoom dungeonRoom : context.dungeonRoomList) {
            if (dungeonRoom.getTotalSecrets() != -1)
                totalSecrets += dungeonRoom.getTotalSecrets();
            else allknown = false;
        }
        return totalSecrets + (allknown ? "" : "+");
    }

    @Override
    protected boolean shouldShow() {
        return SkyblockStatus.isOnDungeon();
    }


    @Override
    protected String getText(boolean example) {
        if (example) {
            return "999/2+";
        }

        DungeonContext context = DungeonFacade.context;
        if(context == null) return "";

        return DungeonUtil.getSecretsFound() +
                "/" +
                (int) Math.ceil(DungeonUtil.getTotalSecretsInt() * context.secretPercentage) +
                " of " +
                DungeonUtil.getTotalSecretsInt() +
                (getTotalSecrets().contains("+") ? "+" : "");
    }
}

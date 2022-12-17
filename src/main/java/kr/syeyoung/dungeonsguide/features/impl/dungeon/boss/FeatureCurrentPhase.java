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

package kr.syeyoung.dungeonsguide.features.impl.dungeon.boss;

import cc.polyfrost.oneconfig.hud.SingleTextHud;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.DungeonFacade;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;

public class FeatureCurrentPhase extends SingleTextHud {
    public FeatureCurrentPhase() {
        super("Current Phase", true);

    }

    @Override
    protected boolean shouldShow() {
        return SkyblockStatus.isOnDungeon() && DungeonFacade.context != null && DungeonFacade.context.bossfightProcessor != null;
    }

    @Override
    protected String getText(boolean example) {
        if(example){
            return "fight-2";
        }

        DungeonContext context = DungeonFacade.context;
        if(context == null) return "";
        if(context.bossfightProcessor == null) return "";
        return context.bossfightProcessor.getCurrentPhase();
    }
}

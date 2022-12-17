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
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.DungeonFacade;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.bossfight.BossfightProcessorThorn;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;

public class FeatureThornBearPercentage extends SingleTextHud {
    public FeatureThornBearPercentage() {
        super("Spirit Bear", true);
    }

    @Override
    protected boolean shouldShow() {
        return SkyblockStatus.isOnDungeon() && DungeonFacade.context != null && DungeonFacade.context.bossfightProcessor instanceof BossfightProcessorThorn;
    }

    @Override
    protected String getText(boolean example) {
        if(example) {
            return "50%";
        } else {
            DungeonContext context = DungeonFacade.context;
            if(context == null) return "";
            if(context.bossfightProcessor == null) return "";
            if(!(context.bossfightProcessor instanceof BossfightProcessorThorn)) return "";
            int percentage = (int) (((BossfightProcessorThorn) context.bossfightProcessor).calculatePercentage() * 100);
            return percentage+"%";
        }
    }
}

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

package kr.syeyoung.dungeonsguide.features.impl.dungeon.boss.f6;

import cc.polyfrost.oneconfig.hud.SingleTextHud;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bossfight.BossfightProcessorSadan;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.entity.boss.BossStatus;

public class FeatureTerracotaTimer extends SingleTextHud {
    public FeatureTerracotaTimer() {
//        super("Dungeon.Bossfight.Floor 6", "Display Terracotta phase timer", "Displays Terracotta phase timer", "bossfight.terracota", true, getFontRenderer().getStringWidth("Terracottas: 1m 99s"), getFontRenderer().FONT_HEIGHT);
        super("Terracottas", true);
    }

    @Override
    protected boolean shouldShow() {
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        return SkyblockStatus.isOnDungeon() && context != null
                && context.getBossfightProcessor() instanceof BossfightProcessorSadan
                && "fight-1".equalsIgnoreCase(context.getBossfightProcessor().getCurrentPhase());
    }


    @Override
    protected String getText(boolean example) {
        return example ? "1m 99s" : TextUtils.formatTime((long) (BossStatus.healthScale * 1000 * 60 * 1.5));
    }
}

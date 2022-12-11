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

import cc.polyfrost.oneconfig.hud.TextHud;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.bossfight.HealthData;
import kr.syeyoung.dungeonsguide.oneconfig.dungeon.huds.BossHealthPage;
import kr.syeyoung.dungeonsguide.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeatureBossHealth extends TextHud {
    public FeatureBossHealth() {
        super(true);
    }


    @Override
    public boolean isEnabled() {
        return true;
    }


    public List<String> getDummyText() {
        ArrayList<String> actualBit = new ArrayList<>();
        actualBit = (ArrayList<String>) addLine(new HealthData("The Professor", 3300000, 5000000, false), actualBit);
        actualBit = (ArrayList<String>) addLine(new HealthData("Chaos Guardian", 500000, 2000000, true), actualBit);
        actualBit = (ArrayList<String>) addLine(new HealthData("Healing Guardian", 1000000, 3000000, true), actualBit);
        actualBit = (ArrayList<String>) addLine(new HealthData("Laser Guardian", 5000000, 5000000, true), actualBit);
        actualBit = (ArrayList<String>) addLine(new HealthData("Giant", 10000000, 20000000, false), actualBit);
        return actualBit;
    }

    public List<String> addLine(HealthData data, List<String> actualBit) {
        if (BossHealthPage.ignoreInattackable && !data.isAttackable()) return Collections.emptyList();

        StringBuilder line = new StringBuilder();

        line.append(data.getName()).append(": ");
        line.append(BossHealthPage.showTotalHealth ? TextUtils.format(data.getHealth()) : data.getHealth()).append(BossHealthPage.showTotalHealth ? "" : "\n");
        if (BossHealthPage.showTotalHealth) {
            line.append("/");
            line.append(BossHealthPage.showTotalHealth ? TextUtils.format(data.getMaxHealth()) : data.getMaxHealth()).append("\n");
        }
        actualBit.add(line.toString());

        return actualBit;
    }


    @Override
    protected void getLines(List<String> lines, boolean editing) {
        if(editing) {
            lines.addAll(getDummyText());
        } else {
            if (!SkyblockStatus.isOnDungeon()) return;
            if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().context == null) return;
            if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().context.bossfightProcessor == null) return;

            List<HealthData> healths = DungeonsGuide.getDungeonsGuide().getDungeonFacade().context.bossfightProcessor.getHealths();
            for (HealthData heal : healths) {
                addLine(heal, lines);
            }
        }

    }
}

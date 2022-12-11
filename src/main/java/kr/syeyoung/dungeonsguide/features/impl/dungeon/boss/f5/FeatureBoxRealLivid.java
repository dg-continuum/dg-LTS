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

package kr.syeyoung.dungeonsguide.features.impl.dungeon.boss.f5;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.bossfight.BossfightProcessorLivid;
import kr.syeyoung.dungeonsguide.features.SimpleFeatureV2;
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.val;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class FeatureBoxRealLivid extends SimpleFeatureV2 {
    public FeatureBoxRealLivid() {
        super("Dungeon.Bossfight.realLividBox");

    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent postRender) {
        if (!DgOneCongifConfig.boxRealLivid) return;
        if (!SkyblockStatus.isOnDungeon()) return;
        val context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().context;
        if (context == null) {
            return;
        }
        val bossfightProcessor = context.bossfightProcessor;
        if (bossfightProcessor instanceof BossfightProcessorLivid) {
            val playerMP = ((BossfightProcessorLivid) bossfightProcessor).getRealLivid();

            if (playerMP != null) {
                RenderUtils.highlightBox(playerMP, AxisAlignedBB.fromBounds(-0.4, 0, -0.4, 0.4, 1.8, 0.4), DgOneCongifConfig.oneconftodgcolor(DgOneCongifConfig.realLividColor), postRender.partialTicks, true);
            }
        }
    }

}

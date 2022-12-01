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

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.solvers.bossfight.BossfightProcessorThorn;
import kr.syeyoung.dungeonsguide.features.SimpleFeatureV2;
import kr.syeyoung.dungeonsguide.oneconfig.dungeon.HideAnimalPage;
import net.minecraft.entity.passive.*;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class FeatureHideAnimals extends SimpleFeatureV2 {
    public FeatureHideAnimals() {
        super("bossfight.hideanimals");
    }

    @SubscribeEvent
    public void onRenderPre(RenderLivingEvent.Pre preRender) {
        if (!HideAnimalPage.enabled) return;
        if (!SkyblockStatus.isOnDungeon()) return;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return;
        if (context.getBossfightProcessor() == null) return;
        if (!(context.getBossfightProcessor() instanceof BossfightProcessorThorn)) return;

        if (preRender.entity instanceof EntitySheep && HideAnimalPage.sheep) {
            preRender.setCanceled(true);
        } else if (preRender.entity instanceof EntityCow && HideAnimalPage.cow) {
            preRender.setCanceled(true);
        } else if (preRender.entity instanceof EntityChicken && HideAnimalPage.chicken) {
            preRender.setCanceled(true);
        } else if (preRender.entity instanceof EntityWolf && HideAnimalPage.wolf) {
            preRender.setCanceled(true);
        } else if (preRender.entity instanceof EntityRabbit && HideAnimalPage.rabbit) {
            preRender.setCanceled(true);
        }
    }
}

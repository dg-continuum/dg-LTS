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

package kr.syeyoung.dungeonsguide.mod.features.impl.boss;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessorThorn;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeatureV2;
import kr.syeyoung.dungeonsguide.mod.onconfig.dungeon.HideAnimal;
import net.minecraft.entity.passive.*;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class FeatureHideAnimals extends SimpleFeatureV2 {
    public FeatureHideAnimals() {
        super("bossfight.hideanimals");
    }

    @SubscribeEvent
    public void onRenderPre(RenderLivingEvent.Pre preRender) {
        if (!HideAnimal.enabled) return;
        if (!SkyblockStatus.isOnDungeon()) return;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return;
        if (context.getBossfightProcessor() == null) return;
        if (!(context.getBossfightProcessor() instanceof BossfightProcessorThorn)) return;

        if (preRender.entity instanceof EntitySheep && HideAnimal.sheep) {
            preRender.setCanceled(true);
        } else if (preRender.entity instanceof EntityCow && HideAnimal.cow) {
            preRender.setCanceled(true);
        } else if (preRender.entity instanceof EntityChicken && HideAnimal.chicken) {
            preRender.setCanceled(true);
        } else if (preRender.entity instanceof EntityWolf && HideAnimal.wolf) {
            preRender.setCanceled(true);
        } else if (preRender.entity instanceof EntityRabbit && HideAnimal.rabbit) {
            preRender.setCanceled(true);
        }
    }
}

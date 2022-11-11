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

package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeatureV2;
import kr.syeyoung.dungeonsguide.mod.onconfig.DgOneCongifConfig;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class FeatureHideNameTags extends SimpleFeatureV2 {
    public FeatureHideNameTags() {
        super("dungeon.hidenametag");
    }

    @SubscribeEvent
    public void onRender(RenderLivingEvent.Pre preRender) {
        if (!DgOneCongifConfig.hideMobNametags) return;
        if (!SkyblockStatus.isOnDungeon()) return;

        if (preRender.entity instanceof EntityArmorStand) {
            EntityArmorStand armorStand = (EntityArmorStand) preRender.entity;
            if (armorStand.getAlwaysRenderNameTag()) {
                preRender.setCanceled(true);
            }
        }
    }


}

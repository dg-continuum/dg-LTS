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

package kr.syeyoung.dungeonsguide.mod.features.impl.etc;

import kr.syeyoung.dungeonsguide.mod.features.SimpleFeatureV2;
import kr.syeyoung.dungeonsguide.mod.onconfig.DgOneCongifConfig;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FeatureDecreaseExplosionSound extends SimpleFeatureV2 {
    public FeatureDecreaseExplosionSound() {
       super("qol.explosionsound");
    }

    @SubscribeEvent
    public void onSoundEvent(PlaySoundEvent soundEvent) {
        if(!DgOneCongifConfig.decreseExplosionSound) return;

        if (soundEvent.name.equalsIgnoreCase("random.explode") && soundEvent.result instanceof PositionedSoundRecord) {
            PositionedSoundRecord positionedSoundRecord = (PositionedSoundRecord) soundEvent.result;

            soundEvent.result = new PositionedSoundRecord(
                    positionedSoundRecord.getSoundLocation(),
                    positionedSoundRecord.getVolume() * (DgOneCongifConfig.explosionDecreseMultiplyer / 100),
                    positionedSoundRecord.getPitch(),
                    positionedSoundRecord.getXPosF(),
                    positionedSoundRecord.getYPosF(),
                    positionedSoundRecord.getZPosF()
            );
        }
    }

}

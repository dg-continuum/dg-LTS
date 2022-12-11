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

package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.bossfight;

import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.utils.BlockCache;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import org.joml.Vector3i;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BossfightProcessorThorn extends GeneralBossfightProcessor {


    private final Set<Vector3i> progressBar = new HashSet<Vector3i>();
    private final World w;
    private int ticksPassed = 0;

    public BossfightProcessorThorn() {
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight").build()
        );
        w = Minecraft.getMinecraft().theWorld;
    }

    @Override
    public void tick() {
        ticksPassed++;
        if (ticksPassed == 20) {
            progressBar.clear();
            for (int x = -30; x <= 30; x++) {
                for (int y = -30; y <= 30; y++) {
                    Vector3i newPos = new Vector3i(5 + x, 77, 5 + y);
                    Block b = BlockCache.getBlock(newPos);
                    if ((b == Blocks.coal_block || b == Blocks.sea_lantern) && BlockCache.getBlock(newPos.add(0, 1, 0)) != Blocks.carpet)
                        progressBar.add(newPos);
                }
            }
        }
    }

    @Override
    public List<HealthData> getHealths() {
        List<HealthData> healths = new ArrayList<HealthData>();
        healths.add(new HealthData("Thorn", Math.round(BossStatus.healthScale * 4), 4, true));
        return healths;
    }

    @Override
    public String getBossName() {
        return "Thorn";
    }

    public double calculatePercentage() {
        int total = progressBar.size(), lit = 0;
        if (total == 0) return 0;
        for (Vector3i pos : progressBar) {
            if (BlockCache.getBlock(pos) == Blocks.sea_lantern) lit++;
        }

        return lit / (double) total;
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!DgOneCongifConfig.debugMode) return;
        try {
            Vector3i pos = new Vector3i(205, 77, 205);
            RenderUtils.highlightBlock(pos, new Color(0, 255, 255, 50), partialTicks, false);
            for (Vector3i pos2 : progressBar) {
                RenderUtils.highlightBlock(pos2, BlockCache.getBlock(pos) == Blocks.sea_lantern ?
                        new Color(0, 255, 0, 50) : new Color(255, 0, 0, 50), partialTicks, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

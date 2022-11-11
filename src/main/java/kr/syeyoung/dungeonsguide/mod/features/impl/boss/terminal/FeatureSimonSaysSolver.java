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

package kr.syeyoung.dungeonsguide.mod.features.impl.boss.terminal;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessorNecron;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeatureV2;
import kr.syeyoung.dungeonsguide.mod.onconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FeatureSimonSaysSolver extends SimpleFeatureV2 {
    private final List<BlockPos> orderbuild = new ArrayList<BlockPos>();
    private final LinkedList<BlockPos> orderclick = new LinkedList<BlockPos>();
    private boolean wasButton = false;

    public FeatureSimonSaysSolver() {
        super("Dungeon.Bossfight.simonsays2");
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        if (!SkyblockStatus.isOnSkyblock()) return;
        if (!DgOneCongifConfig.simonySaysSolver) return;

        DungeonContext dc = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dc == null) return;
        if (!(dc.getBossfightProcessor() instanceof BossfightProcessorNecron)) return;
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;

        BlockPos pos = event.pos.add(1, 0, 0);
        if (120 <= pos.getY() && pos.getY() <= 123 && pos.getX() == 310 && 291 <= pos.getZ() && pos.getZ() <= 294) {
            if (DungeonsGuide.getDungeonsGuide().getBlockCache().getBlockState(event.pos).getBlock() != Blocks.stone_button)
                return;
            if (pos.equals(orderclick.peek())) {
                orderclick.poll();
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.type != TickEvent.Type.CLIENT) {
            return;
        }
        if (!SkyblockStatus.isOnSkyblock()) return;
        DungeonContext dc = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dc == null) {
            wasButton = false;
            return;
        }
        if (!(dc.getBossfightProcessor() instanceof BossfightProcessorNecron)) return;

        if (wasButton && DungeonsGuide.getDungeonsGuide().getBlockCache().getBlockState(new BlockPos(309, 123, 291)).getBlock() == Blocks.air) {
            orderclick.clear();
            orderbuild.clear();
            wasButton = false;
        } else if (!wasButton && DungeonsGuide.getDungeonsGuide().getBlockCache().getBlockState(new BlockPos(309, 123, 291)).getBlock() == Blocks.stone_button) {
            orderclick.addAll(orderbuild);
            wasButton = true;
        }


        if (!wasButton) {
            for (BlockPos allInBox : BlockPos.getAllInBox(new BlockPos(310, 123, 291), new BlockPos(310, 120, 294))) {
                if (DungeonsGuide.getDungeonsGuide().getBlockCache().getBlockState(allInBox).getBlock() == Blocks.sea_lantern && !orderbuild.contains(allInBox)) {
                    orderbuild.add(allInBox);
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (!SkyblockStatus.isOnSkyblock()) return;
        if (!DgOneCongifConfig.simonySaysSolver) return;
        DungeonContext dc = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dc == null) {
            return;
        }
        if (!(dc.getBossfightProcessor() instanceof BossfightProcessorNecron)) return;
        if (Minecraft.getMinecraft().thePlayer.getPosition().distanceSq(309, 123, 291) > 400) return;


        if (orderclick.size() >= 1)
            RenderUtils.highlightBlock(orderclick.get(0), new Color(0, 255, 255, 100), e.partialTicks, false);
        if (orderclick.size() >= 2)
            RenderUtils.highlightBlock(orderclick.get(1), new Color(255, 170, 0, 100), e.partialTicks, false);
    }

}

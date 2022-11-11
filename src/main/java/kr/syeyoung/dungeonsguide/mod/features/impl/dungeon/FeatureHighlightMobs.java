package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeatureV2;
import kr.syeyoung.dungeonsguide.mod.onconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.mod.onconfig.misc.HighlightMobs;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntityBat;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class FeatureHighlightMobs extends SimpleFeatureV2 {
    public FeatureHighlightMobs() {
        super("dungeon.starmobbox");
    }


    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent postRender) {
        if (!SkyblockStatus.isOnDungeon()) return;


        val player = Minecraft.getMinecraft().thePlayer.getPosition();

        if(DgOneCongifConfig.highlightStaredMobs){
            val radius = (int) HighlightMobs.starRadius;
            val sq = radius * radius;
            List<EntityArmorStand> skeletonList = Minecraft.getMinecraft().theWorld.getEntities(EntityArmorStand.class, input -> {
                if (player.distanceSq(input.getPosition()) > sq) return false;
                if (!input.getAlwaysRenderNameTag()) return false;
                return input.getName().contains("✯");
            });

            AColor c = DgOneCongifConfig.oneconftodgcolor(HighlightMobs.starColor);
            for (val entity : skeletonList) {
                RenderUtils.highlightBox(entity, c, postRender.partialTicks, true);
            }
        }

        if(DgOneCongifConfig.highlightBats){
            val radius = (int) HighlightMobs.batRadius;
            val sq = radius * radius;
            List<EntityBat> batList = Minecraft.getMinecraft().theWorld.getEntities(EntityBat.class, input -> {
                if (input != null && input.isInvisible()) return false;
                return input != null && input.getDistanceSq(player) < sq;
            });

            AColor c = DgOneCongifConfig.oneconftodgcolor(HighlightMobs.batColor);
            for (val entitySkeleton : batList) {
                RenderUtils.highlightBox(entitySkeleton, c, postRender.partialTicks, true);
            }
        }

        if(DgOneCongifConfig.highlightSkeletonMasters){
            val radius = (int) HighlightMobs.masterRadius;
            val sq = radius * radius;
            List<EntityArmorStand> skeletonMasterList = Minecraft.getMinecraft().theWorld.getEntities(EntityArmorStand.class, input -> {
                if (player.distanceSq(input.getPosition()) > sq) return false;
                if (!input.getAlwaysRenderNameTag()) return false;
                return input.getName().contains("Skeleton Master");
            });

            AColor c = DgOneCongifConfig.oneconftodgcolor(HighlightMobs.masterColor);
            for (val entitySkeleton : skeletonMasterList) {
                RenderUtils.highlightBox(entitySkeleton, c, postRender.partialTicks, true);
            }
        }

    }


}

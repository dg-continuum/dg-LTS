package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.features.SimpleFeatureV2;
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.oneconfig.misc.HighlightMobsPage;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
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
    public void onRenderWorld(RenderWorldLastEvent renderWorldLastEvent) {
        if (!SkyblockStatus.isOnDungeon()) return;


        val playerPosition = Minecraft.getMinecraft().thePlayer.getPosition();

        if(DgOneCongifConfig.highlightStaredMobs){
            val radius = (int) HighlightMobsPage.starRadius;
            val sq = radius * radius;
            List<EntityArmorStand> skeletonList = Minecraft.getMinecraft().theWorld.getEntities(EntityArmorStand.class, input -> {
                if (playerPosition.distanceSq(input.getPosition()) > sq) return false;
                if (!input.getAlwaysRenderNameTag()) return false;
                return input.getName().contains("✯");
            });

            AColor c = DgOneCongifConfig.oneconftodgcolor(HighlightMobsPage.starColor);
            for (val entity : skeletonList) {
                RenderUtils.highlightBox(entity, c, renderWorldLastEvent.partialTicks, true);
            }
        }

        if(DgOneCongifConfig.highlightBats){
            val radius = (int) HighlightMobsPage.batRadius;
            val sq = radius * radius;
            List<EntityBat> batList = Minecraft.getMinecraft().theWorld.getEntities(EntityBat.class, input -> {
                if (input != null && input.isInvisible()) return false;
                return input != null && input.getDistanceSq(playerPosition) < sq;
            });

            AColor c = DgOneCongifConfig.oneconftodgcolor(HighlightMobsPage.batColor);
            for (val entitySkeleton : batList) {
                RenderUtils.highlightBox(entitySkeleton, c, renderWorldLastEvent.partialTicks, true);
            }
        }

        if(DgOneCongifConfig.highlightSkeletonMasters){
            val radius = (int) HighlightMobsPage.masterRadius;
            val sq = radius * radius;
            List<EntityArmorStand> skeletonMasterList = Minecraft.getMinecraft().theWorld.getEntities(EntityArmorStand.class, input -> {
                if (playerPosition.distanceSq(input.getPosition()) > sq) return false;
                if (!input.getAlwaysRenderNameTag()) return false;
                return input.getName().contains("Skeleton Master");
            });

            AColor c = DgOneCongifConfig.oneconftodgcolor(HighlightMobsPage.masterColor);
            for (val entitySkeleton : skeletonMasterList) {
                RenderUtils.highlightBox(entitySkeleton, c, renderWorldLastEvent.partialTicks, true);
            }
        }

    }


}

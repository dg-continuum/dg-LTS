package kr.syeyoung.dungeonsguide.utils;

import com.github.benmanes.caffeine.cache.Caffeine;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlockCache {
    static Map<Vector3i, Long> visualise = new ConcurrentHashMap<>();
    static long lastAsked;
    static int series;
    static int fadeLength = 1400;
    static int fadeStartOpacity = 200;


    private final com.github.benmanes.caffeine.cache.LoadingCache<BlockPos, IBlockState> cache = Caffeine.newBuilder()
            .maximumSize(30_000)
            .expireAfterWrite(Duration.ofMinutes(1))
            .build(key -> Minecraft.getMinecraft().theWorld.getBlockState(key));

    public BlockCache() {
        MinecraftForge.EVENT_BUS.register(this);
    }


    public static IBlockState getBlockState(@NotNull Vector3d pos) {
        return getBlockState(new BlockPos(pos.x, pos.y, pos.z));
    }
    public static IBlockState getBlockState(@NotNull Vector3i pos) {
        return getBlockState(new BlockPos(pos.x, pos.y, pos.z));
    }

    public static Block getBlock(@NotNull Vector3i pos) {
        return getBlockState(new BlockPos(pos.x, pos.y, pos.z)).getBlock();
    }

    public static Block getBlock(@NotNull BlockPos pos) {
        return getBlockState(pos).getBlock();
    }

    public static IBlockState getBlockState(@NotNull BlockPos pos) {
        if (DgOneCongifConfig.visualiseBlockGetCalls) {
            long now = System.currentTimeMillis();
            // iF THE DIFFERENCE BETWEEN LAST ASKED AND NOW IS BIGGER THEN ONE SECOND RESET THE COUNTER
            if (lastAsked - now >= 1000) {
                series = 0;
                lastAsked = 0;
            } else {
                series++;
                lastAsked = now;
            }

            visualise.put(VectorUtils.BlockPosToVec3i(pos), now + fadeLength + (series * 50L));
        }
        if (DgOneCongifConfig.debugBlockcaching) {
            return DungeonsGuide.getDungeonsGuide().getBlockCache().cache.get(pos);
        } else {
            return Minecraft.getMinecraft().theWorld.getBlockState(pos);
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load e) {
        cache.invalidateAll();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!DgOneCongifConfig.visualiseBlockGetCalls) return;

        val iterator = visualise.entrySet().iterator();
        while (iterator.hasNext()) {
            val blockPosLongEntry = iterator.next();

            long ttl = (blockPosLongEntry.getValue()) - System.currentTimeMillis();


            if (ttl <= 0) {
                iterator.remove();
            } else {

                int alpha = (int) (fadeStartOpacity * (ttl / fadeLength));

                if(alpha < 0){
                    alpha = 0;
                }

                if(alpha > 255){
                    alpha = 255;
                }


                java.awt.Color thaColor = new java.awt.Color(3, 148, 252, alpha);

                RenderUtils.highlightBlock(blockPosLongEntry.getKey(), thaColor, event.partialTicks);
            }

        }
        GlStateManager.color(1, 1, 1, 1);
    }
}

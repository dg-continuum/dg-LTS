package kr.syeyoung.dungeonsguide.dungeon;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import kr.syeyoung.dungeonsguide.Main;
import kr.syeyoung.dungeonsguide.dungeon.pathfinding.ThetaStar;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DungeonFacade {

    public static final ExecutorService ex = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("Dg-AsyncPathFinder-%d").build());

    @Getter
    @Setter
    private DungeonContext context;

    public void init() {
        DungeonListener dgEventListener = new DungeonListener();
        MinecraftForge.EVENT_BUS.register(dgEventListener);
        ex.shutdownNow();
        try {
            DungeonRoomInfoRegistry.loadAll(Main.getConfigDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final Map<Vec3, ThetaStar> activeThetaStar = new HashMap<>();

    public float calculatePathLenght(BlockPos from, BlockPos to, DungeonRoom r){
        float distance = -1;
        Vec3 fromv3 = new Vec3(from.getX(), from.getY(), from.getZ());
        Vec3 tov3 = new Vec3(to.getX(), to.getY(), to.getZ());
        val a = genPathfind(fromv3, tov3, r);
        List<Vec3> b = null;
        try {
            b = a.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        Vec3 last = null;
        for (Iterator<Vec3> iterator = b.iterator(); iterator.hasNext(); ) {
            Vec3 vec3 = iterator.next();
            if(last != null){
                distance += vec3.distanceTo(last);
            }
            last = vec3;
        }
        
        return distance;
    }


    Future<List<Vec3>> genPathfind(Vec3 from, Vec3 to, DungeonRoom room){
        return DungeonFacade.ex.submit(() -> {
            BlockPos poss = new BlockPos(from);
            ThetaStar pathFinder =
                    activeThetaStar.computeIfAbsent(from, pos -> new ThetaStar(room,new Vec3(poss.getX(), poss.getY(), poss.getZ())
                            .addVector(0.5, 0.5, 0.5)));
            pathFinder.pathfind(to, 100);
            return pathFinder.getRoute();
        });
    }

}

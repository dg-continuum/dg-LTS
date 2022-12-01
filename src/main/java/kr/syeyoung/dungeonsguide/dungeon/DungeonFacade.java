package kr.syeyoung.dungeonsguide.dungeon;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import kr.syeyoung.dungeonsguide.Main;
import kr.syeyoung.dungeonsguide.dungeon.pathfinding.CachedPathFinder;
import kr.syeyoung.dungeonsguide.dungeon.pathfinding.impl.ThetaStar;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.Setter;
import lombok.val;
import net.minecraftforge.common.MinecraftForge;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DungeonFacade {


    public CachedPathFinder getCachedPathFinder() {
        return cachedPathFinder;
    }

    final CachedPathFinder cachedPathFinder;

    public final ExecutorService ex;

    public static DungeonFacade INSTANCE;

    public DungeonContext getContext() {
        return context;
    }

    @Setter
    private DungeonContext context;

    public DungeonFacade() {
        INSTANCE = this;
        ex = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder().setNameFormat("Dg-AsyncPathFinder-%d").build()
        );
        cachedPathFinder = new CachedPathFinder();
    }

    public void init() {
        DungeonListener dgEventListener = new DungeonListener();
        MinecraftForge.EVENT_BUS.register(dgEventListener);
        try {
            DungeonRoomInfoRegistry.loadAll(Main.getConfigDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public float calculatePathLenght(Vector3i from, Vector3i to, DungeonRoom r){
        float distance = -1;
        Vector3d fromv3 = new Vector3d(from.x, from.y, from.z);
        Vector3d tov3 = new Vector3d(to.x, to.y, to.z);
        val a = genPathfind(fromv3, tov3, r);
        List<Vector3d> b;
        try {
            b = a.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        Vector3d last = null;
        for (Iterator<Vector3d> iterator = b.iterator(); iterator.hasNext(); ) {
            Vector3d vec3 = iterator.next();
            if(last != null){
                distance += vec3.distance(last);
            }
            last = vec3;
        }
        
        return distance;
    }


    Future<List<Vector3d>> genPathfind(Vector3d from, Vector3d to, DungeonRoom room){
        return ex.submit(() -> {
            ThetaStar pathFinder = new ThetaStar(room);
            pathFinder.pathfind(from, to,100);
            return pathFinder.getRoute();
        });
    }

}

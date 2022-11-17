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

package kr.syeyoung.dungeonsguide.dungeon;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonSpecificDataProvider;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonSpecificDataProviderRegistry;
import kr.syeyoung.dungeonsguide.dungeon.events.DungeonEvent;
import kr.syeyoung.dungeonsguide.dungeon.events.DungeonEventData;
import kr.syeyoung.dungeonsguide.dungeon.events.impl.DungeonNodataEvent;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.solvers.bossfight.BossfightProcessor;
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.List;
import java.util.*;

public class DungeonContext {

    private final Map<Integer, Vec3> batSpawnedLocations = new HashMap<>();

    private final List<Integer> killedBats = new ArrayList<>();
    /**
     * This is static because it's used in the constructor,
     * it means we cannot set the name without having an object,
     * and we cannot create an object without the name
     * so its static
     */
    @Getter
    @Setter
    private static String dungeonName;
    @Getter
    private final World world;

    public MapProcessor getMapProcessor() {
        return mapProcessor;
    }

    private final MapProcessor mapProcessor;
    @Getter
    private final Map<Point, DungeonRoom> roomMapper = new HashMap<>();
    @Getter
    private final List<DungeonRoom> dungeonRoomList = new ArrayList<>();
    @Getter
    private final List<RoomProcessor> globalRoomProcessors = new ArrayList<>();
    @Getter
    private final Map<String, Integer> deaths = new HashMap<>();
    @Getter
    private final List<String[]> milestoneReached = new ArrayList<>();
    @Getter
    private final Set<String> players = new HashSet<>();
    @Getter
    private final List<DungeonEvent> events = new ArrayList<>();
    public static final Rectangle roomBoundary = new Rectangle(-10, -10, 138, 138);
    @Getter
    @Setter
    public int percentage;
    @Getter
    @Setter
    private long started = -1;
    @Getter
    @Setter
    private BlockPos dungeonMin;
    @Getter
    @Setter
    private long BossRoomEnterSeconds = -1;
    @Getter
    @Setter
    private long init = -1;
    @Getter
    @Setter
    private BlockPos bossroomSpawnPos = null;
    @Getter
    @Setter
    private boolean hasTrapRoom = false;

    public boolean isGotMimic() {
        return gotMimic;
    }

    private boolean gotMimic = false;
    @Getter @Setter
    private int latestSecretCnt = 0;
    @Getter @Setter
    private int latestTotalSecret = 0;
    @Getter @Setter
    private int latestCrypts = 0;
    @Getter
    private int maxSpeed = 600;
    @Getter
    private double secretPercentage = 1.0;
    @Getter
    @Setter
    private BossfightProcessor bossfightProcessor;
    @Getter @Setter
    private boolean ended = false;
    @Getter @Setter
    private boolean defeated = false;

    @Getter
    final DungeonSpecificDataProvider dataProvider;

    public DungeonContext(World world) {
        this.world = world;
        createEvent(new DungeonNodataEvent("DUNGEON_CONTEXT_CREATION"));
        mapProcessor = new MapProcessor(this);

        dataProvider = DungeonSpecificDataProviderRegistry.getDoorFinder(getDungeonName());
        if (dataProvider != null) {
            hasTrapRoom = dataProvider.hasTrapRoom(getDungeonName());
            secretPercentage = dataProvider.secretPercentage(getDungeonName());
            maxSpeed = dataProvider.speedSecond(getDungeonName());
        } else {
            ChatTransmitter.addToQueue(ChatTransmitter.PREFIX + "Failed To create Dungeon Data provider (new dungeon?), report this in discord");
        }
        init = System.currentTimeMillis();
    }

    public static long getTimeElapsed() {
        DungeonContext ctx = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (ctx == null) return -1;

        return System.currentTimeMillis() - ctx.started;
    }

    public Map<Integer, Vec3> getBatSpawnedLocations() {
        return batSpawnedLocations;
    }

    public List<Integer> getKilledBats() {
        return killedBats;
    }

    public void setGotMimic(boolean gotMimic) {
        this.gotMimic = gotMimic;
        createEvent(new DungeonNodataEvent("MIMIC_KILLED"));
    }

    static final Logger logger = LogManager.getLogger("DungeonContext");

    public void createEvent(DungeonEventData eventData) {
        if(DgOneCongifConfig.DEBUG_MODE) logger.info(eventData.getEventName(), eventData.toString());
//        events.add(new DungeonEvent(eventData));
    }

}

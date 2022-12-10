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
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.solvers.bossfight.BossfightProcessor;
import kr.syeyoung.dungeonsguide.utils.VectorUtils;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.awt.*;
import java.util.List;
import java.util.*;

public class DungeonContext {

    private final Map<Integer, Vector3i> batSpawnedLocations = new HashMap<>();

    private final List<Integer> killedBats = new ArrayList<>();

    public static String getDungeonName() {
        return dungeonName;
    }

    public Map<Vector2i, DungeonRoom> getRoomMapper() {
        return roomMapper;
    }

    public List<DungeonRoom> getDungeonRoomList() {
        return dungeonRoomList;
    }

    public List<RoomProcessor> getGlobalRoomProcessors() {
        return globalRoomProcessors;
    }

    public Map<String, Integer> getDeaths() {
        return deaths;
    }

    public List<String[]> getMilestoneReached() {
        return milestoneReached;
    }

    public long getStarted() {
        return started;
    }

    public BlockPos getDungeonMin() {
        return dungeonMin;
    }

    public long getBossRoomEnterSeconds() {
        return BossRoomEnterSeconds;
    }

    public long getInit() {
        return init;
    }

    public BlockPos getBossroomSpawnPos() {
        return bossroomSpawnPos;
    }

    public boolean isHasTrapRoom() {
        return hasTrapRoom;
    }

    public int getLatestSecretCnt() {
        return latestSecretCnt;
    }

    public int getLatestTotalSecret() {
        return latestTotalSecret;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public double getSecretPercentage() {
        return secretPercentage;
    }

    public boolean isEnded() {
        return ended;
    }

    public boolean isDefeated() {
        return defeated;
    }

    public static void setDungeonName(String dungeonName) {
        DungeonContext.dungeonName = dungeonName;
    }

    /**
     * This is static because it's used in the constructor,
     * it means we cannot set the name without having an object,
     * and we cannot create an object without the name
     * so its static
     */
    private static String dungeonName;

    public World getWorld() {
        return world;
    }

    private final World world;

    public Set<Vector3i> getExpositions() {
        return expositions;
    }

    private final Set<Vector3i> expositions = new HashSet<>();

    public MapProcessor getMapProcessor() {
        return mapProcessor;
    }

    private final MapProcessor mapProcessor;
    private final Map<Vector2i, DungeonRoom> roomMapper = new HashMap<>();
    private final List<DungeonRoom> dungeonRoomList = new ArrayList<>();
    private final List<RoomProcessor> globalRoomProcessors = new ArrayList<>();
    private final Map<String, Integer> deaths = new HashMap<>();
    private final List<String[]> milestoneReached = new ArrayList<>();

    public Set<String> getPlayers() {
        return players;
    }

    private final Set<String> players = new HashSet<>();
    public static final Rectangle roomBoundary = new Rectangle(-10, -10, 138, 138);

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public void setStarted(long started) {
        this.started = started;
    }

    public void setDungeonMin(BlockPos dungeonMin) {
        this.dungeonMin = dungeonMin;
    }

    public void setBossRoomEnterSeconds(long bossRoomEnterSeconds) {
        BossRoomEnterSeconds = bossRoomEnterSeconds;
    }

    public void setInit(long init) {
        this.init = init;
    }

    public void setBossroomSpawnPos(BlockPos bossroomSpawnPos) {
        this.bossroomSpawnPos = bossroomSpawnPos;
    }

    public void setHasTrapRoom(boolean hasTrapRoom) {
        this.hasTrapRoom = hasTrapRoom;
    }

    public int percentage;

    private long started = -1;

    private BlockPos dungeonMin;

    private long BossRoomEnterSeconds = -1;

    private long init;

    private BlockPos bossroomSpawnPos = null;

    private boolean hasTrapRoom = false;

    public boolean isGotMimic() {
        return gotMimic;
    }

    private boolean gotMimic = false;

    public void setLatestSecretCnt(int latestSecretCnt) {
        this.latestSecretCnt = latestSecretCnt;
    }

    public void setLatestTotalSecret(int latestTotalSecret) {
        this.latestTotalSecret = latestTotalSecret;
    }

    private int latestSecretCnt = 0;
    private int latestTotalSecret = 0;

    public int getLatestCrypts() {
        return latestCrypts;
    }

    public void setLatestCrypts(int latestCrypts) {
        this.latestCrypts = latestCrypts;
    }

    private int latestCrypts = 0;
    private int maxSpeed = 600;
    private double secretPercentage = 1.0;

    public BossfightProcessor getBossfightProcessor() {
        return bossfightProcessor;
    }

    public void setBossfightProcessor(BossfightProcessor bossfightProcessor) {
        this.bossfightProcessor = bossfightProcessor;
    }

    private BossfightProcessor bossfightProcessor;

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    public void setDefeated(boolean defeated) {
        this.defeated = defeated;
    }

    private boolean ended = false;
    private boolean defeated = false;

    public DungeonSpecificDataProvider getDataProvider() {
        return dataProvider;
    }

    final DungeonSpecificDataProvider dataProvider;

    public DungeonContext(World world) {
        this.world = world;
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

    public Map<Integer, Vector3i> getBatSpawnedLocations() {
        return batSpawnedLocations;
    }

    public List<Integer> getKilledBats() {
        return killedBats;
    }

    public void setGotMimic(boolean gotMimic) {
        this.gotMimic = gotMimic;
    }

    @Nullable
    public DungeonRoom getCurrentRoom() {
        Vector2i roomPt = mapProcessor.worldPointToRoomPoint(VectorUtils.getPlayerVector3i());

        return getRoomMapper().get(roomPt);
    }


    @Nullable
    public RoomProcessor getCurrentRoomProcessor() {
        Optional<DungeonRoom> dungeonRoomOpt = Optional.ofNullable(DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext()).map(DungeonContext::getMapProcessor).map(a -> a.worldPointToRoomPoint(VectorUtils.getPlayerVector3i())).map(a -> DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getRoomMapper().get(a));


        DungeonRoom dungeonRoom = dungeonRoomOpt.orElse(null);
        if (dungeonRoom == null) return null;
        return dungeonRoom.getRoomProcessor();
    }

}

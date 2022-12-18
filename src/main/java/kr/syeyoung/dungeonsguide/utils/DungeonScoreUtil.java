package kr.syeyoung.dungeonsguide.utils;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.RoomState;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.MathHelper;

public class DungeonScoreUtil {
    public static int getCompleteRooms() {
        for (NetworkPlayerInfo networkPlayerInfoIn : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
            String name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
            if (name.startsWith("§r Completed Rooms: §r")) {
                String milestone = TextUtils.stripColor(name).substring(18);
                return Integer.parseInt(milestone);
            }
        }
        return 0;
    }

    public static int getTotalRooms() {
        int compRooms = getCompleteRooms();
        if (compRooms == 0) return 100;
        return (int) (100 * (compRooms / (double) DungeonsGuide.getDungeonsGuide().getDungeonFacade().context.percentage));
    }

    public static int getUndiscoveredPuzzles() {
        int cnt = 0;
        for (NetworkPlayerInfo networkPlayerInfoIn : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
            String name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
            if (name.startsWith("§r ???: ")) {
                cnt++;
            }
        }
        return cnt;
    }

    public static ScoreCalculation calculateScore() {
        if (!SkyblockStatus.isOnDungeon()) return null;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().context;
        if (context == null) return null;
        if (!context.mapProcessor.isInitialized()) return null;

        int skill = 100;
        int deaths = 0;
        {
            deaths = DungeonUtil.getTotalDeaths();
            skill -= DungeonUtil.getTotalDeaths() * 2;
            int totalCompRooms = 0;
            int roomCnt = 0;
            int roomSkillPenalty = 0;
//            boolean bossroomIncomplete = true;
            boolean traproomIncomplete = context.isHasTrapRoom();
            int incompletePuzzles = getUndiscoveredPuzzles();

            for (DungeonRoom dungeonRoom : context.dungeonRoomList) {
//                if (dungeonRoom.getColor() == 74 && dungeonRoom.getCurrentState() != DungeonRoom.RoomState.DISCOVERED)
//                    bossroomIncomplete = false;
                if (dungeonRoom.getColor() == 62 && dungeonRoom.getCurrentState() != RoomState.DISCOVERED)
                    traproomIncomplete = false;
                if (dungeonRoom.getCurrentState() != RoomState.DISCOVERED)
                    totalCompRooms += dungeonRoom.getUnitPoints().size();
                if (dungeonRoom.getColor() == 66 && (dungeonRoom.getCurrentState() == RoomState.DISCOVERED || dungeonRoom.getCurrentState() == RoomState.FAILED)) // INCOMPLETE PUZZLE ON MAP
                    incompletePuzzles++;
                roomCnt += dungeonRoom.getUnitPoints().size();
            }
            roomSkillPenalty += incompletePuzzles * 10;
            if (context.mapProcessor.getUndiscoveredRoom() != 0)
                roomCnt = getTotalRooms();
            roomSkillPenalty += (roomCnt - totalCompRooms) * 4;
//            if (bossroomIncomplete) roomSkillPenalty -=1;
            if (traproomIncomplete) roomSkillPenalty -= 1;


            skill -= roomSkillPenalty;


            skill = MathHelper.clamp_int(skill, 0, 100);
        }
        int explorer = 0;
        boolean fullyCleared = false;
        boolean totalSecretsKnown = true;
        int totalSecrets = 0;
        int secrets = 0;
        {
            int completed = 0;
            double total = 0;

            for (DungeonRoom dungeonRoom : context.dungeonRoomList) {
                if (dungeonRoom.getCurrentState() != RoomState.DISCOVERED && dungeonRoom.getCurrentState() != RoomState.FAILED)
                    completed += dungeonRoom.getUnitPoints().size();
                total += dungeonRoom.getUnitPoints().size();
            }

            totalSecrets = DungeonUtil.getTotalSecretsInt();
            totalSecretsKnown = DungeonUtil.sureOfTotalSecrets();

            fullyCleared = completed >= getTotalRooms() && context.mapProcessor.getUndiscoveredRoom() == 0;
            explorer += MathHelper.clamp_int((int) Math.floor(6.0 / 10.0 * (context.mapProcessor.getUndiscoveredRoom() != 0 ? DungeonsGuide.getDungeonsGuide().getDungeonFacade().context.percentage : completed / total * 100)), 0, 60);
            explorer += MathHelper.clamp_int((int) Math.floor(40 * (secrets = DungeonUtil.getSecretsFound()) / Math.ceil(totalSecrets * context.secretPercentage)), 0, 40);
        }
        int time = 0;
        {
            int maxTime = context.maxSpeed;
//            int timeSec = FeatureRegistry.DUNGEON_SBTIME.getTimeElapsed() / 1000 - maxTime + 480;
//
//            if (timeSec <= 480) time = 100;
//            else if (timeSec <= 580) time = (int) Math.ceil(148 - 0.1 * timeSec);
//            else if (timeSec <= 980) time = (int) Math.ceil(119 - 0.05 * timeSec);
//            else if (timeSec < 3060) time = (int) Math.ceil(3102 - (1/30.0) * timeSec);
//            time = MathHelper.clamp_int(time, 0, 100); // just in case.
            time = TimeScoreUtil.estimate(DungeonUtil.getTimeElapsed(), maxTime);
        }
        int bonus = 0;
        int tombs;
        {
            bonus += tombs = MathHelper.clamp_int(DungeonUtil.getTombsFound(), 0, 5);
            if (context.isGotMimic()) bonus += 2;
        }

        // amazing thing
        return new ScoreCalculation(skill, explorer, time, bonus, tombs, fullyCleared, secrets, totalSecrets, (int) Math.ceil(totalSecrets * context.secretPercentage), totalSecretsKnown, deaths);
    }

    public static String getLetter(int score) {
        if (score <= 99) return "D";
        if (score <= 159) return "C";
        if (score <= 229) return "B";
        if (score <= 269) return "A";
        if (score <= 299) return "S";
        return "S+";
    }

    @Data
    @AllArgsConstructor
    public static class ScoreCalculation {
        private int skill;
        private int explorer;
        private int time;
        private int bonus;
        private int tombs;
        private boolean fullyCleared;
        private int secrets, totalSecrets, effectiveTotalSecrets;
        private boolean totalSecretsKnown;
        private int deaths;
    }
}

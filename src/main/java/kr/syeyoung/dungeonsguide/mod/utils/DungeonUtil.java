package kr.syeyoung.dungeonsguide.mod.utils;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

import java.util.Collection;

public class DungeonUtil {
    public static int getTombsFound() {
        if(Minecraft.getMinecraft().thePlayer == null) return 0;
        for (NetworkPlayerInfo networkPlayerInfoIn : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
            String name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
            if (name.startsWith("§r Crypts: §r§6")) {
                return Integer.parseInt(TextUtils.stripColor(name).substring(9));
            }
        }
        return 0;
    }

    public static int getTotalDeaths() {
        if (!SkyblockStatus.isOnDungeon()) return 0;
        for (NetworkPlayerInfo networkPlayerInfoIn : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
            String name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
            if (name.contains("Deaths")) {
                String whatever = TextUtils.keepIntegerCharactersOnly(TextUtils.keepScoreboardCharacters(TextUtils.stripColor(name)));
                if (whatever.isEmpty()) break;
                return Integer.parseInt(whatever);
            }
        }
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return 0;
        int d = 0;
        for (Integer value : context.getDeaths().values()) {
            d += value;
        }
        return d;
    }

    static public int getTimeElapsed() {
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        Collection<Score> scores = scoreboard.getSortedScores(objective);
        String time = "idkyet";
        for (Score sc:scores) {
            ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(sc.getPlayerName());
            String strippedLine = TextUtils.keepScoreboardCharacters(TextUtils.stripColor(ScorePlayerTeam.formatPlayerName(scorePlayerTeam, sc.getPlayerName()))).trim();
            if (strippedLine.startsWith("Time Elapsed: ")) {
                time = strippedLine.substring(14);
            }
        }
        time = time.replace(" ", "");
        int hour = time.indexOf('h') == -1 ? 0 : Integer.parseInt(time.substring(0, time.indexOf('h')));
        if (time.contains("h")) time = time.substring(time.indexOf('h') + 1);
        int minute = time.indexOf('m') == -1 ? 0 : Integer.parseInt(time.substring(0, time.indexOf('m')));
        if (time.contains("m")) time = time.substring(time.indexOf('m') + 1);
        int second = time.indexOf('s') == -1 ? 0 : Integer.parseInt(time.substring(0, time.indexOf('s')));

        int time2 = hour * 60 * 60 + minute * 60 + second;
        return time2 * 1000;
    }

    public static int getSecretsFound() {
        for (val networkPlayerInfoIn : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
            String name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
            if (name.startsWith("§r Secrets Found: §r§b") && !name.contains("%")) {
                String noColor = TextUtils.stripColor(name);
                return Integer.parseInt(noColor.substring(16));
            }
        }
        return 0;
    }

    public static int getTotalSecretsInt() {
        if (getSecretsFound() != 0) {
            return (int) Math.ceil(getSecretsFound() / getSecretPercentage() * 100);
        }
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        int totalSecrets = 0;
        for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
            if (dungeonRoom.getTotalSecrets() != -1) {
                totalSecrets += dungeonRoom.getTotalSecrets();
            }
        }
        return totalSecrets;
    }

    public static boolean sureOfTotalSecrets() {
        if (getSecretsFound() != 0) return true;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context.getMapProcessor().getUndiscoveredRoom() > 0) return false;
        boolean allknown = true;
        for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
            if (dungeonRoom.getTotalSecrets() == -1) {
                allknown = false;
                break;
            }
        }
        return allknown;
    }

    public static double getSecretPercentage() {
        for (NetworkPlayerInfo networkPlayerInfoIn : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
            String name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
            if (name.startsWith("§r Secrets Found: §r") && name.contains("%")) {
                String noColor = TextUtils.stripColor(name);
                return Double.parseDouble(noColor.substring(16).replace("%", ""));
            }
        }
        return 0;
    }
}

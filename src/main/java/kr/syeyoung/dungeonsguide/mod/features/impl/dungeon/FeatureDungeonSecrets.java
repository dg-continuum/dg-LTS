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

package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon;

import cc.polyfrost.oneconfig.hud.SingleTextHud;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;

public class FeatureDungeonSecrets extends SingleTextHud {

    public FeatureDungeonSecrets() {
        super("Secrets", true);
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

    public String getTotalSecrets() {
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return "?";
        int totalSecrets = 0;
        boolean allknown = true;
        for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
            if (dungeonRoom.getTotalSecrets() != -1)
                totalSecrets += dungeonRoom.getTotalSecrets();
            else allknown = false;
        }
        return totalSecrets + (allknown ? "" : "+");
    }

    public boolean isHUDViewable() {
        return SkyblockStatus.isOnDungeon();
    }


    @Override
    protected String getText(boolean example) {
        if (example) {
            return "999/2+";
        }

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if(context == null) return "";

        return getSecretsFound() +
                "/" +
                (int) Math.ceil(getTotalSecretsInt() * context.getSecretPercentage()) +
                " of " +
                getTotalSecretsInt() +
                (getTotalSecrets().contains("+") ? "+" : "");
    }
}

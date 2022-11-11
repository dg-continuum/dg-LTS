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

package kr.syeyoung.dungeonsguide.features.impl.dungeon.huds;

import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.annotations.Text;
import cc.polyfrost.oneconfig.hud.TextHud;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

import java.util.List;


public class FeatureWarnLowHealth extends TextHud {


    @Slider(
            name = "Health Threshold",
            description = "Health Threshold for this feature to be toggled. default to 500",
            min = 100,
            max = 2000
    )
    public static float threashhold = 500;


    @Text(
            name = "low health text",
            placeholder = "is low"
    )
    public static String lowmessage = "is low";


    public FeatureWarnLowHealth() {
        super(true);

    }


    @Override
    protected boolean shouldShow() {
        return SkyblockStatus.isOnDungeon();
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {
        if(example){
            lines.add("Steve is low: 500hp");
            return;
        }
        if(Minecraft.getMinecraft().thePlayer == null) return;

        String lowestHealthName = "";
        int lowestHealth = Integer.MAX_VALUE;
        Scoreboard scoreboard = Minecraft.getMinecraft().thePlayer.getWorldScoreboard();
        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        for (Score sc : scoreboard.getSortedScores(objective)) {
            ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(sc.getPlayerName());
            String line = ScorePlayerTeam.formatPlayerName(scorePlayerTeam, sc.getPlayerName()).trim();
            String stripped = TextUtils.keepScoreboardCharacters(TextUtils.stripColor(line));
            if (line.contains("[") && line.endsWith("❤")) {
                String name = stripped.split(" ")[stripped.split(" ").length - 2];
                int health = Integer.parseInt(stripped.split(" ")[stripped.split(" ").length - 1]);
                if (health < lowestHealth) {
                    lowestHealth = health;
                    lowestHealthName = name;
                }
            }
        }
        if (lowestHealth > threashhold) {
            return;
        }
        lines.add(lowestHealthName + " " + lowmessage + " " + lowestHealth + "hp");

    }
}

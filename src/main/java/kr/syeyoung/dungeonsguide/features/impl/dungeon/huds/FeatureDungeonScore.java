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

import cc.polyfrost.oneconfig.config.annotations.Checkbox;
import cc.polyfrost.oneconfig.hud.TextHud;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.utils.DungeonScoreUtil;

import java.util.List;

public class FeatureDungeonScore extends TextHud {
    @Checkbox(
            name = "Show each score instead of sum",
            description = "Skill: 100 Explore: 58 S->S+(5 tombs) instead of Score: 305"
    )
    public static boolean verbose = false;

    public FeatureDungeonScore() {
        super(false);
    }

    @Override
    protected boolean shouldShow() {
        return SkyblockStatus.isOnDungeon();
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {

        if(example){
            if(verbose){
                lines.add("Score: 305 (S+)");
            } else {
                lines.add("Skill: 100 (0 Deaths)");
                lines.add("Explorer: 99 (Rooms O Secrets 39/40)");
                lines.add("Time: 100 Bonus: 0 Total: 299");
                lines.add("S->S+ (1 Required 1 crypt)");
            }

            return;
        }

        DungeonScoreUtil.ScoreCalculation score = DungeonScoreUtil.calculateScore();
        if (score == null) {
            return;
        }
        int sum = score.getTime() + score.getSkill() + score.getExplorer() + score.getBonus();
        if (verbose) {
            lines.add("Skill: " + score.getSkill() + "(" + score.getDeaths() + ")");

            lines.add("Explorer: " + score.getExplorer() + "(Rooms " + (score.isFullyCleared() ? "O" : "X") + " Secrets " + score.getSecrets() + "/" + score.getEffectiveTotalSecrets() + " of " + score.getTotalSecrets() + (score.isTotalSecretsKnown() ? "" : "?") + ")");

            lines.add("Time: " + score.getTime() + " Bonus: " + score.getBonus() + " Total: " + sum);

            lines.add(buildRequirement(score));

        } else {
            String letter = DungeonScoreUtil.getLetter(sum);
            lines.add("Score: ( " + sum + " " + letter + ")");
        }

    }

    public int getScoreRequirement(String letter) {
        if (letter.equals("D")) return 0;
        if (letter.equals("C")) return 100;
        if (letter.equals("B")) return 160;
        if (letter.equals("A")) return 230;
        if (letter.equals("S")) return 270;
        if (letter.equals("S+")) return 300;
        return -1;
    }

    public String getNextLetter(String letter) {
        if (letter.equals("D")) return "C";
        if (letter.equals("C")) return "B";
        if (letter.equals("B")) return "A";
        if (letter.equals("A")) return "S";
        if (letter.equals("S")) return "S+";
        else return null;
    }

    public String buildRequirement(DungeonScoreUtil.ScoreCalculation calculation) {
        int current = calculation.getTime() + calculation.getBonus() + calculation.getExplorer() + calculation.getSkill();
        String currentLetter = DungeonScoreUtil.getLetter(current);
        String nextLetter = getNextLetter(currentLetter);
        if (nextLetter == null) {
            return "S+ Expected";
        }
        int req = getScoreRequirement(nextLetter);
        int reqPT2 = req - current;
        int reqPT = req - current;

        int tombsBreakable = Math.min(5 - calculation.getTombs(), reqPT);
        reqPT -= tombsBreakable;

        double secretPer = 40.0 / calculation.getEffectiveTotalSecrets();
        int secrets = (int) Math.ceil(reqPT / secretPer);

        return currentLetter + "->" + nextLetter + " (" + reqPT2 + " required " + tombsBreakable + " crypt " + secrets + " secrets)";

    }

}

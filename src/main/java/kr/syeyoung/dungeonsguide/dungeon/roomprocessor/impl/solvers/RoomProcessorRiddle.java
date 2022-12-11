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

package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.solvers;

import kr.syeyoung.dungeonsguide.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.oneconfig.solvers.ThreeWeirdosSolverPage;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import org.joml.Vector3i;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class RoomProcessorRiddle extends GeneralRoomProcessor {

    private static final List<Pattern> patternList = Arrays.asList(
            Pattern.compile("My chest doesn't have the reward. We are all telling the truth.*"),
            Pattern.compile("The reward isn't in any of our chests.*"),
            Pattern.compile("The reward is not in my chest!.*"),
            Pattern.compile("At least one of them is lying, and the reward is not in .+'s chest.*"),
            Pattern.compile("Both of them are telling the truth. Also,.+has the reward in their chest.*"),
            Pattern.compile("My chest has the reward and I'm telling the truth.*")
    );
    BlockPos chest;

    public RoomProcessorRiddle(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
    }

    @Override
    public void chatReceived(IChatComponent chat) {
        super.chatReceived(chat);
        if (!DgOneCongifConfig.riddleSolver) return;
        String ch2 = chat.getUnformattedText();
        if (!ch2.startsWith("§e[NPC] ")) {
            return;
        }
        String watsaid = TextUtils.stripColor(ch2.split(":")[1]).trim();
        boolean foundMatch = false;
        for (Pattern p : patternList) {
            if (p.matcher(watsaid).matches()) {
                foundMatch = true;
                break;
            }
        }
        if (foundMatch) {
            ChatTransmitter.addToQueue(ChatTransmitter.PREFIX + "§eRiddle §7:: " + ch2.split(":")[0].trim() + " §fhas the reward!");
            final String name = TextUtils.stripColor(ch2.split(":")[0]).replace("[NPC] ", "").trim();
            final Vector3i low = getDungeonRoom().getMin();
            final Vector3i high = getDungeonRoom().getMax();
            getDungeonRoom();
            World w = Minecraft.getMinecraft().theWorld;
            List<EntityArmorStand> armor = w.getEntities(EntityArmorStand.class, input -> {
                BlockPos pos = input.getPosition();
                return low.x < pos.getX() && pos.getX() < high.x
                        && low.z < pos.getZ() && pos.getZ() < high.z && TextUtils.stripColor(input.getName()).equalsIgnoreCase(name);
            });

            if (armor != null) {
                this.chest = null;
                BlockPos pos = armor.get(0).getPosition();
                for (BlockPos allInBox : BlockPos.getAllInBox(pos.add(-1, 0, -1), pos.add(1, 0, 1))) {
                    Block b = w.getChunkFromBlockCoords(allInBox).getBlock(allInBox);

                    if ((b == Blocks.chest || b == Blocks.trapped_chest) && allInBox.distanceSq(pos) == 1) {
                        this.chest = allInBox;
                        return;
                    }
                }
            }

        }
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!DgOneCongifConfig.riddleSolver) return;
        if (chest != null) {
            RenderUtils.highlightBoxAColor(AxisAlignedBB.fromBounds(chest.getX(), chest.getY(), chest.getZ(), chest.getX() + 1, chest.getY() + 1, chest.getZ() + 1), new AColor(ThreeWeirdosSolverPage.chestColor.getRed(), ThreeWeirdosSolverPage.chestColor.getBlue(), ThreeWeirdosSolverPage.chestColor.getGreen(), ThreeWeirdosSolverPage.chestColor.getAlpha()), partialTicks, true);
        }
    }
}

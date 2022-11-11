/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.features.impl.discord.onlinealarm;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.enumuration.EDiscordRelationshipType;
import kr.syeyoung.dungeonsguide.mod.discord.rpc.JDiscordActivity;
import kr.syeyoung.dungeonsguide.mod.discord.rpc.JDiscordRelation;
import kr.syeyoung.dungeonsguide.mod.events.impl.DiscordUserUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeatureV2;
import kr.syeyoung.dungeonsguide.mod.features.impl.discord.inviteViewer.ImageTexture;
import kr.syeyoung.dungeonsguide.mod.features.impl.discord.inviteViewer.PartyInviteViewer;
import kr.syeyoung.dungeonsguide.mod.onconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class PlayingDGAlarm extends SimpleFeatureV2 {
    public PlayingDGAlarm() {
        super("discord.playingalarm");
    }
    private final List<PlayerOnline> notif = new CopyOnWriteArrayList<>();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick) {
        if (tick.phase == TickEvent.Phase.END && tick.type == TickEvent.Type.CLIENT ) {
            if (!SkyblockStatus.isOnSkyblock()) return;

            try {
                List<PlayerOnline> partyJoinRequestList = new ArrayList<>();
                boolean isOnHypixel = DungeonsGuide.getDungeonsGuide().getSkyblockStatus().isOnHypixel();
                for (PlayerOnline joinRequest:notif) {
                    if (!isOnHypixel || joinRequest.getEnd() < System.currentTimeMillis()){
                        partyJoinRequestList.add(joinRequest);
                    }
                }
                notif.removeAll(partyJoinRequestList);
            } catch (Throwable e) {e.printStackTrace();}
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post postRender) {
        if (!(postRender.type == RenderGameOverlayEvent.ElementType.EXPERIENCE || postRender.type == RenderGameOverlayEvent.ElementType.JUMPBAR)) return;

        if (!SkyblockStatus.isOnSkyblock()) return;

        if (!DgOneCongifConfig.discordFriendOnlineAcidifications) return;

        try {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0,0,100);
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            GlStateManager.scale(1.0 / sr.getScaleFactor(), 1.0 / sr.getScaleFactor(), 1.0);
            int height = 90;
            int gap = 5;
            int x = Minecraft.getMinecraft().displayWidth-350-gap;
            int y = Minecraft.getMinecraft().displayHeight-(height+gap)*notif.size();
            for (PlayerOnline partyJoinRequest : notif) {
                renderRequest(partyJoinRequest, x, y, 350,height);
                y += height + gap;
            }
            GlStateManager.popMatrix();
            GlStateManager.enableBlend();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onDiscordUserUpdate(DiscordUserUpdateEvent discordUserUpdateEvent) {
        JDiscordRelation prev = discordUserUpdateEvent.getPrev();
        JDiscordRelation current = discordUserUpdateEvent.getCurrent();
        if (!isDisplayable(prev) && isDisplayable(current)) {
            notif.add(new PlayerOnline(current, System.currentTimeMillis()+3000));
        }
    }


    public void renderRequest(PlayerOnline online, int x, int y, int width, int height) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x,y,0);

        Gui.drawRect(0, 0,width,height, 0xFF23272a);
        Gui.drawRect(2, 2, width-2, height-2, 0XFF2c2f33);

        String avatar = "https://cdn.discordapp.com/avatars/"+Long.toUnsignedString(online.getJDiscordRelation().getDiscordUser().getId())+"/"+online.getJDiscordRelation().getDiscordUser().getAvatar()+"."+(online.getJDiscordRelation().getDiscordUser().getAvatar().startsWith("a_") ? "gif":"png");
        Future<ImageTexture> loadedImageFuture = PartyInviteViewer.loadImage(avatar);
        ImageTexture loadedImage = null;
        if (loadedImageFuture.isDone()) {
            try {
                loadedImage = loadedImageFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        if (loadedImage != null) {
            loadedImage.drawFrameAndIncrement( 7,7,height-14,height-14);
        } else {
            Gui.drawRect(7, 7, height - 7, height-7, 0xFF4E4E4E);
        }

        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        GlStateManager.pushMatrix();
        GlStateManager.translate(height +3,7, 0);

        GlStateManager.pushMatrix();
        GlStateManager.scale(3.0,3.0,1.0);
        fr.drawString(online.getJDiscordRelation().getDiscordUser().getUsername()+"", 0,0, 0xFFFFFFFF, true);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(fr.getStringWidth(online.getJDiscordRelation().getDiscordUser().getUsername()+"") * 3 + 1, (int)(fr.FONT_HEIGHT*1.5), 0);
        fr.drawString("#"+online.getJDiscordRelation().getDiscordUser().getDiscriminator(), 0,0,0xFFaaaaaa, true);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, fr.FONT_HEIGHT * 3 + 5, 0);
        GlStateManager.scale(1.0,1.0,1.0);
        fr.drawString("Started Playing Skyblock! (Dismissed in "+(TextUtils.formatTime(online.getEnd() - System.currentTimeMillis()))+")", 0,0,0xFFFFFFFF,false);
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }


    @Data @AllArgsConstructor
    public static class PlayerOnline {
        private JDiscordRelation jDiscordRelation;
        private long end;
    }


    public boolean isDisplayable(JDiscordRelation jDiscordRelation) {
        EDiscordRelationshipType relationshipType = jDiscordRelation.getDiscordRelationshipType();
        if (relationshipType == EDiscordRelationshipType.DiscordRelationshipType_Blocked) return false;
        if (relationshipType == EDiscordRelationshipType.DiscordRelationshipType_None) return false;
        if (relationshipType == EDiscordRelationshipType.DiscordRelationshipType_PendingIncoming) return false;
        if (relationshipType == EDiscordRelationshipType.DiscordRelationshipType_PendingOutgoing) return false;

        JDiscordActivity jDiscordActivity = jDiscordRelation.getDiscordActivity();
        return jDiscordActivity.getApplicationId() == 816298079732498473L;
    }
}

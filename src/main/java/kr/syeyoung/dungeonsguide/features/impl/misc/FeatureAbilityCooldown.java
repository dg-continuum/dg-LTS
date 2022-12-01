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

package kr.syeyoung.dungeonsguide.features.impl.misc;

import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.hud.TextHud;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeatureAbilityCooldown extends TextHud {
    transient private static final Map<String, SkyblockAbility> skyblockAbilities = new HashMap<>();
    transient private static final Map<String, List<SkyblockAbility>> skyblockAbilitiesByItemID = new HashMap<>();

    @Data
    @AllArgsConstructor
    public static class UsedAbility {
        private SkyblockAbility ability;
        @EqualsAndHashCode.Exclude
        private long cooldownEnd;
    }

    @Data
    @AllArgsConstructor
    public static class SkyblockAbility {
        private String name;
        private int manaCost;
        private int cooldown;

        private String itemId;
    }


    transient private final TreeSet<UsedAbility> usedAbilities = new TreeSet<>((c1, c2) -> {
        int a = Comparator.comparingLong(UsedAbility::getCooldownEnd).compare(c1, c2);
        return c1.getAbility().getName().equals(c2.getAbility().getName()) ? 0 : a;
    });

    static {
        register("Disgusting Healing", -1, -1, "REAPER_MASK");
        register("Spirit Leap", -1, 5, "SPIRIT_LEAP");
        register("Farmer's Grace", -1, -1, "RANCHERS_BOOTS");
        register("Raise Souls", -1, 1, "SUMMONING_RING");
        register("Raise Souls", -1, 1, "REAPER_SCYTHE");
        register("Raise Souls", -1, 1, "NECROMANCER_SWORD");
        register("Instant Heal", 70, -1, "FLORID_ZOMBIE_SWORD");
        register("Instant Heal", 70, -1, "ZOMBIE_SWORD");
        register("Instant Heal", 70, -1, "ORNATE_ZOMBIE_SWORD");
        register("Phantom Impel", -1, -1, "PHANTOM_ROD");
        register("Implosion", 300, 10, "IMPLOSION_SCROLL");
        register("Parley", -1, 5, "ASPECT_OF_THE_JERRY");
        register("Guided Bat", 250, -1, "BAT_WAND");
        register("Bat Swarm", -1, -1, "WITCH_MASK");
        register("Flay", -1, -1, "SOUL_WHIP");
        register("Mithril's Protection", -1, -1, "MITHRIL_COAT");
        register("Second Wind", -1, 30, "SPIRIT_MASK");
        register("Love Tap", -1, -1, "ZOMBIE_SOLDIER_CUTLASS");
        register("Spiky", -1, -1, "PUFFERFISH_HAT");
        register("Jingle Bells", -1, 1, "JINGLE_BELLS");
        register("Wither Shield", 150, 10, "WITHER_SHIELD_SCROLL");
        register("Wither Impact", 150, 5, "WITHER_SHIELD_SCROLL");
        register("Brute Force", -1, -1, "WARDEN_HELMET");
        register("Growth", -1, 4, "GROWTH_LEGGINGS");
        register("Shadowstep", -1, 60, "SILENT_DEATH");
        register("Creeper Veil", -1, -1, "WITHER_CLOAK");
        register("Ice Bolt", 50, -1, "FROZEN_SCYTHE");
        register("Rapid-fire", 10, -1, "JERRY_STAFF");
        register("Water Burst", 20, -1, "SALMON_HELMET_NEW");
        register("Mist Aura", -1, -1, "SORROW_BOOTS");
        register("Deploy", -1, -1, "RADIANT_POWER_ORB");
        register("Ice Spray", 50, 5, "ICE_SPRAY_WAND");
        register("Grand... Zapper?", -1, -1, "BLOCK_ZAPPER");
        register("Seek the King", -1, 5, "ROYAL_PIGEON");
        register("Mist Aura", -1, -1, "SORROW_CHESTPLATE");
        register("Healing Boost", -1, -1, "REVIVED_HEART");
        register("Deploy", -1, -1, "OVERFLUX_POWER_ORB");
        register("Swing", -1, -1, "BONE_BOOMERANG");
        register("Growth", -1, 4, "GROWTH_CHESTPLATE");
        register("Squash 'em", -1, -1, "RECLUSE_FANG");
        register("Roll em'", -1, -1, "PUMPKIN_DICER");
        register("Cleave", -1, -1, "SUPER_CLEAVER");
        register("Water Burst", 20, -1, "SALMON_BOOTS_NEW");
        register("Farmer's Delight", -1, -1, "BASKET_OF_SEEDS");
        register("Block Damage", -1, 60, "GUARDIAN_CHESTPLATE");
        register("Water Burst", 20, -1, "SALMON_LEGGINGS");
        register("Bone Shield", -1, -1, "SKELETON_HELMET");
        register("Iron Punch", 70, 3, "GOLEM_SWORD");
        register("Built-in Storage", -1, -1, "BUILDERS_WAND");
        register("Nasty Bite", -1, -1, "MOSQUITO_BOW");
        register("Ender Warp", 50, 45, "ENDER_BOW");
        register("Cleave", -1, -1, "CLEAVER");
        register("Party Time!", -1, -1, "PARTY_HAT_CRAB");
        register("Giant's Slam", 100, 30, "GIANTS_SWORD");
        register("Snow Placer", -1, -1, "SNOW_SHOVEL");
        register("Greed", -1, -1, "MIDAS_SWORD");
        register("Clownin' Around", -1, 316, "STARRED_BONZO_MASK");
        register("Weather", -1, 5, "WEATHER_STICK");
        register("ME SMASH HEAD", 100, -1, "EDIBLE_MACE");
        register("Splash", 10, 1, "FISH_HAT");
        register("Deploy", -1, -1, "PLASMAFLUX_POWER_ORB");
        register("Dragon Rage", 100, -1, "ASPECT_OF_THE_DRAGON");
        register("Burning Souls", 400, -1, "PIGMAN_SWORD");
        register("Water Burst", 20, -1, "SALMON_CHESTPLATE_NEW");
        register("Fire Blast", 150, 30, "EMBER_ROD");
        register("Commander Whip", -1, -1, "ZOMBIE_COMMANDER_WHIP");
        register("Spooktacular", -1, -1, "GHOUL_BUSTER");
        register("Cleave", -1, -1, "HYPER_CLEAVER");
        register("Leap", 50, 1, "SILK_EDGE_SWORD");
        register("Throw", 150, 5, "LIVID_DAGGER");
        register("Double Jump", 50, -1, "SPIDER_BOOTS");
        register("Speed Boost", 50, -1, "ROGUE_SWORD");
        register("Spirit Glide", 250, 60, "THORNS_BOOTS");
        register("Sting", 100, -1, "STINGER_BOW");
        register("Roll em'", -1, -1, "MELON_DICER");
        register("Explosive Shot", -1, -1, "EXPLOSIVE_BOW");
        register("Heat-Seeking Rose", 35, 1, "FLOWER_OF_TRUTH");
        register("Small Heal", 60, 1, "WAND_OF_HEALING");
        register("Dreadlord", 40, -1, "CRYPT_DREADLORD_SWORD");
        register("Shadow Fury", -1, 15, "STARRED_SHADOW_FURY");
        register("Double Jump", 40, -1, "TARANTULA_BOOTS");
        register("Acupuncture", 200, 5, "VOODOO_DOLL");
        register("Showtime", 100, -1, "STARRED_BONZO_STAFF");
        register("Heartstopper", -1, -1, "SCORPION_FOIL");
        register("Rapid Fire", -1, 100, "MACHINE_GUN_BOW");
        register("Water Burst", 20, -1, "SALMON_HELMET");
        register("Alchemist's Bliss", -1, -1, "NETHER_WART_POUCH");
        register("Water Burst", 20, -1, "SALMON_CHESTPLATE");
        register("Shadow Fury", -1, 15, "SHADOW_FURY");
        register("Healing Boost", -1, -1, "ZOMBIE_HEART");
        register("Witherlord", 40, 3, "CRYPT_WITHERLORD_SWORD");
        register("Revive", -1, -1, "REVIVE_STONE");
        register("Rejuvenate", -1, -1, "VAMPIRE_MASK");
        register("Mist Aura", -1, -1, "SORROW_HELMET");
        register("Place Dirt", -1, -1, "INFINIDIRT_WAND");
        register("Clownin' Around", -1, 360, "BONZO_MASK");
        register("Shadow Warp", 300, 10, "SHADOW_WARP_SCROLL");
        register("Molten Wave", 500, 1, "MIDAS_STAFF");
        register("Growth", -1, 4, "GROWTH_HELMET");
        register("Howl", 150, 20, "WEIRD_TUBA");
        register("Medium Heal", 80, 1, "WAND_OF_MENDING");
        register("Throw", 20, -1, "AXE_OF_THE_SHREDDED");
        register("Ink Bomb", 60, 30, "INK_WAND");
        register("Whassup?", -1, -1, "AATROX_BATPHONE");
        register("Deploy", -1, -1, "MANA_FLUX_POWER_ORB");
        register("Extreme Focus", -1, -1, "END_STONE_BOW");
        register("Healing Boost", -1, -1, "CRYSTALLIZED_HEART");
        register("Mist Aura", -1, -1, "SORROW_LEGGINGS");
        register("Showtime", 100, -1, "BONZO_STAFF");
        register("Triple Shot", -1, -1, "RUNAANS_BOW");
        register("Water Burst", 20, -1, "SALMON_LEGGINGS_NEW");
        register("Rejuvenate", -1, -1, "VAMPIRE_WITCH_MASK");
        register("Terrain Toss", 250, 1, "YETI_SWORD");
        register("Instant Transmission", 50, -1, "ASPECT_OF_THE_END");
        register("Ether Transmission", 180, -1, "ASPECT_OF_THE_END");
        register("Ether Transmission", 180, -1, "ASPECT_OF_THE_VOID");
        register("Detonate", -1, 60, "CREEPER_LEGGINGS");
        register("Extreme Focus", -1, -1, "END_STONE_SWORD");
        register("Leap", 50, 1, "LEAPING_SWORD");
        register("Fun Guy Bonus", -1, -1, "FUNGI_CUTTER");
        register("Cleave", -1, -1, "GIANT_CLEAVER");
        register("Tempest", -1, -1, "HURRICANE_BOW");
        register("Big Heal", 100, 1, "WAND_OF_RESTORATION");
        register("Growth", -1, 4, "GROWTH_BOOTS");
        register("Stinger", 150, -1, "SCORPION_BOW");
        register("Eye Beam", -1, -1, "PRECURSOR_EYE");
        register("Water Burst", 20, -1, "SALMON_BOOTS");
        register("Mining Speed Boost", -1, 120, null);
        register("Pikobulus", -1, 110, null);
        // abilities

        register("Healing Circle", -1, 2, "DUNGEON_STONE");
        register("Wish", -1, 120, "DUNGEON_STONE");
        register("Guided Sheep", -1, 30, "DUNGEON_STONE");
        register("Thunderstorm", -1, 500, "DUNGEON_STONE");
        register("Throwing Axe", -1, 10, "DUNGEON_STONE");
        register("Ragnarok", -1, 60, "DUNGEON_STONE");
        register("Explosive Shot", -1, 40, "DUNGEON_STONE");
        register("Rapid Fire", -1, 100, "DUNGEON_STONE");
        register("Seismic Wave", -1, 15, "DUNGEON_STONE");
        register("Castle of Stone", -1, 150, "DUNGEON_STONE");
    }
    transient Pattern thePattern = Pattern.compile("§b-(\\d+) Mana \\(§6(.+)§b\\)");
    transient Pattern thePattern2 = Pattern.compile("§r§aUsed (.+)§r§a! §r§b\\((1194) Mana\\)§r");
    transient Pattern thePattern3 = Pattern.compile("§r§aUsed (.+)§r§a!§r");
    transient private String lastActionbarAbility;

    public FeatureAbilityCooldown() {
        super(true);
        MinecraftForge.EVENT_BUS.register(this);
        EventManager.INSTANCE.register(this);
    }

    static void register(String name, int manaCost, int cooldown, String itemId) {
        register(new SkyblockAbility(name, manaCost, cooldown, itemId));
    }

    static void register(SkyblockAbility skyblockAbility) {
        if (!skyblockAbilities.containsKey(skyblockAbility.getName()))
            skyblockAbilities.put(skyblockAbility.getName(), skyblockAbility);
        if (skyblockAbility.getItemId() != null && skyblockAbility.getCooldown() != -1) {
            List<SkyblockAbility> skyblockAbility1 = skyblockAbilitiesByItemID.computeIfAbsent(skyblockAbility.getItemId(), a -> new ArrayList<>());
            skyblockAbility1.add(skyblockAbilities.get(skyblockAbility.getName()));
            skyblockAbilitiesByItemID.put(skyblockAbility.getItemId(), skyblockAbility1);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tick) {
        if (tick.phase != TickEvent.Phase.END || tick.type != TickEvent.Type.CLIENT) return;
        if (!SkyblockStatus.isOnSkyblock()) return;
        EntityPlayerSP sp = Minecraft.getMinecraft().thePlayer;
        if (sp == null) return;
        if (sp.inventory == null || sp.inventory.armorInventory == null) return;
        for (ItemStack itemStack : sp.inventory.armorInventory) {
            checkForCooldown(itemStack);
        }
        for (ItemStack itemStack : sp.inventory.mainInventory) {
            checkForCooldown(itemStack);
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!SkyblockStatus.isOnSkyblock()) return;
        if (event.type == 2) {
            Matcher m = thePattern.matcher(event.message.getFormattedText());
            if (m.find()) {
                String name = m.group(2);
                if (!name.equalsIgnoreCase(lastActionbarAbility)) {
                    used(name);
                }
                lastActionbarAbility = name;
            } else {
                lastActionbarAbility = null;
            }
        } else {
            String message = event.message.getFormattedText();
            if (message.equals("§r§aYour §r§9Bonzo's Mask §r§asaved your life!§r")) {
                used("Clownin' Around");
            } else {
                Matcher m = thePattern2.matcher(message);
                if (m.matches()) {
                    String abilityName = TextUtils.stripColor(m.group(1));
                    used(abilityName);
                } else {
                    Matcher m2 = thePattern3.matcher(message);
                    if (m2.matches()) {
                        String abilityName = TextUtils.stripColor(m2.group(1));
                        used(abilityName);
                    } else if (message.startsWith("§r§aYou used your ") || message.endsWith("§r§aPickaxe Ability!§r")) {
                        String nocolor = TextUtils.stripColor(message);
                        String abilityName = nocolor.substring(nocolor.indexOf("your") + 5, nocolor.indexOf("Pickaxe") - 1);
                        used(abilityName);
                    }
                }
            }
        }
    }

    @Override
    protected boolean shouldShow() {
        return SkyblockStatus.isOnSkyblock();
    }

    @Override
    protected void getLines(List<String> lines, boolean example) {

        if(example){

            lines.add("Random Ability: 10s");
            lines.add("Random Ability2: 10m 9s");
            lines.add("Random Ability: READY");

            return;
        }

        for (UsedAbility usedAbility : usedAbilities) {
            long end = usedAbility.getCooldownEnd();
            if (System.currentTimeMillis() >= end) {
                if (System.currentTimeMillis() <= end + 20000) {
                    lines.add(usedAbility.getAbility().getName() + ": READY");
                }
            } else {
                StringBuilder thisline = new StringBuilder();
                thisline.append(usedAbility.getAbility().getName());
                thisline.append(": ");
                long millies = end - System.currentTimeMillis();
                double decimalPlaces = Math.pow(10, 1);
                millies = (long) (((millies - 1) / decimalPlaces + 1) * decimalPlaces);
                long hr = (millies / (1000 * 60 * 60));
                long min = ((millies / (1000 * 60)) % 60);
                double seconds = (millies / 1000.0) % 60;
                String secondStr = String.format("%." + (2) + "f", seconds);

                if (hr > 0) {
                    thisline.append(hr);
                    thisline.append("h ");
                }
                if (hr > 0 || min > 0) {
                    thisline.append(min);
                    thisline.append("m ");
                }
                if (hr > 0 || min > 0 || seconds > 0) {
                    thisline.append(secondStr);
                    thisline.append("s ");
                }
                lines.add(thisline.toString());
            }
        }
    }

    private void used(String ability) {
        if (skyblockAbilities.containsKey(ability)) {
            SkyblockAbility skyblockAbility = skyblockAbilities.get(ability);
            if (skyblockAbility.getCooldown() > 0) {
                UsedAbility usedAbility = new UsedAbility(skyblockAbility, System.currentTimeMillis() + skyblockAbility.getCooldown() * 1000);
                for (int i = 0; i < 3; i++) usedAbilities.remove(usedAbility);
                usedAbilities.add(usedAbility);
            }
        } else {
            System.out.println("Unknown ability: " + ability);
        }
    }

    public void checkForCooldown(ItemStack itemStack) {
        if (itemStack == null) return;
        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null) return;
        NBTTagCompound extra = nbt.getCompoundTag("ExtraAttributes");
        if (extra == null) return;
        String id = extra.getString("id");
        if (!skyblockAbilitiesByItemID.containsKey(id)) return;
        List<SkyblockAbility> skyblockAbility = skyblockAbilitiesByItemID.get(id);

        NBTTagCompound display = nbt.getCompoundTag("display");
        if (display == null) return;
        NBTTagList lore = display.getTagList("Lore", 8);
        int thecd;
        SkyblockAbility currentAbility = null;
        for (int i = 0; i < lore.tagCount(); i++) {
            String specific = lore.getStringTagAt(i);
            if (specific.startsWith("§8Cooldown: §a") && currentAbility != null) {
                String thecdstr = TextUtils.stripColor(specific).substring(10).trim();
                thecdstr = thecdstr.substring(0, thecdstr.length() - 1);
                thecd = Integer.parseInt(thecdstr);
                currentAbility.setCooldown(thecd);
                currentAbility = null;
            } else if (specific.startsWith("§6Item Ability: ")) {
                String ability = TextUtils.stripColor(specific).substring(14).trim();

                for (SkyblockAbility skyblockAbility1 : skyblockAbility) {
                    if (skyblockAbility1.getName().equals(ability)) {
                        currentAbility = skyblockAbility1;
                        break;
                    }
                }
            }
        }
    }
}

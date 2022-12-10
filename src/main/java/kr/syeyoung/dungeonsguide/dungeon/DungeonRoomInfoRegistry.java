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

import com.google.common.io.Files;
import com.google.gson.Gson;
import kr.syeyoung.dungeonsguide.Main;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.dungeon.newmechanics.DungeonMechanic;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class DungeonRoomInfoRegistry {
    static final Logger logger = LogManager.getLogger("DungeonRoomInfoRegistry");
    @Getter
    private static final List<DungeonRoomInfo> registered = new ArrayList<>();
    private static final Map<Short, List<DungeonRoomInfo>> shapeMap = new HashMap<>();
    private static final Map<UUID, DungeonRoomInfo> uuidMap = new HashMap<>();
    static Gson gson = new Gson();


    static DungeonMechanic dungeonMechanicTypeAdaptetr(kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic value) {

        if (value instanceof DungeonBreakableWall) {
            kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonBreakableWall aaa = new kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonBreakableWall();
            aaa.setSecretPoint(((DungeonBreakableWall) value).getSecretPoint());
            aaa.setPreRequisite(((DungeonBreakableWall) value).getPreRequisite());
            return aaa;
        }

        if (value instanceof DungeonDoor) {
            kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonDoor aaa = new kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonDoor();
            aaa.setSecretPoint(((DungeonDoor) value).getSecretPoint());
            aaa.setClosePreRequisite(((DungeonDoor) value).getClosePreRequisite());
            aaa.setOpenPreRequisite(((DungeonDoor) value).getOpenPreRequisite());
            return aaa;
        }

        if (value instanceof DungeonDummy) {
            kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonDummy aaa = new kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonDummy();
            aaa.setSecretPoint(((DungeonDummy) value).getSecretPoint());
            aaa.setPreRequisite(((DungeonDummy) value).getPreRequisite());
            return aaa;
        }

        if (value instanceof DungeonFairySoul) {
            kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonFairySoul aaa = new kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonFairySoul();
            aaa.setSecretPoint(((DungeonFairySoul) value).getSecretPoint());
            aaa.setPreRequisite(((DungeonFairySoul) value).getPreRequisite());
            return aaa;
        }


        if (value instanceof DungeonJournal) {
            kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonJournal aaa = new kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonJournal();
            aaa.setSecretPoint(((DungeonJournal) value).getSecretPoint());
            aaa.setPreRequisite(((DungeonJournal) value).getPreRequisite());
            return aaa;
        }

        if (value instanceof DungeonLever) {
            kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonLever aaa = new kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonLever();
            aaa.setLeverPoint(((DungeonLever) value).getLeverPoint());
            aaa.setTriggering(((DungeonLever) value).getTriggering());
            return aaa;
        }

        if (value instanceof DungeonNPC) {
            kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonNPC aaa = new kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonNPC();
            aaa.setSecretPoint(((DungeonNPC) value).getSecretPoint());
            aaa.setPreRequisite(((DungeonNPC) value).getPreRequisite());
            return aaa;
        }

        if (value instanceof DungeonOnewayDoor) {
            kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonOnewayDoor aaa = new kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonOnewayDoor();
            aaa.setSecretPoint(((DungeonOnewayDoor) value).getSecretPoint());
            aaa.setPreRequisite(((DungeonOnewayDoor) value).getPreRequisite());
            return aaa;
        }


        if (value instanceof DungeonOnewayLever) {
            kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonOnewayLever aaa = new kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonOnewayLever();
            aaa.setTriggering(((DungeonOnewayLever) value).getTriggering());
            aaa.setLeverPoint(((DungeonOnewayLever) value).getLeverPoint());
            aaa.setPreRequisite(((DungeonOnewayLever) value).getPreRequisite());
            return aaa;
        }

        if (value instanceof DungeonPressurePlate) {
            kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonPressurePlate aaa = new kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonPressurePlate();
            aaa.setPlatePoint(((DungeonPressurePlate) value).getPlatePoint());
            aaa.setTriggering(((DungeonPressurePlate) value).getTriggering());
            aaa.setPreRequisite(((DungeonPressurePlate) value).getPreRequisite());
            return aaa;
        }

//        if(value instanceof DungeonRoomDoor){
//            kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonRoomDoor aaa = new kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonRoomDoor();
//            aaa.setOffsetPoint(((DungeonRoomDoor) value).getOffsetPoint());
//            aaa.set
//            return aaa;
//        }

        if (value instanceof DungeonSecret) {
            kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonSecret aaa = new kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonSecret();
            aaa.setSecretPoint(((DungeonSecret) value).getSecretPoint());
            aaa.setSecretType(kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonSecret.SecretType.valueOf(((DungeonSecret) value).getSecretType().name()));
            aaa.setPreRequisite(((DungeonSecret) value).getPreRequisite());
            return aaa;
        }


        if (value instanceof DungeonTomb) {
            kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonTomb aaa = new kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl.DungeonTomb();
            aaa.setSecretPoint(((DungeonTomb) value).getSecretPoint());
            aaa.setPreRequisite(((DungeonTomb) value).getPreRequisite());
            return aaa;
        }

        return null;
    }

    public static void register(@NotNull DungeonRoomInfo dungeonRoomInfo) {

//        String configPath = String.valueOf(Main.getConfigDir());
//
//        if (configPath == null) {
//            configPath = System.getProperty("user.dir");
//        }
//
//
//        File file = new File(configPath + "/" + "rooms" + "/" + dungeonRoomInfo.getUuid() + ".json");
//        if (!file.exists()) {
//            try {
//                DungeonRoomInfoKotlin a = new DungeonRoomInfoKotlin();
//                a.setUserMade(dungeonRoomInfo.isUserMade());
//                a.setShape(dungeonRoomInfo.getShape());
//                a.setColor(dungeonRoomInfo.getColor());
//                a.setBlocks(dungeonRoomInfo.getBlocks());
//                a.setUuid(dungeonRoomInfo.getUuid());
//                a.setName(dungeonRoomInfo.getName());
//                a.setProcessorId(dungeonRoomInfo.getProcessorId());
//                a.setProperties(dungeonRoomInfo.getProperties());
//                a.setTotalSecrets(dungeonRoomInfo.getTotalSecrets());
//
//                logger.info("Serialising " + a.getName());
//                for (val mechanic : dungeonRoomInfo.getMechanics().entrySet()) {
//                    val key = mechanic.getKey();
//                    val value = mechanic.getValue();
//
//                    DungeonMechanic value1 = dungeonMechanicTypeAdaptetr(value);
//                    logger.info("mech name: " + key + " type: " + value1.getMechType());
//                    logger.info("Serialised Version: " + gson.toJson(value1));
//
//
//                    a.getMechanics().put(key, value1);
//
//                }
//                logger.info("all mechanics: " + gson.toJson(a.getMechanics()));
//                FileUtils.writeStringToFile(file, gson.toJson(a));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }


        if (uuidMap.containsKey(dungeonRoomInfo.getUuid())) {
            DungeonRoomInfo dri1 = uuidMap.get(dungeonRoomInfo.getUuid());
            registered.remove(dri1);
            shapeMap.get(dri1.getShape()).remove(dri1);
            uuidMap.remove(dri1.getUuid());
        }
        dungeonRoomInfo.setRegistered(true);
        registered.add(dungeonRoomInfo);
        uuidMap.put(dungeonRoomInfo.getUuid(), dungeonRoomInfo);
        List<DungeonRoomInfo> roomInfos = shapeMap.get(dungeonRoomInfo.getShape());
        if (roomInfos == null) {
            roomInfos = new ArrayList<>();
        }
        roomInfos.add(dungeonRoomInfo);
        shapeMap.put(dungeonRoomInfo.getShape(), roomInfos);
    }

    public static List<DungeonRoomInfo> getByShape(Short shape) {
        List<DungeonRoomInfo> dungeonRoomInfos = shapeMap.get(shape);
        return dungeonRoomInfos == null ? Collections.emptyList() : dungeonRoomInfos;
    }

    public static DungeonRoomInfo getByUUID(UUID uid) {
        return uuidMap.get(uid);
    }

    public static void unregister(DungeonRoomInfo dungeonRoomInfo) {
        if (!dungeonRoomInfo.isRegistered())
            throw new IllegalStateException("what tha fak? that is not registered one");
        if (!uuidMap.containsKey(dungeonRoomInfo.getUuid()))
            throw new IllegalStateException("what tha fak? that is not registered one, but you desperately wanted to trick this program");
        dungeonRoomInfo.setRegistered(false);
        registered.remove(dungeonRoomInfo);
        shapeMap.get(dungeonRoomInfo.getShape()).remove(dungeonRoomInfo);
        uuidMap.remove(dungeonRoomInfo.getUuid());
    }

    public static void saveAll(File dir) {
        dir.mkdirs();
        StringBuilder nameidstring = new StringBuilder("name,uuid,processsor,secrets");
        StringBuilder ids = new StringBuilder();
        for (DungeonRoomInfo dungeonRoomInfo : registered) {
            try {
                if (dungeonRoomInfo.isUserMade()) {
                    try (FileOutputStream fos = new FileOutputStream(new File(dir, dungeonRoomInfo.getUuid().toString() + ".roomdata"))) {
                        try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                            oos.writeObject(dungeonRoomInfo);
                            oos.flush();
                        }
                    }

                    nameidstring.append("\n")
                            .append(dungeonRoomInfo.getName())
                            .append(",")
                            .append(dungeonRoomInfo.getUuid())
                            .append(",")
                            .append(dungeonRoomInfo.getProcessorId())
                            .append(",")
                            .append(dungeonRoomInfo.getTotalSecrets());

                    ids.append("roomdata/")
                            .append(dungeonRoomInfo.getUuid())
                            .append(".roomdata\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            Files.write(nameidstring.toString(), new File(dir, "roomidmapping.csv"), Charset.defaultCharset());
            Files.write(ids.toString(), new File(dir, "datas.txt"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadAll(File dir) throws IOException {
        registered.clear();
        shapeMap.clear();
        uuidMap.clear();
        List<String> lines = IOUtils.readLines(Objects.requireNonNull(Main.class.getResourceAsStream("/roomdata/datas.txt")));
        for (String name : lines) {
            if (name.endsWith(".roomdata")) {
                try {
                    DungeonRoomInfo dri;
                    try (InputStream fis = Main.class.getResourceAsStream("/" + name)) {
                        try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                            dri = (DungeonRoomInfo) ois.readObject();
                        }
                    }
                    register(dri);
                } catch (Exception e) {
                    logger.error(name);
                    e.printStackTrace();
                }
            }
        }
        if (dir != null) {
            for (File f : Objects.requireNonNull(dir.listFiles())) {
                if (!f.getName().endsWith(".roomdata")) continue;
                try {
                    DungeonRoomInfo dri;
                    try (InputStream fis = java.nio.file.Files.newInputStream(f.toPath())) {
                        try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                            dri = (DungeonRoomInfo) ois.readObject();
                        }
                    }
                    register(dri);
                } catch (Exception e) {
                    logger.error(f.getName());
                    e.printStackTrace();
                }
            }
        }

    }

}

package kr.syeyoung.dungeonsguide.dungeon

import com.google.common.io.Files
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kr.syeyoung.dungeonsguide.Main
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic
import kr.syeyoung.dungeonsguide.dungeon.mechanics.impl.*
import kr.syeyoung.dungeonsguide.dungeon.room.data.RoomShape
import kr.syeyoung.dungeonsguide.dungeon.roomdetection.NewDungeonRoomBuilder
import kr.syeyoung.dungeonsguide.utils.RuntimeTypeAdapterFactory
import kr.syeyoung.dungeonsguide.utils.SuperclassExclusionStrategy
import lombok.Getter
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.*
import java.nio.charset.Charset
import java.util.*

object DungeonRoomInfoRegistry {
    val logger: Logger = LogManager.getLogger("DungeonRoomInfoRegistry")

    @Getter
    private val registered: MutableList<DungeonRoomInfo?> = ArrayList()
    private val shapeMap: MutableMap<Short, MutableList<DungeonRoomInfo>> = HashMap()
    private val uuidMap: MutableMap<UUID, DungeonRoomInfo> = HashMap()


    private val typeFactory: RuntimeTypeAdapterFactory<DungeonMechanic> = RuntimeTypeAdapterFactory
        .of(DungeonMechanic::class.java, "mechType")
        .registerSubtype(DungeonBreakableWall::class.java, "BreakableWall")
        .registerSubtype(DungeonDoor::class.java, "Door")
        .registerSubtype(DungeonDummy::class.java, "Dummy")
        .registerSubtype(DungeonFairySoul::class.java, "Fairysoul")
        .registerSubtype(DungeonJournal::class.java, "Journal")
        .registerSubtype(DungeonLever::class.java, "Lever")
        .registerSubtype(DungeonNPC::class.java, "Npc")
        .registerSubtype(DungeonOnewayDoor::class.java, "OnewayDoor")
        .registerSubtype(DungeonOnewayLever::class.java, "OnewayLever")
        .registerSubtype(DungeonPressurePlate::class.java, "PressurePlate")
        .registerSubtype(DungeonRoomDoor::class.java, "RoomDoor")
        .registerSubtype(DungeonSecret::class.java, "Secret")
        .registerSubtype(DungeonTomb::class.java, "Tomb")
    val gson: Gson = GsonBuilder()
        .registerTypeAdapterFactory(typeFactory)
        .setPrettyPrinting()
        .addDeserializationExclusionStrategy(SuperclassExclusionStrategy())
        .addSerializationExclusionStrategy(SuperclassExclusionStrategy())
        .create();

    val thashapes: MutableSet<Short> = HashSet()

    @JvmStatic
    fun register(dungeonRoomInfo: DungeonRoomInfo) {
        logger.info("loading room ${dungeonRoomInfo.name} shape: ${dungeonRoomInfo.shape} ")
        if (uuidMap.containsKey(dungeonRoomInfo.uuid)) {
            val dri1 = uuidMap[dungeonRoomInfo.uuid]
            registered.remove(dri1)
            shapeMap[dri1!!.shape]!!.remove(dri1)
            uuidMap.remove(dri1.uuid)
        }
        dungeonRoomInfo.registered = true
        registered.add(dungeonRoomInfo)
        uuidMap[dungeonRoomInfo.uuid] = dungeonRoomInfo

        val roomInfos = shapeMap[dungeonRoomInfo.shape] ?: ArrayList()

        roomInfos.add(dungeonRoomInfo)



        dungeonRoomInfo.shape?.let {

            if (!thashapes.contains(it)) {
                logger.info("found new shape $it, amount of shapes ${thashapes.size}")
                val builder = StringBuilder()
                (0..3).forEach { dy ->
                    builder.append("\n")
                    (0..3).forEach { dx ->
                        val isSet = it.toInt() shr dy * 4 + dx and 0x1 != 0
                        builder.append(if (isSet) "O" else "X")
                    }
                }
                println("Shape visual: $builder")
                thashapes.add(it)
            }

            shapeMap[it] = roomInfos
        }

    }
    @JvmStatic
    fun getByShape(shape: Short): MutableList<DungeonRoomInfo> {
        return shapeMap[shape]?.toMutableList() ?: mutableListOf()
    }

    @JvmStatic
    fun getRoomInfosByShape(inShape: RoomShape): List<DungeonRoomInfo> {

        val tempMap: MutableList<DungeonRoomInfo> = ArrayList()
        NewDungeonRoomBuilder.shapeMap.forEach { (thaShortShape, shape) ->
            if (shape == inShape) {
                tempMap.addAll(shapeMap[thaShortShape.toShort()] ?: ArrayList())
            }
        }

        return tempMap
    }

    @JvmStatic
    fun getByUUID(uid: UUID): DungeonRoomInfo? {
        return uuidMap[uid]
    }

    fun unregister(dungeonRoomInfo: DungeonRoomInfo) {
        check(dungeonRoomInfo.registered || uuidMap.containsKey(dungeonRoomInfo.uuid)) { "tried to unregister a unregistered roomInfo" }
        dungeonRoomInfo.registered = false
        registered.remove(dungeonRoomInfo)
        shapeMap[dungeonRoomInfo.shape]?.remove(dungeonRoomInfo)
        uuidMap.remove(dungeonRoomInfo.uuid)
    }
    @JvmStatic
    fun saveAll(dir: File) {
        dir.mkdirs()
        val nameidstring = StringBuilder("name,uuid,processsor,secrets")
        val ids = StringBuilder()
        for (dungeonRoomInfo in registered) {
            try {
                if (dungeonRoomInfo!!.isUserMade) {
                    FileOutputStream(File(dir, dungeonRoomInfo.uuid.toString() + ".roomdata")).use { fos ->
                        ObjectOutputStream(fos).use { oos ->
                            oos.writeObject(dungeonRoomInfo)
                            oos.flush()
                        }
                    }
                    nameidstring.append("\n")
                        .append(dungeonRoomInfo.name)
                        .append(",")
                        .append(dungeonRoomInfo.uuid)
                        .append(",")
                        .append(dungeonRoomInfo.processorId)
                        .append(",")
                        .append(dungeonRoomInfo.totalSecrets)
                    ids.append("roomdata/")
                        .append(dungeonRoomInfo.uuid)
                        .append(".roomdata\n")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        try {
            Files.write(nameidstring.toString(), File(dir, "roomidmapping.csv"), Charset.defaultCharset())
            Files.write(ids.toString(), File(dir, "datas.txt"), Charset.defaultCharset())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    @JvmStatic
    fun loadAll(dir: File) {
        registered.clear()
        shapeMap.clear()
        uuidMap.clear()
        val lines = IOUtils.readLines(
            Objects.requireNonNull(
                Main::class.java.getResourceAsStream("/roomdataindex.txt")
            )
        )
        for (name in lines) {
            if (name.endsWith(".json")) {
                try {
                    Main::class.java.getResourceAsStream("/$name")
                        .use { fis ->
                            fis?.let {
                                InputStreamReader(fis).use { yas ->
                                    register(gson.fromJson(yas, DungeonRoomInfo::class.java))
                                }
                            }
                        }
                } catch (e: Exception) {
                    logger.error(name)
                    e.printStackTrace()
                }
            }
        }
        for (f in Objects.requireNonNull(dir.listFiles())) {
            if (!f.name.endsWith(".json")) continue
            try {
                java.nio.file.Files.newInputStream(f.toPath())
                    .use { fis ->
                        InputStreamReader(fis).use { yas ->
                            register(gson.fromJson(yas, DungeonRoomInfo::class.java))
                        }
                    }
            } catch (e: Exception) {
                logger.error(f.name)
                e.printStackTrace()
            }
        }
    }
}
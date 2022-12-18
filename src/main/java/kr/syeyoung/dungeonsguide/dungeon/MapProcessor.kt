package kr.syeyoung.dungeonsguide.dungeon

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.util.concurrent.ThreadFactoryBuilder
import kr.syeyoung.dungeonsguide.DungeonsGuide
import kr.syeyoung.dungeonsguide.chat.ChatTransmitter
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonSpecificDataProviderRegistry
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.EDungeonDoorType
import kr.syeyoung.dungeonsguide.events.impl.DungeonContextInitializationEvent
import kr.syeyoung.dungeonsguide.features.impl.dungeon.FeatureCollectScore
import kr.syeyoung.dungeonsguide.utils.MapUtils
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus
import kr.syeyoung.dungeonsguide.utils.simple.SimpleFuse
import kr.syeyoung.dungeonsguide.utils.simple.SimpleLock
import kr.syeyoung.dungeonsguide.utils.simple.SimpleTimeFuse
import kr.syeyoung.dungeonsguide.utils.simple.SimpleTimer
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemMap
import net.minecraft.util.Vec3
import net.minecraft.world.storage.MapData
import net.minecraftforge.common.MinecraftForge
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.joml.Vector2d
import org.joml.Vector2i
import org.joml.Vector3i
import java.awt.Color
import java.awt.Dimension
import java.awt.Rectangle
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.floor

class MapProcessor(private val context: DungeonContext) {
    private val processMapThrottle = SimpleTimer(5)
    private val waitDelay = SimpleTimeFuse(100)
    private val processMapLock = SimpleLock()

    private val mapIconToPlayerMap: BiMap<String, String> = HashBiMap.create()
    private var processedFinishedMapFuse = SimpleFuse()

    private val roomsFound: MutableList<Vector2i> = ArrayList()

    /**
     * If the player on the map is closer than value this it won't save it
     * this should be done with render-distance but whateva
     */
    private var closenessDistance = 50

    private val es: ExecutorService =
        Executors.newSingleThreadExecutor(ThreadFactoryBuilder().setNameFormat("Dg-MapProcessor-%d").build())

    var unitRoomDimension: Dimension? = null
    var doorDimensions: Dimension? = null // width: width of door, height: gap between rooms
    var topLeftMapPoint: Vector2i? = null

    var isInitialized = false
        private set
    var undiscoveredRoom = 0

    var latestMapData: MapData? = null
        private set

    fun tick() {
        Minecraft.getMinecraft().thePlayer ?: return
        if (waitDelay.isBlown) {
            val stack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(8)
            if (stack == null || stack.item !is ItemMap) {
                return
            }
            val mapData = (stack.item as ItemMap).getMapData(stack, mc.theWorld)
            if (mapData != null) {
                processMapThrottle.tick()
                if (processMapThrottle.shouldRun() && processMapLock.isUnlocked) {
                    processMapData(mapData)
                }
                latestMapData = mapData
                if (mapIconToPlayerMap.size < context.players.size && this.isInitialized) {
                    getPlayersFromMap(latestMapData!!)
                }
            }
        }
    }

    private fun processMapData(mapData: MapData) {
        val mapColorData = mapData.colors

        es.execute {
            processMapLock.lock()
            logger.info("processing MapData")
            if (doorDimensions == null || !isInitialized) {
                assembleMap(mapColorData)
            } else {
                processMap(mapColorData)
            }
            if (context.isEnded) {
                processFinishedMap(mapColorData)
            }
            processMapLock.unLock()
        }
    }

    private fun assembleMap(mapData: ByteArray) {
        val firstRoom = MapUtils.findFirstColorWithIn(mapData, 30.toByte(), Rectangle(0, 0, 128, 128)) ?: return
        val width = MapUtils.getWidthOfColorAt(mapData, 30.toByte(), firstRoom)
        val height = MapUtils.getHeightOfColorAt(mapData, 30.toByte(), firstRoom)
        val unitRoomDimension = Dimension(width, height)
        var doorDir: Vector2i? = null
        val midfirstRoom =
            Vector2i(firstRoom.x + (unitRoomDimension.width / 2), firstRoom.y + (unitRoomDimension.height / 2))
        val halfWidth = (unitRoomDimension.width / 2) + 2
        for (v in directions) {
            val color = MapUtils.getMapColorAt(
                mapData, (v.x * halfWidth + midfirstRoom.x), (v.y * halfWidth + midfirstRoom.y)
            )
            if (color.toInt() != 0) {
                doorDir = v
                break
            }
        }
        if (doorDir == null) {
            return
        }
        val basePoint = Vector2i(firstRoom.x, firstRoom.y)
        if (doorDir.x > 0) {
            basePoint.x += unitRoomDimension.width
        }
        if (doorDir.x < 0) {
            basePoint.x -= 1
        }
        if (doorDir.y > 0) {
            basePoint.y += unitRoomDimension.height
        }
        if (doorDir.y < 0) {
            basePoint.y -= 1
        }
        val gap = MapUtils.getLengthOfColorExtending(mapData, 0.toByte(), basePoint, doorDir)
        val pt = MapUtils.findFirstColorWithInNegate(
            mapData, 0.toByte(), Rectangle(
                basePoint.x,
                basePoint.y,
                abs(doorDir.y) * unitRoomDimension.width + 1,
                abs(doorDir.x) * unitRoomDimension.height + 1
            )
        ) ?: return

        val doorWidth = MapUtils.getLengthOfColorExtending(
            mapData, MapUtils.getMapColorAt(mapData, pt.x, pt.y), pt, Vector2i(
                abs(doorDir.y), abs(doorDir.x)
            )
        )
        val doorDimensions = Dimension(doorWidth, gap)

        // Determine Top Left
        var topLeftX = firstRoom.x
        var topLeftY = firstRoom.y
        while (topLeftX >= unitRoomDimension.width + doorDimensions.height) topLeftX -= unitRoomDimension.width + doorDimensions.height
        while (topLeftY >= unitRoomDimension.height + doorDimensions.height) topLeftY -= unitRoomDimension.height + doorDimensions.height
        val topLeftMapPoint = Vector2i(topLeftX, topLeftY)

        // determine door location based on npc, and determine map min from there
        val doorFinder = DungeonSpecificDataProviderRegistry.getDoorFinder(SkyblockStatus.dungeonNameStrriped) ?: return
        val door = doorFinder.findDoor(mc.theWorld, SkyblockStatus.dungeonNameStrriped) ?: return
        ChatTransmitter.sendDebugChat("door Pos: $door")
        val unitPoint = mapPointToRoomPoint(firstRoom, topLeftMapPoint, unitRoomDimension, doorDimensions)
        unitPoint.x += unitPoint.x + 1
        unitPoint.y += unitPoint.y + 1

        unitPoint.x += doorDir.x
        unitPoint.y += doorDir.y

        val offset = doorFinder.findDoorOffset(mc.theWorld, SkyblockStatus.dungeonNameStrriped)
        val worldX = unitPoint.x * 16
        val worldY = unitPoint.y * 16
        context.dungeonMin = door.add(-worldX, 0, -worldY)
        ChatTransmitter.sendDebugChat("Found Green room: $firstRoom")
        ChatTransmitter.sendDebugChat("Axis match: ${doorDir == offset}")
        ChatTransmitter.sendDebugChat("World Min: ${context.dungeonMin}")
        ChatTransmitter.sendDebugChat("Dimension: $unitRoomDimension")
        ChatTransmitter.sendDebugChat("top Left: $topLeftMapPoint")
        ChatTransmitter.sendDebugChat("door dimension: $doorDimensions")
        MinecraftForge.EVENT_BUS.post(DungeonContextInitializationEvent())

        this.unitRoomDimension = unitRoomDimension
        this.topLeftMapPoint = topLeftMapPoint
        this.isInitialized = true
        this.doorDimensions = doorDimensions
    }

    private fun mapPointToWorldPoint(inX: Int, inY: Int): Vector3i {
        val x =
            ((inX - topLeftMapPoint!!.x) / (unitRoomDimension!!.width.toDouble() + doorDimensions!!.height) * 32 + context.dungeonMin!!.x).toInt()
        val y =
            ((inY - topLeftMapPoint!!.y) / (unitRoomDimension!!.height.toDouble() + doorDimensions!!.height) * 32 + context.dungeonMin!!.z).toInt()
        return Vector3i(x, 70, y)
    }

    fun roomPointToMapPoint(roomPoint: Vector2i): Vector2i {
        return Vector2i(
            roomPoint.x * (unitRoomDimension!!.width + doorDimensions!!.height) + topLeftMapPoint!!.x,
            roomPoint.y * (unitRoomDimension!!.height + doorDimensions!!.height) + topLeftMapPoint!!.y
        )
    }

    fun roomPointToWorldPoint(roomPoint: Vector2i): Vector3i {
        return Vector3i(
            context.dungeonMin!!.x + roomPoint.x * 32, context.dungeonMin!!.y, context.dungeonMin!!.z + roomPoint.y * 32
        )
    }

    fun worldPointToRoomPoint(worldPoint: Vector3i): Vector2i? {
        return if (context.dungeonMin == null) null else {
            Vector2i(
                (worldPoint.x - context.dungeonMin!!.x) / 32, (worldPoint.z - context.dungeonMin!!.z) / 32
            )
        }
    }

    fun worldPointToMapPoint(worldPoint: Vec3): Vector2i? {
        return if (context.dungeonMin == null) {
            null
        } else {
            Vector2i(
                topLeftMapPoint!!.x + ((worldPoint.xCoord - context.dungeonMin!!.x) / 32.0f * (unitRoomDimension!!.width + doorDimensions!!.height)).toInt(),
                topLeftMapPoint!!.y + ((worldPoint.zCoord - context.dungeonMin!!.z) / 32.0f * (unitRoomDimension!!.height + doorDimensions!!.height)).toInt()
            )
        }
    }

    fun worldPointToMapPointFLOAT(worldPoint: Vec3): Vector2d? {
        if (context.dungeonMin == null) {
            return null
        }
        val x =
            topLeftMapPoint!!.x + (worldPoint.xCoord - context.dungeonMin!!.x) / 32.0f * (unitRoomDimension!!.width + doorDimensions!!.height)
        val y =
            topLeftMapPoint!!.y + (worldPoint.zCoord - context.dungeonMin!!.z) / 32.0f * (unitRoomDimension!!.height + doorDimensions!!.height)
        return Vector2d(x, y)
    }

    enum class MapColor(val color: Int) {
        RED(18), GREEN(30), GRAY(85), BROWN(35), MAYBEBROWN(34), NONE(0), IDKMAN(-1);
    }

    private fun getMapColor(color: Int): MapColor {
        return when (color) {
            0 -> MapColor.NONE
            18 -> MapColor.RED
            30 -> MapColor.GREEN
            34 -> MapColor.MAYBEBROWN
            35 -> MapColor.BROWN
            85 -> MapColor.GRAY
            else -> MapColor.IDKMAN
        }
    }

    private fun processMap(mapData: ByteArray) {
        val topLeftMapPointCopy = topLeftMapPoint?.clone() as Vector2i
        val unitRoomDimensionCopy = unitRoomDimension?.clone() as Dimension
        val doorDimensionsCopy = doorDimensions?.clone() as Dimension

        val roomHeight =
            ((128.0 - topLeftMapPointCopy.y) / (unitRoomDimensionCopy.height + doorDimensionsCopy.height)).toInt()
        val roomWidth =
            ((128.0 - topLeftMapPointCopy.x) / (unitRoomDimensionCopy.width + doorDimensionsCopy.height)).toInt()
        if (MapUtils.getMapColorAt(mapData, 0, 0).toInt() != 0) return
        undiscoveredRoom = 0
        for (y in 0..roomHeight) {
            for (x in 0..roomWidth) {
                val currentPoint = Vector2i(x, y)
                val mapPoint = roomPointToMapPoint(currentPoint)
                MapUtils.record(mapData, mapPoint.x, mapPoint.y, Color(255, 255, 0, 80))
                val currentColor = getMapColor(MapUtils.getMapColorAt(mapData, mapPoint.x, mapPoint.y).toInt())
                val dungeonRoom = context.roomMapper[currentPoint]

                if (dungeonRoom != null) {
                    if ((currentColor == MapColor.RED) && (dungeonRoom.currentState != RoomState.FINISHED)) {
                        dungeonRoom.currentState = RoomState.COMPLETE_WITHOUT_SECRETS
                        dungeonRoom.totalSecrets = 0
                    } else if (currentColor == MapColor.GREEN) {
                        dungeonRoom.currentState = RoomState.FINISHED
                        dungeonRoom.totalSecrets = 0
                    } else if (dungeonRoom.currentState != RoomState.FINISHED) {
                        MapUtils.record(
                            mapData,
                            mapPoint.x + unitRoomDimensionCopy.width / 2,
                            mapPoint.y + unitRoomDimensionCopy.height / 2,
                            Color(0, 255, 0, 80)
                        )
                        val centerColor = getMapColor(
                            MapUtils.getMapColorAt(
                                mapData,
                                mapPoint.x + unitRoomDimensionCopy.width / 2,
                                mapPoint.y + unitRoomDimensionCopy.height / 2
                            ).toInt()
                        )
                        when (centerColor) {
                            MapColor.MAYBEBROWN -> {
                                dungeonRoom.currentState = RoomState.COMPLETE_WITHOUT_SECRETS
                            }

                            MapColor.GREEN -> {
                                dungeonRoom.currentState = RoomState.FINISHED
                            }

                            MapColor.RED -> {
                                dungeonRoom.currentState = RoomState.FAILED
                            }

                            else -> {}
                        }
                    }
                    if (dungeonRoom.totalSecrets == -1) {
                        val toInt = dungeonRoom.color.toInt()
                        if (toInt == 82 || toInt == 74) {
                            dungeonRoom.totalSecrets = 0
                        }
                        MapUtils.record(mapData, mapPoint.x, mapPoint.y + 1, Color(0, 255, 0, 80))
                    }
                } else if (currentColor == MapColor.GRAY) {
                    undiscoveredRoom++
                } else if (currentColor != MapColor.NONE) {
                    MapUtils.record(mapData, mapPoint.x, mapPoint.y, Color(0, 255, 255, 80))
                    val room = buildRoom(mapData, currentPoint) ?: continue

                    // USELESS DEBUG CODE
                    ChatTransmitter.sendDebugChat("New Map discovered! shape: ${room.shape} color: ${room.color} unitPos: $x,$y mapMin: ${room.min} mapMx: ${room.max}")
                    val builder = StringBuilder()
                    (0..3).forEach { dy ->
                        builder.append("\n")
                        (0..3).forEach { dx ->
                            val isSet = room.shape.toInt() shr dy * 4 + dx and 0x1 != 0
                            builder.append(if (isSet) "O" else "X")
                        }
                    }
                    ChatTransmitter.sendDebugChat("Shape visual: $builder")
                    // END

                    context.dungeonRoomList.add(room)
                    room.unitPoints.forEach {
                        roomsFound.add(it)
                        context.roomMapper[it] = room
                    }
                    if (room.roomProcessor.readGlobalChat()) {
                        context.globalRoomProcessors.add(room.roomProcessor)
                    }
                }
            }
        }
    }

    private fun buildRoom(mapData: ByteArray, roomPoint: Vector2i): DungeonRoom? {
        val toCheck: Queue<Array<Vector2i>> = LinkedList()
        toCheck.add(arrayOf(roomPoint, roomPoint))
        val checked: MutableSet<Vector2i> = HashSet()
        val ayConnected: MutableList<Vector2i> = ArrayList()
        var minX = Int.MAX_VALUE
        var minY = Int.MAX_VALUE
        var maxX = 0
        var maxY = 0
        while (toCheck.peek() != null) {
            val check = toCheck.poll() ?: continue

            if (checked.contains(check[1])) {
                continue
            }
            checked.add(check[1])
            if (checkIfConnected(mapData, check[0], check[1])) {
                ayConnected.add(check[1])
                if (check[1].x < minX) minX = check[1].x
                if (check[1].y < minY) minY = check[1].y
                if (check[1].x > maxX) maxX = check[1].x
                if (check[1].y > maxY) maxY = check[1].y
                for (dir in directions) {
                    val newPt = Vector2i(check[1].x + dir.x, check[1].y + dir.y)
                    toCheck.add(arrayOf(check[1], newPt))
                }
            }
        }
        var shape: Short = 0
        for (p in ayConnected) {
            val localX = p.x - minX
            val localY = p.y - minY
            shape = (shape.toInt() or (((1 shl localY) * 4) + localX)).toShort()
        }
        val doors: MutableSet<Vector2d> = HashSet()
        for (p in ayConnected) {
            for (v in door_dirs) {
                val v2 = Vector2d(p.x + v.x, p.y + v.y)
                if (doors.contains(v2)) doors.remove(v2) else doors.add(v2)
            }
        }
        val pt2 = roomPointToMapPoint(ayConnected[0])
        val unit1 = MapUtils.getMapColorAt(mapData, pt2.x, pt2.y)

        // 0: none 1: open door 2. unopen door 3: wither door 4. red door
        val doorsAndStates: MutableSet<Pair<Vector2d, EDungeonDoorType>> = HashSet()
        val halfWidth = unitRoomDimension!!.width + 4
        for (door in doors) {
            val floorX = floor(door.x).toInt()
            val floorY = floor(door.y).toInt()
            val mapPt = roomPointToMapPoint(Vector2i(floorX, floorY))
            val someX = mapPt.x + (unitRoomDimension!!.width / 2) + (halfWidth * (door.x - floorX)).toInt()
            val someY = mapPt.y + (unitRoomDimension!!.height / 2) + (halfWidth * (door.y - floorY)).toInt()
            MapUtils.record(mapData, someX, someY, Color.green)
            val vector2d = Vector2d(door.x - minX, door.y - minY)
            when (MapUtils.getMapColorAt(mapData, someX, someY).toInt()) {
                0 -> {
                    doorsAndStates.add(Pair(vector2d, EDungeonDoorType.NONE))
                }

                18 -> {
                    if (unit1.toInt() != 18) {
                        doorsAndStates.add(Pair(vector2d, EDungeonDoorType.BLOOD))
                    }
                }

                85 -> {
                    doorsAndStates.add(Pair(vector2d, EDungeonDoorType.UNOPEN))
                }

                119 -> {
                    doorsAndStates.add(Pair(vector2d, EDungeonDoorType.WITHER))
                }

                else -> {
                    doorsAndStates.add(Pair(vector2d, EDungeonDoorType.ENTRANCE))
                }
            }
        }
        return try {
            DungeonRoom(
                ayConnected,
                shape,
                unit1,
                roomPointToWorldPoint(Vector2i(minX, minY)),
                roomPointToWorldPoint(Vector2i(maxX + 1, maxY + 1)).add(-1, 0, -1),
                context,
                doorsAndStates
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun checkIfConnected(mapData: ByteArray, unitPoint1: Vector2i, unitPoint2: Vector2i): Boolean {
        if (unitPoint1 === unitPoint2) return true
        if (unitPoint1 == unitPoint2) return true
        val high = if (unitPoint2.y > unitPoint1.y) {
            Vector2i(unitPoint2)
        } else {
            if (unitPoint2.x > unitPoint1.x) {
                Vector2i(unitPoint2)
            } else {
                Vector2i(unitPoint1)
            }
        }
        val low = if (high == unitPoint2) {
            Vector2i(unitPoint1)
        } else {
            Vector2i(unitPoint2)
        }
        val xOff = low.x - high.x
        val yOff = low.y - high.y
        val pt = roomPointToMapPoint(high)
        val pt2 = roomPointToMapPoint(low)
        val unit1 = MapUtils.getMapColorAt(mapData, pt.x, pt.y)
        val unit2 = MapUtils.getMapColorAt(mapData, pt2.x, pt2.y)
        pt.add(xOff, yOff)
        val unit3 = MapUtils.getMapColorAt(mapData, pt.x, pt.y)
        return unit1 == unit2 && unit2 == unit3 && unit1.toInt() != 0
    }


    private fun processFinishedMap(mapData: ByteArray) {
        if (MapUtils.getMapColorAt(mapData, 0, 0).toInt() == 0) {
            return
        }
        if (processedFinishedMapFuse.checkAndBlow()) {
            MapUtils.clearMap()
            MapUtils.record(mapData, 0, 0, Color.GREEN)
            FeatureCollectScore.collectDungeonRunData(mapData, context)
        }
    }

    private fun getPlayersFromMap(mapdata: MapData) {
        if (DungeonsGuide.getDungeonsGuide().verbose) logger.info("Getting players from map")
        for ((mapDecString, vec4) in mapdata.mapDecorations) {
            if (!mapIconToPlayerMap.containsValue(mapDecString)) {
                if (DungeonsGuide.getDungeonsGuide().verbose) logger.info("mapIconToPlayerMap dosent have Player")
                val x = vec4.func_176112_b() / 2 + 64
                val y = vec4.func_176113_c() / 2 + 64
                val mapPos = mapPointToWorldPoint(x, y)
                var potentialPlayer: String? = null
                for (player in context.players) {
                    if (DungeonsGuide.getDungeonsGuide().verbose) logger.info(
                        "Player: {} isNear: {} ", player, isPlayerNear(player, mapPos)
                    )
                    if (!mapIconToPlayerMap.containsKey(player)) {
                        if (DungeonsGuide.getDungeonsGuide().verbose) logger.info("Potential profile is: $player")
                        potentialPlayer = player
                        break
                    }
                }
                if (potentialPlayer != null) {
                    if (DungeonsGuide.getDungeonsGuide().verbose) {
                        logger.info("potentialPlayer is not null")
                    }
                    var shouldSave = true
                    for ((_, vall) in mapdata.mapDecorations) {
                        val x2 = vall.func_176112_b() / 2 + 64
                        val y2 = vall.func_176113_c() / 2 + 64
                        val dx = x2 - x
                        val dy = y2 - y
                        if (((dx * dx) + (dy * dy)) < closenessDistance) {
                            shouldSave = false
                            break
                        }
                    }
                    if (shouldSave) {
                        if (DungeonsGuide.getDungeonsGuide().verbose) {
                            logger.info("added $potentialPlayer to mapIconPlayerMap with $mapDecString")
                        }
                        if (mapIconToPlayerMap.containsKey(potentialPlayer)) {
                            mapIconToPlayerMap.replace(potentialPlayer, mapDecString)
                        } else {
                            mapIconToPlayerMap[potentialPlayer] = mapDecString
                        }
                        if (DungeonsGuide.getDungeonsGuide().verbose) logger.info("mapIconToPlayerMap:")
                        if (DungeonsGuide.getDungeonsGuide().verbose) {
                            mapIconToPlayerMap.forEach { (key: String?, value: String?) ->
                                logger.info("  $key: $value")
                            }
                        }
                    } else {
                        if (DungeonsGuide.getDungeonsGuide().verbose) logger.info("shouldSave is false")
                    }
                } else {
                    if (DungeonsGuide.getDungeonsGuide().verbose) {
                        logger.info("potentialPlayer is null")
                    }
                }
            } else {
                if (DungeonsGuide.getDungeonsGuide().verbose) logger.info("mapIconToPlayerMap has player ")
            }
        }
    }

    private fun isPlayerNear(player: String, mapPos: Vector3i): Boolean {
        val entityPlayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(player)
        if (entityPlayer != null && !entityPlayer.isInvisible) {
            val pos = entityPlayer.position
            val dx = mapPos.x - pos.x
            val dz = mapPos.z - pos.z
            return ((dx * dx) + (dz * dz)) < closenessDistance
        }
        return false
    }

    private fun mapPointToRoomPoint(
        mapPoint: Vector2i,
        topLeftMapPoint: Vector2i,
        unitRoomDimension: Dimension,
        doorDimensions: Dimension,
    ): Vector2i {
        val x =
            ((mapPoint.x - topLeftMapPoint.x) / (unitRoomDimension.width.toDouble() + doorDimensions.height)).toInt()
        val y =
            ((mapPoint.y - topLeftMapPoint.y) / (unitRoomDimension.height.toDouble() + doorDimensions.height)).toInt()
        return Vector2i(x, y)
    }

    companion object {
        private val directions: Set<Vector2i> = setOf(Vector2i(0, 1), Vector2i(0, -1), Vector2i(1, 0), Vector2i(-1, 0))
        private val door_dirs: Set<Vector2d> =
            setOf(Vector2d(0.0, 0.5), Vector2d(0.0, -0.5), Vector2d(0.5, 0.0), Vector2d(-0.5, 0.0))
        private val mc = Minecraft.getMinecraft()
        val logger: Logger = LogManager.getLogger("MapProcessor")
    }
}
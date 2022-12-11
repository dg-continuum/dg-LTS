package kr.syeyoung.dungeonsguide.dungeon

import kr.syeyoung.dungeonsguide.chat.ChatTransmitter
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonSpecificDataProvider
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonSpecificDataProviderRegistry.getDoorFinder
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.RoomProcessor
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.bossfight.BossfightProcessor
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus
import kr.syeyoung.dungeonsguide.utils.VectorUtils
import net.minecraft.util.BlockPos
import org.joml.Vector2i
import org.joml.Vector3i
import java.awt.Rectangle
import java.util.*

class DungeonContext {
    @JvmField
    val batSpawnedLocations: MutableMap<Int, Vector3i> = HashMap()
    @JvmField
    val killedBats: MutableList<Int> = ArrayList()
    @JvmField
    val expositions: MutableSet<Vector3i> = HashSet()
    @JvmField
    val mapProcessor: MapProcessor = MapProcessor(this)
    @JvmField
    val roomMapper: MutableMap<Vector2i?, DungeonRoom> = HashMap()
    @JvmField
    val dungeonRoomList: MutableList<DungeonRoom> = ArrayList()
    @JvmField
    val globalRoomProcessors: MutableList<RoomProcessor> = ArrayList()
    @JvmField
    val deaths: MutableMap<String, Int> = HashMap()
    @JvmField
    val milestoneReached: MutableList<Array<String>> = ArrayList()
    @JvmField
    val players: MutableSet<String> = HashSet()
    @JvmField
    var percentage = 0
    @JvmField
    var started: Long = -1
    @JvmField
    var dungeonMin: BlockPos? = null
    var bossRoomEnterSeconds: Long = -1
    var init: Long
    var bossroomSpawnPos: BlockPos? = null
    var isHasTrapRoom = false
    var isGotMimic = false
    var latestSecretCnt = 0
    var latestTotalSecret = 0
    var latestCrypts = 0
    @JvmField
    var maxSpeed = 600
    @JvmField
    var secretPercentage = 1.0
    @JvmField
    var bossfightProcessor: BossfightProcessor? = null
    var isEnded = false
    var isDefeated = false
    val dataProvider: DungeonSpecificDataProvider? = getDoorFinder(SkyblockStatus.dungeonNameStrriped)

    init {
        if (dataProvider != null) {
            isHasTrapRoom = dataProvider.hasTrapRoom(SkyblockStatus.dungeonNameStrriped)
            secretPercentage = dataProvider.secretPercentage(SkyblockStatus.dungeonNameStrriped)
            maxSpeed = dataProvider.speedSecond(SkyblockStatus.dungeonNameStrriped)
        } else {
            ChatTransmitter.addToQueue(ChatTransmitter.PREFIX + "Failed To create Dungeon Data provider (new dungeon?), report this in discord")
        }
        init = System.currentTimeMillis()
    }

    val currentRoom: DungeonRoom?
        get() = roomMapper[mapProcessor.worldPointToRoomPoint(VectorUtils.getPlayerVector3i())]
    val currentRoomProcessor: RoomProcessor?
        get() {
            val dungeonRoomOpt =
                Optional.ofNullable(DungeonFacade.context).map { obj: DungeonContext -> obj.mapProcessor }
                    .map { a: MapProcessor -> a.worldPointToRoomPoint(VectorUtils.getPlayerVector3i()) }
                    .map { a: Vector2i? -> DungeonFacade.context!!.roomMapper[a] }
            val dungeonRoom = dungeonRoomOpt.orElse(null) ?: return null
            return dungeonRoom.roomProcessor
        }

    companion object {
        val roomBoundary = Rectangle(-10, -10, 138, 138)
        @JvmStatic
        val timeElapsed: Long
            get() {
                val ctx = DungeonFacade.context
                return if (ctx == null) {
                    -1
                } else {
                    System.currentTimeMillis() - ctx.started
                }
            }
    }
}
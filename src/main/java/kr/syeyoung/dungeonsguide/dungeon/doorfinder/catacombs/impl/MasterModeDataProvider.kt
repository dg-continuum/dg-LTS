package kr.syeyoung.dungeonsguide.dungeon.doorfinder.catacombs.impl

import kr.syeyoung.dungeonsguide.chat.ChatTransmitter
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.catacombs.CatacombsDataProvider
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.BossfightProcessor
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.bossfight.BossfightProcessorLivid
import net.minecraft.util.ChatComponentText
import net.minecraft.world.World

class MasterModeDataProvider : CatacombsDataProvider() {
    override fun createBossfightProcessor(w: World, dungeonName: String): BossfightProcessor? {
        val floor = dungeonName.substring(14).trim { it <= ' ' }
        ChatTransmitter.sendDebugChat(ChatComponentText("Floor: Master mode $floor Building bossfight processor"))
        return if (floor == "M5") {
            BossfightProcessorLivid(true)
        } else null
    }

    override fun hasTrapRoom(dungeonName: String): Boolean {
        return when (val floor = dungeonName.substring(14).trim { it <= ' ' }) {
            "M3", "M4", "M5", "M6" -> true
            else -> floor == "M7"
        }
    }

    override fun secretPercentage(dungeonName: String): Double {
        return 1.0
    }

    override fun speedSecond(dungeonName: String): Int {
        return 480
    }
}
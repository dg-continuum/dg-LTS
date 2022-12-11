package kr.syeyoung.dungeonsguide.dungeon.doorfinder.catacombs.impl

import kr.syeyoung.dungeonsguide.chat.ChatTransmitter
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.catacombs.CatacombsDataProvider
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.bossfight.*
import net.minecraft.util.ChatComponentText
import net.minecraft.world.World

class NormalModeDataProvider : CatacombsDataProvider() {
    override fun createBossfightProcessor(w: World, dungeonName: String): BossfightProcessor? {
        val floor = dungeonName.substring(14).trim { it <= ' ' }
        ChatTransmitter.sendDebugChat(ChatComponentText("Floor: $floor Building bossfight processor"))
        return when (floor) {
            "F1" -> BossfightProcessorBonzo()
            "F2" -> BossfightProcessorScarf()
            "F3" -> BossfightProcessorProf()
            "F4" -> BossfightProcessorThorn()
            "F5" -> BossfightProcessorLivid(false)
            "F6" -> BossfightProcessorSadan()
            "F7" -> BossfightProcessorNecron()
            else -> null
        }
    }

    override fun hasTrapRoom(dungeonName: String): Boolean {
        return when (dungeonName.substring(14).trim { it <= ' ' }) {
            "F3", "F4", "F5", "F6" -> true
            else -> dungeonName.substring(14).trim { it <= ' ' } == "F7"
        }
    }

    override fun secretPercentage(dungeonName: String): Double {
        return when (dungeonName.substring(14).trim { it <= ' ' }) {
            "F1", "E" -> 0.3
            "F2" -> 0.4
            "F3" -> 0.5
            "F4" -> 0.6
            "F5" -> 0.7
            "F6" -> 0.85
            else -> 1.0
        }
    }

    override fun speedSecond(dungeonName: String): Int {
        return when (dungeonName.substring(14).trim { it <= ' ' }) {
            "F5", "F7" -> 720
            else -> 600
        }
    }
}
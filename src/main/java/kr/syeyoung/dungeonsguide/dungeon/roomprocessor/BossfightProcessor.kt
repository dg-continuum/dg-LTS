package kr.syeyoung.dungeonsguide.dungeon.roomprocessor

import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.bossfight.HealthData

interface BossfightProcessor : RoomProcessor {
    val phases: List<String?>?
    val currentPhase: String?
    val nextPhases: List<String?>?
    val healths: List<HealthData?>?
    val bossName: String?
}
package kr.syeyoung.dungeonsguide.dungeon.doorfinder

enum class EDungeonDoorType(
    val isExist: Boolean,
    val isKeyRequired: Boolean,
    val isHeadToBlood: Boolean,
    var Name: String,
) {
    NONE(false, false, false, "?"),
    ENTRANCE(true, false, false, "entrance"),
    WITHER(true, true, true, "withergate"),
    WITHER_FAIRY(true, false, true, "wither-fairy-gate"),
    BLOOD(true, true, true, "bloodgate"),
    UNOPEN(true, false, false, "gate");
}
package kr.syeyoung.dungeonsguide.dungeon.actions

enum class ActionState(private val state: String) {
    navigate("navigate"), found("found");
}
package kr.syeyoung.dungeonsguide.dungeon.actions

enum class ActionState(val state: String) {
    navigate("navigate"), openn("open"), found("found"), click("click");

    companion object {

        @JvmStatic
        fun turnIntoForm(yas: String): ActionState{
            return when (yas){
                "open" -> openn
                else -> valueOf(yas)
            }
        }

    }

}
package kr.syeyoung.dungeonsguide.dungeon.actions;

public enum ActionState {
    navigate("navigate"),
    found("found");

    private final String state;

    ActionState(String state) {
        this.state = state;
    }
}

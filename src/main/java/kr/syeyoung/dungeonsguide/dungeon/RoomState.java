package kr.syeyoung.dungeonsguide.dungeon;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RoomState {
    DISCOVERED(0), COMPLETE_WITHOUT_SECRETS(0), FINISHED(0), FAILED(-14);
    private final int scoreModifier;
}

package kr.syeyoung.dungeonsguide.whosonline.api.messages.server;

import kr.syeyoung.dungeonsguide.whosonline.api.messages.AbstractMessage;
import lombok.Data;

@Data
public class S04Pong implements AbstractMessage {
    public final long c;
}

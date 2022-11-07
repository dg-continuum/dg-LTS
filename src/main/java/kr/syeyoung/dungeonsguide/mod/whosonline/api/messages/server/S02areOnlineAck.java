package kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.server;

import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.AbstractMessage;
import lombok.Data;

import java.util.Map;

@Data
public class S02areOnlineAck implements AbstractMessage {
    public final Map<String, Boolean> users;
    public final String nonce;
}

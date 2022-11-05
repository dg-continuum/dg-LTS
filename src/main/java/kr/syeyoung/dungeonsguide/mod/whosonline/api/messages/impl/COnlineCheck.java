package kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.impl;

import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.AbstractMessage;
import lombok.Data;

@Data
public class COnlineCheck extends AbstractMessage {
    public static final String t = "/is_online";

    /**
     * The uuid to check
     */
    public final String c;
}

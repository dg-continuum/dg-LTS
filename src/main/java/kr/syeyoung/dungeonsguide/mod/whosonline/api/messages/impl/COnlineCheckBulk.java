package kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.impl;

import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.AbstractMessage;
import lombok.Data;

@Data
public class COnlineCheckBulk extends AbstractMessage {
    public static final String t = "/is_online/bulk";

    /**
     * array of uuids to check if they are online
     */
    public final String[] c;
}

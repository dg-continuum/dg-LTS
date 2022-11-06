package kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.impl;

import lombok.Data;

@Data
public class COnlineCheck {
    public final String t = "/is_online";

    /**
     * The uuid to check
     */
    public final OBJ c;

    @Data
    public static class OBJ {
        public final String uuid;
        public final String nouce;
    }

}

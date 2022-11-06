package kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.impl;

import lombok.Data;

@Data
public class COnlineCheckBulk {
    public final String t = "/is_online/bulk";

    /**
     * array of uuids to check if they are online
     */
    public final OBJ c;

    @Data
    public static class OBJ {
        public final String[] uuids;
        public final String nonce;
    }

}

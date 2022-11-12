package kr.syeyoung.dungeonsguide.whosonline.api.messages.client;

import lombok.Data;

@Data
public class C02OnlineCheckBulk {
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

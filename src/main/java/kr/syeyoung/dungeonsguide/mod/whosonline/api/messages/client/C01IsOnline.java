package kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.client;

import lombok.Data;

@Data
public class C01IsOnline {
    public final String t = "/is_online";

    /**
     * The uuid to check
     */
    public final OBJ c;

    @Data
    public static class OBJ {
        public final String uuid;
        public final String nonce;
    }

}

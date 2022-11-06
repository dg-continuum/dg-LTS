package kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.client;

import lombok.Data;

@Data
public class C00Connect {
    public final String t = "/connect";

    /**
     * Clients player uuid
     */
    public final String c;
}
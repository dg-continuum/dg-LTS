package kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.impl;

import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.AbstractMessage;
import lombok.Data;

@Data
public class CConnect extends AbstractMessage {
    public static final String t = "/connect";

    /**
     * Clients player uuid
     */
    public final String c;
}
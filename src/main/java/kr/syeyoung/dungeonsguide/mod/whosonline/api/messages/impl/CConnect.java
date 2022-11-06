package kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.impl;

import lombok.Data;

@Data
public class CConnect  {
    public final String t = "/connect";

    /**
     * Clients player uuid
     */
    public final String c;
}
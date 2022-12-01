package kr.syeyoung.dungeonsguide.features;

import lombok.Getter;

public class SimpleFeatureV2 {
    @Getter
    private final String id;

    public SimpleFeatureV2(String id) {
        this.id = id;
    }
}

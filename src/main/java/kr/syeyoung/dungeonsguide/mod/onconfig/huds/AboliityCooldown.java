package kr.syeyoung.dungeonsguide.mod.onconfig.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.ability.FeatureAbilityCooldown;

public class AboliityCooldown {
    @HUD(
            name = "View Ability Cooldowns"
    )
    public static FeatureAbilityCooldown facd = new FeatureAbilityCooldown();
}

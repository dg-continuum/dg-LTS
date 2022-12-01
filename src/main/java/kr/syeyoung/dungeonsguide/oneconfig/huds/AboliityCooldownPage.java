package kr.syeyoung.dungeonsguide.oneconfig.huds;

import cc.polyfrost.oneconfig.config.annotations.HUD;
import kr.syeyoung.dungeonsguide.features.impl.misc.FeatureAbilityCooldown;

public class AboliityCooldownPage {
    @HUD(
            name = "View Ability Cooldowns"
    )
    public static FeatureAbilityCooldown facd = new FeatureAbilityCooldown();
}

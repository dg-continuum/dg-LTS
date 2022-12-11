package kr.syeyoung.dungeonsguide.dungeon.mechanics.predicates

import com.google.common.base.Predicate
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand

class PredicateArmorStand : Predicate<Entity?> {
    override fun apply(input: Entity?): Boolean {
        return input is EntityArmorStand
    }

    override fun hashCode(): Int {
        return 0
    }

    override fun equals(o: Any?): Boolean {
        return o === this || o != null && o.javaClass == this.javaClass
    }

    companion object {
        @JvmStatic
        val INSTANCE = PredicateArmorStand()
    }
}
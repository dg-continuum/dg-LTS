package kr.syeyoung.dungeonsguide.dungeon.doorfinder.catacombs

import com.google.common.collect.Sets
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonSpecificDataProvider
import kr.syeyoung.dungeonsguide.utils.BlockCache
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.world.World
import javax.vecmath.Vector2d

abstract class CatacombsDataProvider : DungeonSpecificDataProvider {
    /**
     * This gets all morts checks for iron bars near him
     * and based on iron bars determine the door location
     *
     * @param w           World that we are going to look for the door in
     * world is explicitly specified instead of mc.theWorld bc we can use cached worlds
     * @param dungeonName dungeon type eg master mode, currently unused
     * @return Block pos of the dungeon entrance
     */
    override fun findDoor(w: World, dungeonName: String): BlockPos? {
        val armorStand = getMorts(w)
        if (!armorStand.isEmpty()) {
            val mort = armorStand.iterator().next()
            var pos = mort.position
            pos = pos.add(0, 3, 0)
            for (i in 0..4) {
                for (vector2d in directions) {
                    val test = pos.add(vector2d.x * i, 0.0, vector2d.y * i)
                    if (BlockCache.getBlock(test) === Blocks.iron_bars) {
                        return pos.add(vector2d.x * (i + 2), -2.0, vector2d.y * (i + 2))
                    }
                }
            }
        }
        return null
    }

    override fun findDoorOffset(w: World, dungeonName: String): Vector2d? {
        val armorStand = getMorts(w)
        return if (!armorStand.isEmpty()) {
            getVector2d(w, armorStand)!!
        } else null
    }

    companion object {
        private val directions: Set<Vector2d> =
            Sets.newHashSet(Vector2d(0.0, 1.0), Vector2d(0.0, -1.0), Vector2d(1.0, 0.0), Vector2d(-1.0, 0.0))

        fun getVector2d(w: World, armorStand: Collection<EntityArmorStand>): Vector2d? {
            val mort = armorStand.iterator().next()
            var pos = mort.position
            pos = pos.add(0, 3, 0)
            for (i in 0..4) {
                for (vector2d in directions) {
                    val test = pos.add(vector2d.x * i, 0.0, vector2d.y * i)
                    if (w.getChunkFromBlockCoords(test).getBlock(test) === Blocks.iron_bars) {
                        return vector2d
                    }
                }
            }
            return null
        }

        fun getMorts(w: World): Collection<EntityArmorStand> {
            return w.getEntities(EntityArmorStand::class.java) { input: EntityArmorStand? -> input!!.name == "§bMort" }
        }
    }
}
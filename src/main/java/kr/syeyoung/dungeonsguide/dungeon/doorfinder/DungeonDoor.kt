package kr.syeyoung.dungeonsguide.dungeon.doorfinder

import com.google.common.collect.Sets
import kr.syeyoung.dungeonsguide.utils.BlockCache
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.world.World
import org.joml.Vector3i

class DungeonDoor(world: World, pos: Vector3i, type: EDungeonDoorType) {
    private val w: World
    val position: Vector3i
    val type: EDungeonDoorType
    var isZDir = false

    init {
        var type = type
        w = world
        position = pos
        if (type == EDungeonDoorType.WITHER && BlockCache.getBlockState(pos).block === Blocks.air) type =
            EDungeonDoorType.WITHER_FAIRY
        this.type = type
        var exist = type.isExist
        for (x in -1..1) {
            for (y in -1..1) {
                for (z in -1..1) {
                    val pos2 = pos.add(x, y, z)
                    val block = BlockCache.getBlockState(pos2).block
                    if (BlockCache.getBlockState(pos).block !== block) exist = false
                }
            }
        }
        if (exist) {
            val ZCheck = pos.add(0, 0, 2)
            isZDir = BlockCache.getBlockState(ZCheck).block === Blocks.air
            if (isZDir) {
                for (x in -1..1) {
                    for (y in -1..1) {
                        var z = -2
                        while (z <= 2) {
                            val pos2 = pos.add(x, y, z)
                            if (BlockCache.getBlockState(pos2).block !== Blocks.air) exist = false
                            z += 4
                        }
                    }
                }
            } else {
                var x = -2
                while (x <= 2) {
                    for (y in -1..1) {
                        for (z in -1..1) {
                            val pos2 = pos.add(x, y, z)
                            if (BlockCache.getBlockState(pos2).block !== Blocks.air) exist = false
                        }
                    }
                    x += 4
                }
            }
        }
        if (!exist) {
            isZDir = false
        }
    }

    companion object {
        private val legalBlocks: Set<Block> = Sets.newHashSet(
            Blocks.coal_block,
            Blocks.barrier,
            Blocks.monster_egg,
            Blocks.air,
            Blocks.stained_hardened_clay
        )
    }
}
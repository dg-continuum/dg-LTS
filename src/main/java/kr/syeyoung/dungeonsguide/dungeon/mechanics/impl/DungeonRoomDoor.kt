package kr.syeyoung.dungeonsguide.dungeon.mechanics.impl

import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionMove
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonDoor
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic
import kr.syeyoung.dungeonsguide.dungeon.mechanics.MechanicType
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import org.joml.Vector3i
import java.awt.Color

class DungeonRoomDoor(val dungeonRoom: DungeonRoom, val doorfinder: DungeonDoor) : DungeonMechanic(), Cloneable {


    public override fun clone(): DungeonRoomDoor {
        return DungeonRoomDoor(dungeonRoom, doorfinder)
    }

    lateinit var offsetPoint: OffsetPoint


    override val mechType: MechanicType = MechanicType.RoomDoor

    init {
        if (doorfinder.isZDir) {
            if (dungeonRoom.canAccessAbsolute(doorfinder.position.add(0, 0, 2, Vector3i()))) {
                offsetPoint = OffsetPoint(dungeonRoom, doorfinder.position.add(0, 0, 2, Vector3i()))
            } else if (dungeonRoom.canAccessAbsolute(
                    doorfinder.position.add(0, 0, -2, Vector3i())
                )
            ) {
                offsetPoint = OffsetPoint(dungeonRoom, doorfinder.position.add(0, 0, -2, Vector3i()))
            }
        } else {
            offsetPoint = if (dungeonRoom.canAccessAbsolute(doorfinder.position.add(2, 0, 0, Vector3i()))) {
                OffsetPoint(dungeonRoom, doorfinder.position.add(2, 0, 0, Vector3i()))
            } else if (dungeonRoom.canAccessAbsolute(
                    doorfinder.position.add(-2, 0, 0, Vector3i())
                )
            ) {
                OffsetPoint(dungeonRoom, doorfinder.position.add(-2, 0, 0, Vector3i()))
            } else {
                OffsetPoint(dungeonRoom, doorfinder.position)
            }
        }

    }

    override fun getAction(state: String, dungeonRoom: DungeonRoom): Set<AbstractAction> {
        require("navigate".equals(state, ignoreCase = true)) { "$state is not valid state for secret" }
        return setOf(ActionMove(offsetPoint))
    }

    override fun highlight(color: Color, name: String, dungeonRoom: DungeonRoom, partialTicks: Float) {
        val pos = offsetPoint.getVector3i(dungeonRoom)
        RenderUtils.highlightBlock(
            pos, color, partialTicks
        )
        RenderUtils.drawTextAtWorld(
            name, pos.x + 0.5f, pos.y + 0.75f, pos.z + 0.5f, -0x1, 0.03f, false, true, partialTicks
        )
        RenderUtils.drawTextAtWorld(
            getCurrentState(dungeonRoom),
            pos.x + 0.5f,
            pos.y + 0.25f,
            pos.z + 0.5f,
            -0x1,
            0.03f,
            false,
            true,
            partialTicks
        )
    }

    override fun getCurrentState(dungeonRoom: DungeonRoom): String {
        return if (doorfinder.type.isKeyRequired) "key" else "normal"
    }

    override fun getPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        return setOf("navigate")
    }

    override fun getTotalPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        return setOf("key-open", "key-closed", "normal")
    }

    override fun getRepresentingPoint(dungeonRoom: DungeonRoom): OffsetPoint {
        return offsetPoint
    }
}
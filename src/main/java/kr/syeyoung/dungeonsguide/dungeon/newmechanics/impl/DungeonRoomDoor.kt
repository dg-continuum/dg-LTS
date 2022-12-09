package kr.syeyoung.dungeonsguide.dungeon.newmechanics.impl

import com.google.common.collect.Sets
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.impl.ActionMove
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonDoor
import kr.syeyoung.dungeonsguide.dungeon.newmechanics.MechanicType
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import java.awt.Color

class DungeonRoomDoor() : kr.syeyoung.dungeonsguide.dungeon.newmechanics.DungeonMechanic() {

    constructor(dungeonRoom: DungeonRoom, doorfinder: DungeonDoor) : this() {
        this.dungeonRoom = dungeonRoom
        this.doorfinder = doorfinder
    }


    lateinit var doorfinder: DungeonDoor
    lateinit var dungeonRoom: DungeonRoom
    var offsetPoint: OffsetPoint? = null


    override val mechType: MechanicType =
        MechanicType.RoomDoor

    init {
        if (doorfinder.isZDir) {
            if (dungeonRoom.canAccessAbsolute(doorfinder.position.add(0, 0, 2))) offsetPoint =
                OffsetPoint(dungeonRoom, doorfinder.position.add(0, 0, 2)) else if (dungeonRoom.canAccessAbsolute(
                    doorfinder.position.add(0, 0, -2)
                )
            ) offsetPoint = OffsetPoint(dungeonRoom, doorfinder.position.add(0, 0, -2))
        } else {
            if (dungeonRoom.canAccessAbsolute(doorfinder.position.add(2, 0, 0))) offsetPoint =
                OffsetPoint(dungeonRoom, doorfinder.position.add(2, 0, 0)) else if (dungeonRoom.canAccessAbsolute(
                    doorfinder.position.add(-2, 0, 0)
                )
            ) offsetPoint = OffsetPoint(dungeonRoom, doorfinder.position.add(-2, 0, 0))
        }
        if (offsetPoint == null) {
            offsetPoint = OffsetPoint(dungeonRoom, doorfinder.position)
        }
    }

    override fun getAction(state: String, dungeonRoom: DungeonRoom): Set<AbstractAction> {
        require("navigate".equals(state, ignoreCase = true)) { "$state is not valid state for secret" }
        val preRequisites: MutableSet<AbstractAction> = HashSet()
        preRequisites.add(ActionMove(offsetPoint!!))
        return preRequisites
    }

    override fun highlight(color: Color, name: String, dungeonRoom: DungeonRoom, partialTicks: Float) {
        val pos = offsetPoint!!.getVector3i(dungeonRoom)
        RenderUtils.highlightBlock(pos, color, partialTicks)
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
        return Sets.newHashSet("navigate")
    }

    override fun getTotalPossibleStates(dungeonRoom: DungeonRoom): Set<String> {
        return Sets.newHashSet("key-open", "key-closed", "normal")
    }

    override fun getRepresentingPoint(dungeonRoom: DungeonRoom): OffsetPoint {
        return offsetPoint!!
    }
}
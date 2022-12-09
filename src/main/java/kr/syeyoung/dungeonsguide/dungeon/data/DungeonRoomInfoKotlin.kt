package kr.syeyoung.dungeonsguide.dungeon.data

import java.io.Serializable
import java.util.*


class DungeonRoomInfoKotlin() : Serializable {

    constructor(shape: Short, color: Byte) : this() {
        this.shape = shape
        this.color = color
    }

    @Transient
    var registered = false

    var isUserMade = false
    var shape: Short? = null
    var color: Byte? = null
    var blocks: Array<IntArray> = emptyArray()
    var uuid: UUID = UUID.randomUUID()
    var name: String = uuid.toString()
    var processorId = "default"
    var properties: Map<String, Any> = HashMap()
    var mechanics: Map<String, kr.syeyoung.dungeonsguide.dungeon.newmechanics.DungeonMechanic> = HashMap()
    var totalSecrets = -1


    companion object {
        private const val serialVersionUID = -8291811286448196640L
    }
}
package kr.syeyoung.dungeonsguide.dungeon.data

import java.io.Serializable

class OffsetPointSet : Cloneable, Serializable {

    var offsetPointList: MutableList<OffsetPoint> = ArrayList()
    public override fun clone(): Any {
        val ops = OffsetPointSet()
        for (offsetPoint in offsetPointList) {
            ops.offsetPointList.add(offsetPoint.clone() as OffsetPoint)
        }
        return ops
    }

    companion object {
        private const val serialVersionUID = -5349635873127088737
    }

}
package kr.syeyoung.dungeonsguide.dungeon.actions.impl

import kr.syeyoung.dungeonsguide.dungeon.DungeonFacade
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRouteProperties
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig
import kr.syeyoung.dungeonsguide.utils.RenderUtils
import kr.syeyoung.dungeonsguide.utils.VectorUtils
import net.minecraft.client.Minecraft
import net.minecraft.util.MathHelper
import org.joml.Vector3d
import java.util.concurrent.Future

open class ActionMove(val target: OffsetPoint) : AbstractAction() {
    private var tick = -1
    private var poses: List<Vector3d?>? = null
    private var latestFuture: Future<List<Vector3d>>? = null

    override fun isComplete(dungeonRoom: DungeonRoom?): Boolean {
        return target.getVector3i(dungeonRoom).distanceSquared(VectorUtils.getPlayerVector3i()) < 2
    }

    override fun onRenderWorld(
        dungeonRoom: DungeonRoom?,
        partialTicks: Float,
        actionRouteProperties: ActionRouteProperties?,
        flag: Boolean
    ) {
        val pos = target.getVector3i(dungeonRoom)
        val distance = pos.distance(VectorUtils.getPlayerVector3i()).toFloat()
        var multiplier = distance / 120f //mobs only render ~120 blocks away
        if (flag) multiplier *= 2.0f
        var scale = 0.45f * multiplier
        scale *= (25.0 / 6.0).toFloat()
        if (actionRouteProperties!!.beacon) {
            if (DgOneCongifConfig.renderSecretBeacons) {
                RenderUtils.renderBeaconBeam(
                    pos.x.toDouble(),
                    pos.y.toDouble(),
                    pos.z.toDouble(),
                    actionRouteProperties.beaconBeamColor,
                    partialTicks
                )
            }
            RenderUtils.highlightBlock(pos, actionRouteProperties.beaconColor, partialTicks)
        }
        if (DgOneCongifConfig.renderSecretDestText) {
            RenderUtils.drawTextAtWorld(
                "Destination",
                pos.x + 0.5f,
                pos.y + 0.5f + scale,
                pos.z + 0.5f,
                -0xff0100,
                if (flag) 2f else 1f,
                true,
                false,
                partialTicks
            )
        }
        RenderUtils.drawTextAtWorld(
            String.format(
                "%.2f",
                MathHelper.sqrt_double(pos.distance(VectorUtils.getPlayerVector3i()))
            ) + "m",
            pos.x + 0.5f,
            pos.y + 0.5f - scale,
            pos.z + 0.5f,
            -0x100,
            if (flag) 2f else 1f,
            true,
            false,
            partialTicks
        )
        if (!DgOneCongifConfig.togglePathfindStatus) {
            RenderUtils.drawLinesVec3(
                poses,
                actionRouteProperties.lineColor,
                actionRouteProperties.lineWidth,
                partialTicks,
                true
            )
        }
    }

    override fun onTick(dungeonRoom: DungeonRoom?, actionRouteProperties: ActionRouteProperties?) {
        tick = (tick + 1) % 1.coerceAtLeast(actionRouteProperties!!.lineRefreshRate)

        if (latestFuture == null){
            if (tick == 0 && actionRouteProperties.isPathfind) {
                if (!DgOneCongifConfig.freezePathfindingStatus || poses == null) {

                    latestFuture = DungeonFacade.INSTANCE.cachedPathFinder.CreatePath(Minecraft.getMinecraft().thePlayer,
                        target.getVector3i(dungeonRoom), dungeonRoom!!)
                }
            }
        } else {
            if(latestFuture!!.isDone){
                poses = latestFuture!!.get()
                latestFuture = null
            }
        }

    }

    fun forceRefresh(dungeonRoom: DungeonRoom) {
        latestFuture = DungeonFacade.INSTANCE.cachedPathFinder.CreatePath(Minecraft.getMinecraft().thePlayer,
            target.getVector3i(dungeonRoom), dungeonRoom)
    }

    override fun toString(): String {
        return "Move\n- target: $target"
    }

}
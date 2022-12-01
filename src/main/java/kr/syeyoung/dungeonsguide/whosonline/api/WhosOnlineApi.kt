package kr.syeyoung.dungeonsguide.whosonline.api

import kr.syeyoung.dungeonsguide.whosonline.WebSocketClientStatus
import kr.syeyoung.dungeonsguide.whosonline.WhosOnlineManager
import kr.syeyoung.dungeonsguide.whosonline.api.messages.client.C01IsOnline
import kr.syeyoung.dungeonsguide.whosonline.api.messages.client.C02OnlineCheckBulk
import org.apache.logging.log4j.LogManager
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

/**
 * Api wrapper for whos online websocket server
 * @author Eryk Ruta
 */
class WhosOnlineApi(
    private val client: WhosOnlineWebSocket,
    private val cache: WhosOnlineCache,
    private val executor: ExecutorService
) {
    var logger = LogManager.getLogger("WhosOnlineApi")

    /**
     * @return -s if websocket is ok
     */
    fun stateCheck(): Boolean {
        return client.status != WebSocketClientStatus.CONNECTED
    }

    /**
     * @param uuid UUid of player to check
     * @return true if player is online with dg
     */
    fun isOnline(uuid: String): Future<Boolean> {
        if (cache.isCached(uuid)) {
            return CompletableFuture.completedFuture(cache.isOnline(uuid))
        }
        return if (stateCheck()) {
            CompletableFuture.completedFuture(false)
        } else executor.submit<Boolean> {
            val messageId = UUID.randomUUID().toString()
            val message = WhosOnlineManager.gson.toJson(C01IsOnline(C01IsOnline.OBJ(uuid, messageId)))
            client.sendAndBlock(message, messageId, TIMEOUT_VALUE)
            logger.info("Is online: {} uuid: {} ", uuid, cache.isOnline(uuid))
            cache.isOnline(uuid)
        }
    }

    /**
     * @param uuids uuids of player to check
     * @return array of their statuses
     */
    fun areOnline(uuids: Array<String>): Future<Array<Boolean?>> {
        if (stateCheck()) {
            val nulls = arrayOfNulls<Boolean>(uuids.size)
            Arrays.fill(nulls, false)
            return CompletableFuture.completedFuture(nulls)
        }
        val notCached: MutableSet<String> = HashSet()
        val cached = HashMap<String, Boolean>()
        for (uuid in uuids) {
            val iscached = cache.isCached(uuid)
            if (iscached) {
                notCached.add(uuid)
            } else {
                cached[uuid] = cache.isOnline(uuid)
            }
        }

        // in case that all the nicks are not cached
        if (notCached.isEmpty()) {
            val res = arrayOfNulls<Boolean>(uuids.size)
            // we do this to preserve the order which they were added in
            for (i in uuids.indices) {
                val uuid = uuids[i]
                res[i] = cached[uuid]
            }
            return CompletableFuture.completedFuture(res)
        }
        return executor.submit<Array<Boolean?>> {
            val messageId = UUID.randomUUID().toString()
            val message = WhosOnlineManager.gson.toJson(
                C02OnlineCheckBulk(
                    C02OnlineCheckBulk.OBJ(
                        messageId,
                        notCached.toTypedArray(),
                    )
                )
            )
            client.sendAndBlock(message, messageId, TIMEOUT_VALUE)
            val returnVals = arrayOfNulls<Boolean>(uuids.size)
            for (i in uuids.indices) {
                val uuid = uuids[i]
                returnVals[i] = cache.isOnline(uuid)
            }
            returnVals
        }
    }

    companion object {
        private const val TIMEOUT_VALUE: Long = 500
    }
}
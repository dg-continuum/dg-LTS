package kr.syeyoung.dungeonsguide.whosonline

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.Gson
import kr.syeyoung.dungeonsguide.whosonline.api.WhosOnlineApi
import kr.syeyoung.dungeonsguide.whosonline.api.WhosOnlineCache
import kr.syeyoung.dungeonsguide.whosonline.api.WhosOnlineRest
import kr.syeyoung.dungeonsguide.whosonline.api.WhosOnlineWebSocket
import lombok.Getter
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.LogManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class WhosOnlineManager(val remoteHost: String) {
    private val ex: ExecutorService
    private val se: ScheduledExecutorService

    fun getWebsocketClient(): WhosOnlineApi? {
        return websocketClient
    }

    private var webSocket: WhosOnlineWebSocket? = null

    @Getter
    private var websocketClient: WhosOnlineApi? = null

    @Getter
    private var cache: WhosOnlineCache? = null
    private var restClient: WhosOnlineRest? = null
    var useDebugServers = false
    fun init() {
        cache = WhosOnlineCache()
        val websocketUri = if (useDebugServers) "ws://$remoteHost/ws" else "wss://$remoteHost/ws"
        val restUri = if (useDebugServers) "http://$remoteHost" else "https://$remoteHost"
        webSocket = WhosOnlineWebSocket(websocketUri, se, cache, Minecraft.getMinecraft().session.playerID)
        restClient = WhosOnlineRest(cache, ex, restUri)
        webSocket!!.connect()
        websocketClient = WhosOnlineApi(webSocket!!, cache!!, ex)

    }

    var closed = false

    init {
        if (remoteHost.startsWith("localhost")) {
            useDebugServers = true
        }
        MinecraftForge.EVENT_BUS.register(this)
        val namedThreadFactory = ThreadFactoryBuilder().setNameFormat("Dg WhosOnline pool").build()
        ex = Executors.newCachedThreadPool(namedThreadFactory)
        se = Executors.newScheduledThreadPool(2, namedThreadFactory)
    }

    fun close() {
        closed = true
        try {
            webSocket?.close()
            webSocket = null
            websocketClient = null
            restClient = null
            ex.awaitTermination(1, TimeUnit.SECONDS)
            se.awaitTermination(1, TimeUnit.SECONDS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SubscribeEvent
    fun websocketdied(e: WhosOnlineDied?) {
        if (closed) return
        logger.info("Who'sOnline websocket died, trying again in 4 seconds")
        se.schedule({ init() }, 4, TimeUnit.SECONDS)
    }

    class WhosOnlineDied : Event()
    companion object {
        @JvmField
        val gson = Gson()
        val logger = LogManager.getLogger("WhosOnlineManager")
    }
}
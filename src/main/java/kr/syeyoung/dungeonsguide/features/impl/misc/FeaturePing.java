package kr.syeyoung.dungeonsguide.features.impl.misc;

import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.hud.SingleTextHud;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import kr.syeyoung.dungeonsguide.events.PacketListener;
import kr.syeyoung.dungeonsguide.oneconfig.DgOneCongifConfig;
import kr.syeyoung.dungeonsguide.utils.SimpleLock;
import kr.syeyoung.dungeonsguide.utils.SkyblockStatus;
import kr.syeyoung.dungeonsguide.utils.TitleRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.status.INetHandlerStatusClient;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.network.status.server.S00PacketServerInfo;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class FeaturePing extends SingleTextHud {

    public FeaturePing() {
        super("Ping: ", true);
        setup();
        MinecraftForge.EVENT_BUS.register(this);
    }


    transient boolean shouldShowAverage = false;
    transient ArrayList<Long> averagePingStore = new ArrayList<>();
    transient Long averagePing = 0L;
    transient private long whenWasPingLocked;

    @Slider(
            name = "Delay bettwen Pings",
            min = 2,
            max = 20
    )
    public static long pingtimeout = 4;

    transient static final int TIMEOUT_THRESHOLD = 350;

    transient boolean lastTimeOutStatus;

    @Override
    public boolean isEnabled() {
        return SkyblockStatus.isOnSkyblock();
    }

    @Override
    public String getText(boolean example) {
        return String.valueOf(shouldShowAverage ? averagePing : ping);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e){
        if(e.phase != TickEvent.Phase.START || e.side != Side.CLIENT) return;
        if(System.currentTimeMillis() - PacketListener.lastPacketReceived > TIMEOUT_THRESHOLD){
            if(!lastTimeOutStatus && !Minecraft.getMinecraft().isSingleplayer()){
                TitleRender.displayTitle("", "§cServer stopped responding", 0,50000, 8);
            }
            lastTimeOutStatus = true;
        } else {
            if(lastTimeOutStatus){
                TitleRender.clearTitle();
                lastTimeOutStatus = false;
            }
        }
    }

    transient long ping = 0;

    void setPing(long ping){
        this.ping = ping;
        if (averagePingStore.size() > 5)
            averagePingStore.remove(averagePingStore.size() - 1);

        averagePingStore.add(0, ping);

        long temp = 0;
        for (long i : averagePingStore) {
            temp += i;
        }
        averagePing = temp / averagePingStore.size();
        if(DgOneCongifConfig.DEBUG_MODE) logger.info("Updating ping: {}", ping);
    }

    @Exclude
    transient static Logger logger = LogManager.getLogger("FeaturePing");
    transient final SimpleLock pingLock = new SimpleLock();

    public void setup(){
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Dg Ping pool").build();
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, namedThreadFactory);

        scheduler.scheduleAtFixedRate(() -> {
            if(!isEnabled()) return;
            if(!SkyblockStatus.isOnSkyblock()) return;
            if(pingLock.isLocked()) {
                if(whenWasPingLocked + 400 < Minecraft.getSystemTime()){
                    pingLock.unLock();
                } else {
                    return;
                }
            }

            try {
                whenWasPingLocked = Minecraft.getSystemTime();
                pingLock.lock();
                ping("mc.hypixel.net:25565");

            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }, 10,4 * 1000, TimeUnit.MILLISECONDS);
    }



    /**
     * STOLEN FROM VANILLA BTW
     */
    public void ping(@NotNull String serverAddress) throws UnknownHostException {
        ServerAddress serveraddress = ServerAddress.fromString(serverAddress);
        NetworkManager networkmanager = NetworkManager.createNetworkManagerAndConnect(InetAddress.getByName(serveraddress.getIP()), serveraddress.getPort(), false);
        networkmanager.setNetHandler(new INetHandlerStatusClient(){
            long pingStart = 0L;
            private boolean hasSentPing = false;
            private boolean sendFuse = false;
            @Override
            public void handleServerInfo(S00PacketServerInfo packetIn) {
                if (!this.sendFuse) {
                    this.sendFuse = true;

                    pingStart = Minecraft.getSystemTime();
                    networkmanager.sendPacket(new C01PacketPing(pingStart));

                    this.hasSentPing = true;
                } else {
                    networkmanager.closeChannel(new ChatComponentText("Received unrequested status"));
                }
            }

            @Override
            public void handlePong(S01PacketPong packetIn) {
                pingLock.unLock();
                setPing(Minecraft.getSystemTime() - pingStart);
                networkmanager.closeChannel(new ChatComponentText("Finished"));
            }
            @Override
            public void onDisconnect(IChatComponent reason) {
                if (!this.hasSentPing) {
                    logger.error("Can't ping {}: {}", serverAddress, reason.getUnformattedText());
                }
            }
        });

        try {
            networkmanager.sendPacket(new C00Handshake(47, serveraddress.getIP(), serveraddress.getPort(), EnumConnectionState.STATUS));
            networkmanager.sendPacket(new C00PacketServerQuery());
        } catch (Exception e) {
            logger.info("Failed to ping {}", serveraddress);
            logger.info(e);
        }
    }




}

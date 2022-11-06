package kr.syeyoung.dungeonsguide.mod.features.impl.etc;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.events.listener.PacketListener;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.text.StyledText;
import kr.syeyoung.dungeonsguide.mod.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.utils.TitleRender;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class FeaturePing extends TextHUDFeature {

    public FeaturePing() {
        super("Misc", "Epic ping warner", "Shows ping and displays timeout warnings", "misc.epicping", false, getFontRenderer().getStringWidth("Ping: 100") + 4, getFontRenderer().FONT_HEIGHT + 2);
        addParameter("average", new FeatureParameter<>("average", "Show average", "Should the ping counter show the average over 5 seconds instead of current", true, "boolean", nval -> shouldShowAverage = nval));
        addParameter("timeoutwarn", new FeatureParameter<>("timeoutwarn", "Show timeout warnings", "Tells you when the server stopped responding", true, "boolean", nval -> shouldShowAverage = nval));
        setup();
        MinecraftForge.EVENT_BUS.register(this);
    }


    boolean shouldShowAverage = false;
    ArrayList<Long> averagePingStore = new ArrayList<>();
    Long averagePing = 0L;
    private boolean sendPingLock;
    private long whenWasPingLocked;

    static final int TIMEOUT_THRESHOLD = 330;

    boolean lastTimeOutStatus;
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e){
        if(e.phase != TickEvent.Phase.START || e.side != Side.CLIENT) return;
        if(System.currentTimeMillis() - PacketListener.lastPacketReceived > TIMEOUT_THRESHOLD){
            if(!lastTimeOutStatus){
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

    long ping = 0;

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
        if(FeatureRegistry.DEBUG.isEnabled()) logger.info("Updating ping: {}", ping);
    }

    Logger logger = LogManager.getLogger("FeaturePing");

    public void setup(){

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Dg Ping pool").build();
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, namedThreadFactory);

        scheduler.scheduleAtFixedRate(() -> {
            if(!isEnabled()) return;
            if(!SkyblockStatus.isOnSkyblock()) return;
            if(sendPingLock) {
                if(whenWasPingLocked + 400 < Minecraft.getSystemTime()){
                    sendPingLock = false;
                } else {
                    return;
                }
            }

            try {
                whenWasPingLocked = Minecraft.getSystemTime();
                sendPingLock = true;
                ping("mc.hypixel.net:25565");

            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }, 10,1000, TimeUnit.MILLISECONDS);
    }


    private static final List<StyledText> dummyText = new ArrayList<>();
    static {
        dummyText.add(new StyledText("Ping: ","base"));
        dummyText.add(new StyledText("999", "ping"));
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }



    @Override
    public boolean isHUDViewable() {
        return true;
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Arrays.asList("ping", "base");
    }

    @Override
    public List<StyledText> getText() {
        return Arrays.asList(
                new StyledText("Ping: ","base"),
                new StyledText(String.valueOf(shouldShowAverage ? averagePing : ping),"ping")
        );
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
                sendPingLock = false;
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


    private void legacyPing(String serverAdress) {
        final ServerAddress serveraddress = ServerAddress.fromString(serverAdress);
        (((new Bootstrap().group(NetworkManager.CLIENT_NIO_EVENTLOOP.getValue())).handler(new ChannelInitializer<Channel>(){

            @Override
            protected void initChannel(Channel channel) {
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException e) {
                    e.printStackTrace();
                }
                channel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>(){


                    long pingStart = 0L;

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     */
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        super.channelActive(ctx);
                        ByteBuf bytebuf = Unpooled.buffer();
                        try {
                            bytebuf.writeByte(254);
                            bytebuf.writeByte(1);
                            bytebuf.writeByte(250);
                            char[] achar = "MC|PingHost".toCharArray();
                            bytebuf.writeShort(achar.length);
                            for (char c0 : achar) {
                                bytebuf.writeChar(c0);
                            }
                            bytebuf.writeShort(7 + 2 * serveraddress.getIP().length());
                            bytebuf.writeByte(127);
                            achar = serveraddress.getIP().toCharArray();
                            bytebuf.writeShort(achar.length);
                            for (char c1 : achar) {
                                bytebuf.writeChar(c1);
                            }
                            bytebuf.writeInt(serveraddress.getPort());

                            pingStart = Minecraft.getSystemTime();
                            logger.info("sent Ping");
                            ctx.channel().writeAndFlush(bytebuf).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                        } finally {
                            bytebuf.release();
                        }
                    }

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                        short short1 = msg.readUnsignedByte();
                        if (short1 == 255) {
                            logger.info("Recived pong");
                            setPing(Minecraft.getSystemTime() - pingStart);
                        }
                        ctx.close();
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext e, Throwable t) {
                        e.close();
                    }
                });
            }
        })).channel(NioSocketChannel.class)).connect(serveraddress.getIP(), serveraddress.getPort());
    }




}

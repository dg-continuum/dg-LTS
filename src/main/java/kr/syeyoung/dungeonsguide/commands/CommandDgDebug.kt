package kr.syeyoung.dungeonsguide.commands

import kr.syeyoung.dungeonsguide.DungeonsGuide
import kr.syeyoung.dungeonsguide.Main
import kr.syeyoung.dungeonsguide.chat.ChatTransmitter
import kr.syeyoung.dungeonsguide.cosmetics.data.ActiveCosmetic
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext
import kr.syeyoung.dungeonsguide.dungeon.DungeonFacade
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoom
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoomInfoRegistry.loadAll
import kr.syeyoung.dungeonsguide.dungeon.DungeonRoomInfoRegistry.saveAll
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionState.Companion.turnIntoForm
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonSpecificDataProvider
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonSpecificDataProviderRegistry
import kr.syeyoung.dungeonsguide.dungeon.mechanics.impl.*
import kr.syeyoung.dungeonsguide.dungeon.roomdetection.NewDungeonRoomBuilder
import kr.syeyoung.dungeonsguide.dungeon.roomdetection.SizeBundleBuilder
import kr.syeyoung.dungeonsguide.dungeon.roomedit.EditingContext
import kr.syeyoung.dungeonsguide.dungeon.roomedit.gui.GuiDungeonRoomEdit
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.BossfightProcessor
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.impl.GeneralRoomProcessor
import kr.syeyoung.dungeonsguide.events.impl.DungeonLeftEvent
import kr.syeyoung.dungeonsguide.features.FeatureRegistry
import kr.syeyoung.dungeonsguide.party.PartyContext
import kr.syeyoung.dungeonsguide.party.PartyManager
import kr.syeyoung.dungeonsguide.utils.*
import kr.syeyoung.dungeonsguide.whosonline.WhosOnlineManager
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.joml.Vector2i
import org.joml.Vector3i
import java.awt.Dimension
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.regex.Pattern
import javax.vecmath.Vector2d

class CommandDgDebug : CommandBase() {
    var toOpen: GuiScreen? = null
    override fun getCommandName(): String {
        return "dgdebug"
    }

    override fun getCommandUsage(sender: ICommandSender): String {
        return "dgdebug"
    }

    @Throws(CommandException::class)
    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (args.isEmpty()) return
        when (args[0].lowercase()) {
            "scoreboard" -> {
                ScoreBoardUtils.forEachLine { l: String -> ChatTransmitter.addToQueue("LINE: $l", false) }
            }
            "scoreboardclean" -> {
                ScoreBoardUtils.forEachLineClean { l: String -> ChatTransmitter.addToQueue("LINE: $l", false) }
            }
            "title" -> {
                if (args.size == 2) {
                    println("Displayuing title:" + args[1])
                    TitleRender.displayTitle(args[1], "", 10, 40, 20)
                }
            }
            "mockdungeonstart" -> {
                if (!Minecraft.getMinecraft().isSingleplayer) {
                    ChatTransmitter.addToQueue("This only works in singlepauer", false)
                    return
                }
                if (args.size == 2) {
                    val time = args[1].toInt()
                    ChatTransmitter.addToQueue("§r§aDungeon starts in $time seconds.§r", false)
                    return
                }
                Thread {
                    try {
                        ChatTransmitter.addToQueue("§r§aDungeon starts in 15 seconds.§r", false)
                        Thread.sleep(6000)
                        ChatTransmitter.addToQueue("§r§aDungeon starts in 10 seconds.§r", false)
                        Thread.sleep(700)
                        ChatTransmitter.addToQueue("§r§aDungeon starts in 5 seconds.§r", false)
                        Thread.sleep(1000)
                        ChatTransmitter.addToQueue("§r§aDungeon starts in 4 seconds.§r", false)
                        Thread.sleep(1000)
                        ChatTransmitter.addToQueue("§r§aDungeon starts in 3 seconds.§r", false)
                        Thread.sleep(1000)
                        ChatTransmitter.addToQueue("§r§aDungeon starts in 2 seconds.§r", false)
                        Thread.sleep(1000)
                        ChatTransmitter.addToQueue("§r§aDungeon starts in 1 seconds.§r", false)
                    } catch (ignored: InterruptedException) {
                    }
                }.start()
            }
            "saverooms" -> {
                saveAll(Main.getConfigDir())
                sender.addChatMessage(ChatComponentText("§eDungeons Guide §7:: §fSuccessfully saved user generated roomdata"))
            }
            "loadrooms" -> {
                loadAll(Main.getConfigDir())
                sender.addChatMessage(ChatComponentText("§eDungeons Guide §7:: §fSuccessfully loaded roomdatas"))
            }
            "brand" -> {
                val serverBrand = Minecraft.getMinecraft().thePlayer.clientBrand
                sender.addChatMessage(ChatComponentText("§eDungeons Guide §7:: §e$serverBrand"))
            }
            "pathfind" -> {
                try {
                    val context = DungeonFacade.context
                    val thePlayer = Minecraft.getMinecraft().thePlayer ?: return
                    if (context!!.bossfightProcessor != null) context.bossfightProcessor!!.tick()
                    val roomPt = context.mapProcessor.worldPointToRoomPoint(VectorUtils.getPlayerVector3i())
                    val dungeonRoom = context.roomMapper[roomPt]
                    val grp = dungeonRoom!!.roomProcessor as GeneralRoomProcessor
                    grp.strategy.createActionRoute(
                        "COMMAND",
                        args[1],
                        turnIntoForm(args[2]),
                        FeatureRegistry.SECRET_LINE_PROPERTIES_GLOBAL.routeProperties
                    )
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
            "reloaddungeon" -> {
                try {
                    MinecraftForge.EVENT_BUS.post(DungeonLeftEvent())
                    DungeonFacade.context = null
                    MapUtils.clearMap()
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
            "sayhikotlin" -> {
                val a = HEY()
                a.doFunStuff()
            }
            "partyid" -> {
                sender.addChatMessage(
                    ChatComponentText(
                        "§eDungeons Guide §7:: §fInternal Party id: " + Optional.ofNullable(
                            PartyManager.INSTANCE.partyContext
                        ).map { obj: PartyContext -> obj.partyID }
                            .orElse(null)))
            }
            "loc" -> {
                val context = DungeonFacade.context
                if (context != null) {
                    sender.addChatMessage(ChatComponentText("§eDungeons Guide §7:: §fYou're in " + SkyblockStatus.dungeonNameStrriped))
                }
            }
            "createfakeroom" -> {
                val f = File(
                    Main.getConfigDir(),
                    "schematics/new roonm-b2df250c-4af2-4201-963c-0ee1cb6bd3de-5efb1f0c-c05f-4064-bde7-cad0874fdf39.schematic"
                )
                val compound: NBTTagCompound
                compound = try {
                    CompressedStreamTools.readCompressed(FileInputStream(f))
                } catch (e: IOException) {
                    e.printStackTrace()
                    return
                }
                val blocks = compound.getByteArray("Blocks")
                val meta = compound.getByteArray("Data")
                for (x in 0 until compound.getShort("Width")) {
                    for (y in 0 until compound.getShort("Height")) {
                        for (z in 0 until compound.getShort("Length")) {
                            val index = x + (y * compound.getShort("Length") + z) * compound.getShort("Width")
                            val pos = BlockPos(x, y, z)
                            val w = MinecraftServer.getServer().entityWorld
                            w.setBlockState(
                                pos, Block.getBlockById(blocks[index].toInt() and 0xFF).getStateFromMeta(
                                    meta[index].toInt() and 0xFF
                                ), 2
                            )
                        }
                    }
                }
                DungeonSpecificDataProviderRegistry.doorFinders[Pattern.compile("TEST DG")] =
                    object : DungeonSpecificDataProvider {
                        override fun findDoor(w: World, dungeonName: String): BlockPos? {
                            return BlockPos(0, 0, 0)
                        }

                        override fun findDoorOffset(w: World, dungeonName: String): Vector2d? {
                            return null
                        }

                        override fun createBossfightProcessor(w: World, dungeonName: String): BossfightProcessor? {
                            return null
                        }

                        override fun hasTrapRoom(dungeonName: String): Boolean {
                            return false
                        }

                        override fun secretPercentage(dungeonName: String): Double {
                            return 0.0
                        }

                        override fun speedSecond(dungeonName: String): Int {
                            return 0
                        }
                    }
                val fakeContext = DungeonContext()
                SkyblockStatus.dungeonNameStrriped = "TEST DG"
                DungeonFacade.context = fakeContext
                DungeonsGuide.getDungeonsGuide().skyblockStatus.isForceIsOnDungeon = true
                val mapProcessor = fakeContext.mapProcessor
                mapProcessor.unitRoomDimension = Dimension(16, 16)
                mapProcessor.doorDimensions = Dimension(4, 4)
                mapProcessor.topLeftMapPoint = Vector2i(0, 0)
                fakeContext.dungeonMin = BlockPos(0, 70, 0)
                val dungeonRoom = DungeonRoom(
                    listOf(Vector2i(0, 0)),
                    ShortUtils.topLeftifyInt(1.toShort()),
                    63.toByte(),
                    Vector3i(0, 70, 0),
                    Vector3i(31, 70, 31),
                    fakeContext,
                    emptySet()
                )
                fakeContext.dungeonRoomList.add(dungeonRoom)
                for (p in listOf(Vector2i(0, 0))) {
                    fakeContext.roomMapper[p] = dungeonRoom
                }
                EditingContext.createEditingContext(dungeonRoom)
                EditingContext.getEditingContext().openGui(GuiDungeonRoomEdit(dungeonRoom))
            }
            "closecontext" -> {
                DungeonsGuide.getDungeonsGuide().skyblockStatus.isForceIsOnDungeon = false
                DungeonFacade.context = null
            }
            "connectwhois" -> {
                DungeonsGuide.getDungeonsGuide().whosOnlineManager.close()
                DungeonsGuide.getDungeonsGuide().whosOnlineManager =
                    WhosOnlineManager("wss://virginity.kokoniara.software/ws")
                DungeonsGuide.getDungeonsGuide().whosOnlineManager.init()
            }
            "isonline" -> {
                if (args.size > 2) {
                    sender.addChatMessage(ChatComponentText("TOO LITTLE ARGS"))
                }
                if (DungeonsGuide.getDungeonsGuide().whosOnlineManager == null) {
                    sender.addChatMessage(ChatComponentText("didnt init manager"))
                }
                val tocheck = args[1]
                Thread {
                    val online = DungeonsGuide.getDungeonsGuide().whosOnlineManager.getWebsocketClient()!!
                        .isOnline(tocheck)
                    if (online != null) {
                        var aBoolean = false
                        try {
                            aBoolean = online.get()
                            ChatTransmitter.addToQueue(tocheck + " is " + if (aBoolean) "online" else "offline")
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        } catch (e: ExecutionException) {
                            e.printStackTrace()
                        }
                    } else {
                        ChatTransmitter.addToQueue("NULL")
                    }
                }.start()
            }
            "readmap" -> {
                try {
                    val fromX = args[1].toInt()
                    val fromY = args[2].toInt()
                    sender.addChatMessage(
                        ChatComponentText(
                            MapUtils.readDigit(MapUtils.getColors(), fromX, fromY).toString() + "-"
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            "listcosmetics" -> {
                DungeonsGuide.getDungeonsGuide().cosmeticsManager.activeCosmeticMap.forEach { (uuid: UUID, activeCosmetic: ActiveCosmetic) ->
                    println("OWNER UUID: $uuid")
                    println("   $activeCosmetic")
                }
            }
            "roomprocessor" -> {
                DungeonFacade.context?.let {
                    it.currentRoom?.updateRoomProcessor()
                }
            }
            "checkroom" -> {
                val room = NewDungeonRoomBuilder.build(DungeonContext())
                sender.addChatMessage(ChatComponentText("Got room: " + room.roomInfo.name))
            }
            "getroomshape" -> {
                val a = SizeBundleBuilder()
                val start = System.currentTimeMillis()
                val (roomshape) = a.generateRoomDataBundle(a.getTopOfRoom(VectorUtils.getPlayerVector3i()).sub(0, 2, 0))
                val stop = System.currentTimeMillis()
                val yes = stop - start
                sender.addChatMessage(ChatComponentText("Got " + roomshape + " in " + yes + "ms"))
            }
            "mockcosmetics" -> {
                toOpen = object : GuiScreen() {
                    private var activityUID: GuiTextField? = null
                    private var playerUID: GuiTextField? = null
                    private var cosmeticUID: GuiTextField? = null
                    private var username: GuiTextField? = null
                    override fun initGui() {
                        println("Init")
                        buttonList.add(
                            GuiButton(
                                106,
                                width / 2 - 155,
                                height / 6 + 72 - 6,
                                fontRendererObj.getStringWidth("add") + 5,
                                fontRendererObj.FONT_HEIGHT + 2,
                                "add"
                            )
                        )
                        buttonList.add(
                            GuiButton(
                                105,
                                width / 2 - 155,
                                height / 6 + 80 - 6,
                                fontRendererObj.getStringWidth("an p") + 5,
                                fontRendererObj.FONT_HEIGHT + 2,
                                "an p"
                            )
                        )
                        activityUID = GuiTextField(2, fontRendererObj, width / 2 - 68, height / 2 - 10, 137, 20)
                        activityUID!!.maxStringLength = 500
                        activityUID!!.setCanLoseFocus(true)
                        activityUID!!.text = "activityUID"
                        activityUID!!.setEnabled(false)
                        playerUID = GuiTextField(2, fontRendererObj, width / 2 - 68, height / 2 - 30, 137, 20)
                        playerUID!!.maxStringLength = 500
                        playerUID!!.setCanLoseFocus(true)
                        playerUID!!.text = "playerUID"
                        playerUID!!.setEnabled(false)
                        cosmeticUID = GuiTextField(2, fontRendererObj, width / 2 - 68, height / 2 - 50, 137, 20)
                        cosmeticUID!!.maxStringLength = 500
                        cosmeticUID!!.setCanLoseFocus(true)
                        cosmeticUID!!.text = "cosmeticUID"
                        cosmeticUID!!.setEnabled(false)
                        username = GuiTextField(2, fontRendererObj, width / 2 - 68, height / 2 - 70, 137, 20)
                        username!!.maxStringLength = 500
                        username!!.setCanLoseFocus(true)
                        username!!.text = "username"
                        username!!.setEnabled(false)
                        super.initGui()
                    }

                    @Throws(IOException::class)
                    override fun actionPerformed(button: GuiButton) {
                        if (button.id == 105) {
                            activityUID!!.text = "98e445dc-650f-49e1-ba6f-b12086533cf7"
                            cosmeticUID!!.text = "dd99fea2-1f18-49ee-92ae-6e00a7a5b75f"
                        }
                        if (button.id == 106) {
                            DungeonsGuide.getDungeonsGuide().cosmeticsManager.activeCosmeticMap.entries
                                .removeIf { (key): Map.Entry<UUID, ActiveCosmetic?> ->
                                    key == UUID.fromString(
                                        playerUID!!.text
                                    )
                                }
                            val cosmeticData = ActiveCosmetic()
                            cosmeticData.activityUID = UUID.fromString(activityUID!!.text)
                            cosmeticData.playerUID = UUID.fromString(playerUID!!.text)
                            cosmeticData.cosmeticData = UUID.fromString(cosmeticUID!!.text)
                            cosmeticData.username = username!!.text
                            DungeonsGuide.getDungeonsGuide().cosmeticsManager.activeCosmeticMap[cosmeticData.activityUID] =
                                cosmeticData
                            try {
                                val method =
                                    DungeonsGuide.getDungeonsGuide().cosmeticsManager.javaClass.getDeclaredMethod("rebuildCaches")
                                method.isAccessible = true
                                method.invoke(DungeonsGuide.getDungeonsGuide().cosmeticsManager)
                            } catch (e: NoSuchMethodException) {
                                e.printStackTrace()
                            } catch (e: InvocationTargetException) {
                                e.printStackTrace()
                            } catch (e: IllegalAccessException) {
                                e.printStackTrace()
                            }
                        }
                        super.actionPerformed(button)
                    }

                    @Throws(IOException::class)
                    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
                        cosmeticUID!!.mouseClicked(mouseX, mouseY, mouseButton)
                        playerUID!!.mouseClicked(mouseX, mouseY, mouseButton)
                        activityUID!!.mouseClicked(mouseX, mouseY, mouseButton)
                        username!!.mouseClicked(mouseX, mouseY, mouseButton)
                        cosmeticUID!!.setEnabled(cosmeticUID!!.isFocused)
                        playerUID!!.setEnabled(playerUID!!.isFocused)
                        activityUID!!.setEnabled(activityUID!!.isFocused)
                        username!!.setEnabled(username!!.isFocused)
                        super.mouseClicked(mouseX, mouseY, mouseButton)
                    }

                    @Throws(IOException::class)
                    override fun keyTyped(typedChar: Char, keyCode: Int) {
                        cosmeticUID!!.textboxKeyTyped(typedChar, keyCode)
                        playerUID!!.textboxKeyTyped(typedChar, keyCode)
                        activityUID!!.textboxKeyTyped(typedChar, keyCode)
                        username!!.textboxKeyTyped(typedChar, keyCode)
                        cosmeticUID!!.setEnabled(cosmeticUID!!.isFocused)
                        playerUID!!.setEnabled(playerUID!!.isFocused)
                        activityUID!!.setEnabled(activityUID!!.isFocused)
                        username!!.setEnabled(username!!.isFocused)
                        super.keyTyped(typedChar, keyCode)
                    }

                    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
                        drawDefaultBackground()
                        cosmeticUID!!.drawTextBox()
                        playerUID!!.drawTextBox()
                        activityUID!!.drawTextBox()
                        username!!.drawTextBox()
                        super.drawScreen(mouseX, mouseY, partialTicks)
                    }
                }
            }
            else -> {
                sender.addChatMessage(ChatComponentText("ain't gonna find much anything here"))
                sender.addChatMessage(ChatComponentText("§eDungeons Guide §7:: §e/dg loadrooms §7-§f Reloads dungeon roomdata."))
                sender.addChatMessage(ChatComponentText("§eDungeons Guide §7:: §e/dg brand §7-§f View server brand."))
                sender.addChatMessage(ChatComponentText("§eDungeons Guide §7:: §e/dg info §7-§f View Current DG User info."))
                sender.addChatMessage(ChatComponentText("§eDungeons Guide §7:: §e/dg saverun §7-§f Save run to be sent to developer."))
                sender.addChatMessage(ChatComponentText("§eDungeons Guide §7:: §e/dg saverooms §7-§f Saves usergenerated dungeon roomdata."))
            }
        }
    }

    @SubscribeEvent
    fun onTick(e: ClientTickEvent) {
        try {
            if (toOpen != null && e.phase == TickEvent.Phase.START) {
                Minecraft.getMinecraft().displayGuiScreen(toOpen)
                toOpen = null
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }
}
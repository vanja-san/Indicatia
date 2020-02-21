package stevekung.mods.indicatia.event;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import org.lwjgl.input.Keyboard;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.status.INetHandlerStatusClient;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.network.status.server.S00PacketServerInfo;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.potion.Potion;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import stevekung.mods.indicatia.config.ConfigManagerIN;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.core.IndicatiaMod;
import stevekung.mods.indicatia.gui.GuiButtonItem;
import stevekung.mods.indicatia.gui.GuiButtonMojangStatus;
import stevekung.mods.indicatia.gui.GuiConfirmDisconnect;
import stevekung.mods.indicatia.gui.GuiMojangStatusChecker;
import stevekung.mods.indicatia.gui.api.GuiSkyBlockAPIViewer;
import stevekung.mods.indicatia.gui.config.GuiExtendedConfig;
import stevekung.mods.indicatia.gui.config.GuiRenderPreview;
import stevekung.mods.indicatia.handler.KeyBindingHandler;
import stevekung.mods.indicatia.utils.*;

public class IndicatiaEventHandler
{
    private final Minecraft mc;
    public static int currentServerPing;
    private static final ThreadPoolExecutor REALTIME_PINGER = new ScheduledThreadPoolExecutor(5, new ThreadFactoryBuilder().setNameFormat("Real Time Server Pinger #%d").setDaemon(true).build());
    private long lastPinger = -1L;
    private long lastButtonClick = -1;
    private static final List<String> INVENTORY_LIST = new ArrayList<>(Arrays.asList("Trades", "Shop Trading Options", "Runic Pedestal"));
    public static String auctionPrice = "";
    public static final List<String> CHATABLE_LIST = new ArrayList<>(Arrays.asList("You                  Other", "Ender Chest", "Craft Item", "Trades", "Shop Trading Options", "Runic Pedestal", "Your Bids", "Bank", "Bank Deposit", "Bank Withdrawal"));
    public static boolean showChat;
    private static long sneakTimeOld;
    private static boolean sneakingOld;
    public static String playerToView;

    public IndicatiaEventHandler()
    {
        this.mc = Minecraft.getMinecraft();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (this.mc.thePlayer != null)
        {
            if (event.phase == TickEvent.Phase.START)
            {
                if (this.mc.getCurrentServerData() != null)
                {
                    long now = System.currentTimeMillis();

                    if (this.lastPinger == -1L || now - this.lastPinger > 5000L)
                    {
                        this.lastPinger = now;
                        IndicatiaEventHandler.getRealTimeServerPing(this.mc.getCurrentServerData());
                    }
                }

                for (EnumAction action : CachedEnum.actionValues)
                {
                    if (action != EnumAction.NONE)
                    {
                        if (ConfigManagerIN.enableAdditionalBlockhitAnimation && this.mc.gameSettings.keyBindAttack.isKeyDown() && this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && this.mc.thePlayer.getCurrentEquippedItem() != null && this.mc.thePlayer.getCurrentEquippedItem().getItemUseAction() == action)
                        {
                            this.mc.thePlayer.swingItem();
                        }
                    }
                }
            }
        }
        GuiIngameForge.renderObjective = ConfigManagerIN.enableRenderScoreboard;
    }

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent event)
    {
        MovementInput movement = event.getMovementInput();
        EntityPlayer player = event.getEntityPlayer();
        String[] keyTS = ConfigManagerIN.keyToggleSprint.split(",");
        int keyTGCtrl = InfoUtils.INSTANCE.parseInt(keyTS[0], "Toggle Sprint");
        int keyTGOther = InfoUtils.INSTANCE.parseInt(keyTS[1], "Toggle Sprint");

        if (this.mc.currentScreen == null && this.mc.gameSettings.keyBindSneak.getKeyCode() != Keyboard.KEY_LCONTROL && keyTGCtrl == Keyboard.KEY_LCONTROL && keyTGOther == Keyboard.KEY_S && Keyboard.isKeyDown(keyTGCtrl) && Keyboard.isKeyDown(keyTGOther))
        {
            ++movement.moveForward;
        }

        // toggle sneak
        movement.sneak = this.mc.gameSettings.keyBindSneak.isKeyDown() || ExtendedConfig.instance.toggleSneak && !event.getEntityPlayer().isSpectator();

        if (ExtendedConfig.instance.toggleSneak && !this.mc.gameSettings.keyBindSneak.isKeyDown() && !player.isSpectator() && !player.capabilities.isCreativeMode)
        {
            movement.moveStrafe = (float)(movement.moveStrafe * 0.3D);
            movement.moveForward = (float)(movement.moveForward * 0.3D);
        }

        // toggle sprint
        if (ExtendedConfig.instance.toggleSprint && !player.isPotionActive(Potion.blindness) && !ExtendedConfig.instance.toggleSneak)
        {
            player.setSprinting(true);
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            if (ConfigManagerIN.enableSmoothSneakingView)
            {
                if (this.mc.thePlayer != null)
                {
                    this.mc.thePlayer.eyeHeight = IndicatiaEventHandler.getSmoothEyeHeight(this.mc.thePlayer);
                }
            }
            if (IndicatiaEventHandler.playerToView != null)
            {
                this.mc.displayGuiScreen(new GuiSkyBlockAPIViewer(GuiSkyBlockAPIViewer.GuiState.PLAYER, IndicatiaEventHandler.playerToView));
                IndicatiaEventHandler.playerToView = null;
            }
        }
    }

    @SubscribeEvent
    public void onPressKey(InputEvent.KeyInputEvent event)
    {
        if (KeyBindingHandler.KEY_QUICK_CONFIG.isKeyDown())
        {
            this.mc.displayGuiScreen(new GuiExtendedConfig());
        }

        if (ExtendedConfig.instance.toggleSprintUseMode.equals("key_binding"))
        {
            String[] keyTS = ConfigManagerIN.keyToggleSprint.split(",");
            int keyTGCtrl = InfoUtils.INSTANCE.parseInt(keyTS[0], "Toggle Sprint");
            int keyTGOther = InfoUtils.INSTANCE.parseInt(keyTS[1], "Toggle Sprint");

            if (Keyboard.isKeyDown(keyTGCtrl) && Keyboard.isKeyDown(keyTGOther))
            {
                ExtendedConfig.instance.toggleSprint = !ExtendedConfig.instance.toggleSprint;
                ClientUtils.setOverlayMessage(JsonUtils.create(ExtendedConfig.instance.toggleSprint ? LangUtils.translate("message.toggle_sprint_enabled") : LangUtils.translate("message.toggle_sprint_disabled")).getFormattedText());
                ExtendedConfig.instance.save();
            }
        }
        if (ExtendedConfig.instance.toggleSneakUseMode.equals("key_binding"))
        {
            String[] keyTS = ConfigManagerIN.keyToggleSneak.split(",");
            int keyTGCtrl = InfoUtils.INSTANCE.parseInt(keyTS[0], "Toggle Sneak");
            int keyTGOther = InfoUtils.INSTANCE.parseInt(keyTS[1], "Toggle Sneak");

            if (Keyboard.isKeyDown(keyTGCtrl) && Keyboard.isKeyDown(keyTGOther))
            {
                ExtendedConfig.instance.toggleSneak = !ExtendedConfig.instance.toggleSneak;
                ClientUtils.setOverlayMessage(JsonUtils.create(ExtendedConfig.instance.toggleSneak ? LangUtils.translate("message.toggle_sneak_enabled") : LangUtils.translate("message.toggle_sneak_disabled")).getFormattedText());
                ExtendedConfig.instance.save();
            }
        }
    }

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event)
    {
        int width = event.gui.width / 2;
        int height = event.gui.height / 2 - 106;

        if (event.gui instanceof GuiMainMenu)
        {
            height = event.gui.height / 4 + 48;
            event.buttonList.add(new GuiButtonMojangStatus(200, width + 104, height + (IndicatiaMod.isIngameAccountSwitcherLoaded ? 63 : 84)));
        }
        if (HypixelEventHandler.isSkyBlock)
        {
            if (event.gui instanceof GuiInventory)
            {
                GuiButton craftingButton = new GuiButtonItem(1000, width + 10, height + 86, width + 70, Item.getItemFromBlock(Blocks.crafting_table));
                craftingButton.visible = HypixelEventHandler.SKY_BLOCK_LOCATION.isHub();
                event.buttonList.add(craftingButton);
                event.buttonList.add(new GuiButtonItem(999, width - 9, height + 86, width + 51, Item.getItemFromBlock(Blocks.ender_chest)));
            }
            else if (event.gui instanceof GuiChest)
            {
                GuiChest chest = (GuiChest)event.gui;
                IInventory lowerChestInventory = chest.lowerChestInventory;
                GuiButton craftingButton = new GuiButtonItem(1000, width + 88, height + 65, Item.getItemFromBlock(Blocks.crafting_table), HypixelEventHandler.SKY_BLOCK_LOCATION.isHub());

                if (IndicatiaEventHandler.isSuitableForGUI(IndicatiaEventHandler.CHATABLE_LIST, lowerChestInventory))
                {
                    event.buttonList.add(new GuiButton(500, width - 108, height + 190, 20, 20, "C"));
                }

                if (IndicatiaEventHandler.isSuitableForGUI(IndicatiaEventHandler.INVENTORY_LIST, lowerChestInventory))
                {
                    event.buttonList.add(new GuiButtonItem(999, width + 88, height + 47, Item.getItemFromBlock(Blocks.ender_chest)));
                    event.buttonList.add(craftingButton);
                    event.buttonList.add(new GuiButtonItem(1001, width + 88, height + 65, width + 88, Items.nether_star, HypixelEventHandler.SKY_BLOCK_LOCATION.isShopOutsideHub(), "SkyBlock Menu"));
                }
                else if (lowerChestInventory.getDisplayName().getUnformattedText().equals("Craft Item"))
                {
                    event.buttonList.add(new GuiButtonItem(999, width + 88, height + 47, Item.getItemFromBlock(Blocks.ender_chest)));
                    event.buttonList.add(new GuiButtonItem(1001, width + 88, height + 65, Items.nether_star, "SkyBlock Menu"));
                }
                else if (lowerChestInventory.getDisplayName().getUnformattedText().equals("Ender Chest"))
                {
                    craftingButton = new GuiButtonItem(1000, width + 88, height + 47, Item.getItemFromBlock(Blocks.crafting_table), HypixelEventHandler.SKY_BLOCK_LOCATION.isHub());
                    event.buttonList.add(craftingButton);
                    event.buttonList.add(new GuiButtonItem(1001, width + 88, height + (HypixelEventHandler.SKY_BLOCK_LOCATION.isHub() ? 65 : 47), Items.nether_star, "SkyBlock Menu"));
                }
            }
        }
        if (event.gui instanceof GuiControls)
        {
            event.buttonList.removeIf(button -> button.id == 200 || button.id == 201);
            event.buttonList.add(new GuiButton(200, width - 155 + 160, event.gui.height - 29, 150, 20, LangUtils.translate("gui.done")));
            event.buttonList.add(new GuiButton(201, width - 155, event.gui.height - 29, 150, 20, LangUtils.translate("controls.resetAll")));
        }
        if (event.gui instanceof GuiOptions)
        {
            event.buttonList.removeIf(button -> button.id == 107);
        }
        if (event.gui instanceof GuiMainMenu)
        {
            int j = event.gui.height / 4 + 48;
            event.buttonList.removeIf(button -> button.id == 14 || button.id == 6);
            event.buttonList.add(new GuiButton(6, width - 100, j + 24 * 2, LangUtils.translate("fml.menu.mods")));
        }
    }

    @SubscribeEvent
    public void onPreActionPerformedGui(GuiScreenEvent.ActionPerformedEvent.Pre event)
    {
        if (ConfigManagerIN.enableConfirmToDisconnect && event.gui instanceof GuiIngameMenu && !this.mc.isSingleplayer())
        {
            if (event.button.id == 1)
            {
                event.setCanceled(true);
                event.button.playPressSound(this.mc.getSoundHandler());
                this.mc.displayGuiScreen(new GuiConfirmDisconnect());
            }
        }
    }

    @SubscribeEvent
    public void onPostActionPerformedGui(GuiScreenEvent.ActionPerformedEvent.Post event)
    {
        long now = System.currentTimeMillis();

        if (event.gui instanceof GuiMainMenu)
        {
            if (event.button.id == 200)
            {
                this.mc.displayGuiScreen(new GuiMojangStatusChecker(event.gui));
            }
        }
        if ((event.gui instanceof GuiInventory || event.gui instanceof GuiChest) && HypixelEventHandler.isSkyBlock)
        {
            if (now - this.lastButtonClick > 100L)
            {
                if (event.button.id == 999)
                {
                    this.mc.thePlayer.sendChatMessage("/enderchest");
                }
                else if (event.button.id == 1000)
                {
                    this.mc.thePlayer.sendChatMessage("/viewcraftingtable");
                }
                else if (event.button.id == 1001)
                {
                    this.mc.thePlayer.sendChatMessage("/sbmenu");
                }
                this.lastButtonClick = now;
            }
            if (event.button.id == 500)
            {
                IndicatiaEventHandler.showChat = !IndicatiaEventHandler.showChat;
            }
        }
    }

    @SubscribeEvent
    public void onPostGuiDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event)
    {
        for (GuiButton button : event.gui.buttonList.stream().filter(button -> button instanceof GuiButtonItem).collect(Collectors.toList()))
        {
            boolean hover = event.mouseX >= button.xPosition && event.mouseY >= button.yPosition && event.mouseX < button.xPosition + button.width && event.mouseY < button.yPosition + button.height;

            if (hover && button.visible)
            {
                GuiUtils.drawHoveringText(Collections.singletonList(((GuiButtonItem)button).getName()), event.mouseX, event.mouseY, event.gui.width, event.gui.height, -1, this.mc.fontRendererObj);
                GlStateManager.disableLighting();
            }
        }
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event)
    {
        if (this.mc.currentScreen instanceof GuiRenderPreview)
        {
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event)
    {
        if (event.gui instanceof GuiMainMenu)
        {
            GuiMainMenu menu = (GuiMainMenu)event.gui;

            if (CalendarUtils.isSteveKunGBirthDay())
            {
                menu.splashText = "Happy birthday, SteveKunG!";
            }
        }
    }

    private static void getRealTimeServerPing(ServerData server)
    {
        IndicatiaEventHandler.REALTIME_PINGER.submit(() ->
        {
            try
            {
                ServerAddress address = ServerAddress.fromString(server.serverIP);
                NetworkManager manager = NetworkManager.func_181124_a(InetAddress.getByName(address.getIP()), address.getPort(), false);

                manager.setNetHandler(new INetHandlerStatusClient()
                {
                    private long currentSystemTime = 0L;

                    @Override
                    public void handleServerInfo(S00PacketServerInfo packet)
                    {
                        this.currentSystemTime = Minecraft.getSystemTime();
                        manager.sendPacket(new C01PacketPing(this.currentSystemTime));
                    }

                    @Override
                    public void handlePong(S01PacketPong packet)
                    {
                        long i = this.currentSystemTime;
                        long j = Minecraft.getSystemTime();
                        IndicatiaEventHandler.currentServerPing = (int) (j - i);
                    }

                    @Override
                    public void onDisconnect(IChatComponent component) {}
                });
                manager.sendPacket(new C00Handshake(47, address.getIP(), address.getPort(), EnumConnectionState.STATUS));
                manager.sendPacket(new C00PacketServerQuery());
            }
            catch (Exception e) {}
        });
    }

    private static float getSmoothEyeHeight(EntityPlayer player)
    {
        if (IndicatiaEventHandler.sneakingOld != player.isSneaking() || IndicatiaEventHandler.sneakTimeOld <= 0L)
        {
            IndicatiaEventHandler.sneakTimeOld = System.currentTimeMillis();
        }

        IndicatiaEventHandler.sneakingOld = player.isSneaking();
        float defaultEyeHeight = 1.62F;
        double sneakPress = 0.0006D;
        double sneakValue = 0.005D;
        int sneakTime = -35;
        long smoothRatio = 88L;

        if (player.isSneaking())
        {
            int sneakSystemTime = (int)(IndicatiaEventHandler.sneakTimeOld + smoothRatio - System.currentTimeMillis());

            if (sneakSystemTime > sneakTime)
            {
                defaultEyeHeight += (float)(sneakSystemTime * sneakPress);

                if (defaultEyeHeight < 0.0F || defaultEyeHeight > 10.0F)
                {
                    defaultEyeHeight = 1.54F;
                }
            }
            else
            {
                defaultEyeHeight = (float)(defaultEyeHeight - sneakValue);
            }
        }
        else
        {
            int sneakSystemTime = (int)(IndicatiaEventHandler.sneakTimeOld + smoothRatio - System.currentTimeMillis());

            if (sneakSystemTime > sneakTime)
            {
                defaultEyeHeight -= (float)(sneakSystemTime * sneakPress);
                defaultEyeHeight = (float)(defaultEyeHeight - sneakValue);

                if (defaultEyeHeight < 0.0F)
                {
                    defaultEyeHeight = 1.62F;
                }
            }
            else
            {
                defaultEyeHeight -= 0.0F;
            }
        }
        return defaultEyeHeight;
    }

    private static boolean isSuitableForGUI(List<String> invList, IInventory lowerChestInventory)
    {
        return invList.stream().anyMatch(invName -> lowerChestInventory.getDisplayName().getUnformattedText().equals(invName));
    }
}
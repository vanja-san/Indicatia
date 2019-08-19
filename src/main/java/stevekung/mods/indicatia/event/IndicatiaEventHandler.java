package stevekung.mods.indicatia.event;

import java.net.InetAddress;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.status.INetHandlerStatusClient;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.network.status.server.S00PacketServerInfo;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.*;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import stevekung.mods.indicatia.config.ConfigManagerIN;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.gui.GuiButtonMojangStatus;
import stevekung.mods.indicatia.gui.GuiConfirmDisconnect;
import stevekung.mods.indicatia.gui.GuiMojangStatusChecker;
import stevekung.mods.indicatia.gui.config.GuiExtendedConfig;
import stevekung.mods.indicatia.gui.config.GuiRenderPreview;
import stevekung.mods.indicatia.handler.KeyBindingHandler;
import stevekung.mods.indicatia.utils.*;
import stevekung.mods.indicatia.utils.JsonUtils;

public class IndicatiaEventHandler
{
    private Minecraft mc;
    public static int currentServerPing;
    private static final ThreadPoolExecutor serverPinger = new ScheduledThreadPoolExecutor(5, new ThreadFactoryBuilder().setNameFormat("Real Time Server Pinger #%d").setDaemon(true).build());
    private static int pendingPingTicks = 100;
    private int disconnectClickCount;
    private int disconnectClickCooldown;

    public static boolean isAFK;
    public static String afkMode = "idle";
    public static String afkReason;
    public static int afkMoveTicks;
    public static int afkTicks;

    private static long sneakTimeOld = 0L;
    private static boolean sneakingOld = false;

    public static boolean isSkyBlock = false;

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
                IndicatiaEventHandler.runAFK(this.mc.thePlayer);
                ScoreObjective scoreObj = this.mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);

                if (scoreObj != null)
                {
                    IndicatiaEventHandler.isSkyBlock = scoreObj.getDisplayName().contains("SKYBLOCK");
                }
                else
                {
                    IndicatiaEventHandler.isSkyBlock = false;
                }

                if (this.disconnectClickCooldown > 0)
                {
                    this.disconnectClickCooldown--;
                }
                if (this.mc.currentScreen != null && this.mc.currentScreen instanceof GuiIngameMenu)
                {
                    if (ConfigManagerIN.enableConfirmDisconnectButton && !this.mc.isSingleplayer())
                    {
                        this.mc.currentScreen.buttonList.forEach(button ->
                        {
                            if (button.id == 1 && ConfigManagerIN.confirmDisconnectMode.equals("click"))
                            {
                                if (this.disconnectClickCooldown < 60)
                                {
                                    int cooldownSec = 1 + this.disconnectClickCooldown / 20;
                                    button.displayString = EnumChatFormatting.RED + LangUtils.translate("message.confirm_disconnect") + " in " + cooldownSec + "...";
                                }
                                if (this.disconnectClickCooldown == 0)
                                {
                                    button.displayString = LangUtils.translate("menu.disconnect");
                                    this.disconnectClickCount = 0;
                                }
                            }
                        });
                    }
                }
                else
                {
                    this.disconnectClickCount = 0;
                    this.disconnectClickCooldown = 0;
                }

                if (IndicatiaEventHandler.pendingPingTicks > 0 && this.mc.getCurrentServerData() != null)
                {
                    IndicatiaEventHandler.pendingPingTicks--;

                    if (IndicatiaEventHandler.pendingPingTicks == 0)
                    {
                        IndicatiaEventHandler.getRealTimeServerPing(this.mc.getCurrentServerData());
                        IndicatiaEventHandler.pendingPingTicks = 100;
                    }
                }

                for (EnumAction action : CachedEnum.actionValues)
                {
                    if (action != EnumAction.NONE)
                    {
                        if (ConfigManagerIN.enableAdditionalBlockhitAnimation && this.mc.gameSettings.keyBindAttack.isKeyDown() && this.mc.thePlayer != null && this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && this.mc.thePlayer.getCurrentEquippedItem() != null && this.mc.thePlayer.getCurrentEquippedItem().getItemUseAction() == action)
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

        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_S))
        {
            ++movement.moveForward;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_F3) && Keyboard.isKeyDown(Keyboard.KEY_D))
        {
            ++movement.moveStrafe;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_F3) && Keyboard.isKeyDown(Keyboard.KEY_A))
        {
            --movement.moveStrafe;
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

        // afk stuff
        if (!IndicatiaEventHandler.afkMode.equals("360_move"))
        {
            int afkMoveTick = IndicatiaEventHandler.afkMoveTicks;

            if (afkMoveTick > 0 && afkMoveTick < 2)
            {
                movement.moveForward += Math.random();
            }
            else if (afkMoveTick > 2 && afkMoveTick < 4)
            {
                movement.moveStrafe += Math.random();
            }
            else if (afkMoveTick > 4 && afkMoveTick < 6)
            {
                movement.moveForward -= Math.random();
            }
            else if (afkMoveTick > 6 && afkMoveTick < 8)
            {
                movement.moveStrafe -= Math.random();
            }
        }
    }

    @SubscribeEvent
    public void onDisconnectedFromServerEvent(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        IndicatiaEventHandler.stopCommandTicks();
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event)
    {
        IndicatiaEventHandler.stopCommandTicks();
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        IndicatiaEventHandler.stopCommandTicks();
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            InfoUtils.INSTANCE.processMouseOverEntity(this.mc);

            if (ConfigManagerIN.enableSmoothSneakingView)
            {
                if (this.mc.thePlayer != null)
                {
                    this.mc.thePlayer.eyeHeight = IndicatiaEventHandler.getSmoothEyeHeight(this.mc.thePlayer);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPressKey(InputEvent.KeyInputEvent event)
    {
        if (KeyBindingHandler.KEY_QUICK_CONFIG.isKeyDown())
        {
            GuiExtendedConfig config = new GuiExtendedConfig();
            this.mc.displayGuiScreen(config);
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
        if (event.gui instanceof GuiMainMenu)
        {
            int height = event.gui.height / 4 + 48;
            event.buttonList.add(new GuiButtonMojangStatus(200, event.gui.width / 2 + 104, height + 84));
        }
    }

    @SubscribeEvent
    public void onPreActionPerformedGui(GuiScreenEvent.ActionPerformedEvent.Pre event)
    {
        if (ConfigManagerIN.enableConfirmDisconnectButton && event.gui instanceof GuiIngameMenu && !this.mc.isSingleplayer())
        {
            if (event.button.id == 1)
            {
                event.setCanceled(true);
                event.button.playPressSound(this.mc.getSoundHandler());

                if (ConfigManagerIN.confirmDisconnectMode.equals("gui"))
                {
                    this.mc.displayGuiScreen(new GuiConfirmDisconnect());
                }
                else
                {
                    this.disconnectClickCount++;
                    event.button.displayString = EnumChatFormatting.RED + LangUtils.translate("message.confirm_disconnect");

                    if (this.disconnectClickCount == 1)
                    {
                        this.disconnectClickCooldown = 100;
                    }
                    if (this.disconnectClickCount == 2)
                    {
                        this.mc.theWorld.sendQuittingDisconnectingPacket();
                        this.mc.loadWorld(null);
                        this.mc.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
                        this.disconnectClickCount = 0;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPostActionPerformedGui(GuiScreenEvent.ActionPerformedEvent.Post event)
    {
        if (event.gui instanceof GuiMainMenu)
        {
            if (event.button.id == 200)
            {
                this.mc.displayGuiScreen(new GuiMojangStatusChecker(event.gui));
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
    public void onPreRenderLivingSpecials(RenderLivingEvent.Specials.Pre<EntityLivingBase> event)
    {
        EntityLivingBase entity = event.entity;
        event.setCanceled(true);

        if (this.canRenderName(entity))
        {
            double d0 = entity.getDistanceSqToEntity(this.mc.getRenderManager().livingPlayer);
            float f = entity.isSneaking() ? RendererLivingEntity.NAME_TAG_RANGE_SNEAK : RendererLivingEntity.NAME_TAG_RANGE;

            if (d0 < f * f)
            {
                String name = entity.getDisplayName().getFormattedText();
                GlStateManager.alphaFunc(516, 0.1F);
                IndicatiaEventHandler.renderEntityName(this.mc, entity, name, event.x, event.y, event.z);
            }
        }
    }

    private boolean canRenderName(EntityLivingBase entity)
    {
        if (entity instanceof EntityLiving)
        {
            return this.getDefaultCanRenderName(entity) && (entity.getAlwaysRenderNameTagForRender() || entity.hasCustomName() && entity == this.mc.getRenderManager().pointedEntity);
        }
        else if (entity instanceof EntityArmorStand)
        {
            return entity.getAlwaysRenderNameTag();
        }
        return this.getDefaultCanRenderName(entity);
    }

    private boolean getDefaultCanRenderName(EntityLivingBase entity)
    {
        EntityPlayerSP playerSP = this.mc.thePlayer;

        if (entity instanceof EntityPlayer && entity != playerSP)
        {
            Team team = entity.getTeam();
            Team team1 = playerSP.getTeam();

            if (team != null)
            {
                Team.EnumVisible visible = team.getNameTagVisibility();

                switch (visible)
                {
                case ALWAYS:
                    return true;
                case NEVER:
                    return false;
                case HIDE_FOR_OTHER_TEAMS:
                    return team1 == null || team.isSameTeam(team1);
                case HIDE_FOR_OWN_TEAM:
                    return team1 == null || !team.isSameTeam(team1);
                default:
                    return true;
                }
            }
        }
        return Minecraft.isGuiEnabled() && entity != this.mc.getRenderManager().livingPlayer && !entity.isInvisibleToPlayer(playerSP) && entity.riddenByEntity == null;
    }

    private static void getRealTimeServerPing(ServerData server)
    {
        IndicatiaEventHandler.serverPinger.submit(() ->
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

    private static void runAFK(EntityPlayerSP player)
    {
        if (IndicatiaEventHandler.isAFK)
        {
            IndicatiaEventHandler.afkTicks++;
            int tick = IndicatiaEventHandler.afkTicks;
            int messageMin = 1200 * ConfigManagerIN.afkMessageTime;
            String s = "s";
            float angle = (float)(tick % 2 == 0 ? Math.random() : -Math.random());

            if (tick == 0)
            {
                s = "";
            }
            if (ConfigManagerIN.enableAFKMessage)
            {
                if (tick % messageMin == 0)
                {
                    String reason = IndicatiaEventHandler.afkReason;
                    reason = reason.isEmpty() ? "" : ", Reason : " + reason;
                    player.sendChatMessage("AFK : " + StringUtils.ticksToElapsedTime(tick) + " minute" + s + reason);
                }
            }

            if (IndicatiaEventHandler.afkMode.equals("idle"))
            {
                player.setAngles(angle, angle);
            }
            else if (IndicatiaEventHandler.afkMode.equals("360"))
            {
                player.setAngles((float)(Math.random() + 1.0F), 0.0F);
            }
            else if (IndicatiaEventHandler.afkMode.equals("360_move"))
            {
                player.setAngles((float)(Math.random() + 1.0F), 0.0F);
                IndicatiaEventHandler.afkMoveTicks++;
                IndicatiaEventHandler.afkMoveTicks %= 8;
            }
            else
            {
                player.setAngles(angle, angle);
                IndicatiaEventHandler.afkMoveTicks++;
                IndicatiaEventHandler.afkMoveTicks %= 8;
            }
        }
        else
        {
            IndicatiaEventHandler.afkTicks = 0;
        }
    }

    private static void stopCommandTicks()
    {
        if (IndicatiaEventHandler.isAFK)
        {
            IndicatiaEventHandler.isAFK = false;
            IndicatiaEventHandler.afkReason = "";
            IndicatiaEventHandler.afkTicks = 0;
            IndicatiaEventHandler.afkMoveTicks = 0;
            IndicatiaEventHandler.afkMode = "idle";
            LoggerIN.info("Stopping AFK Command");
        }
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

    private static void renderEntityName(Minecraft mc, EntityLivingBase entity, String str, double x, double y, double z)
    {
        int maxDistance = 64;
        double d0 = entity.getDistanceSqToEntity(mc.getRenderViewEntity());

        if (d0 <= maxDistance * maxDistance)
        {
            boolean flag = entity.isSneaking();
            float f = mc.getRenderManager().playerViewY;
            float f1 = mc.getRenderManager().playerViewX;
            boolean flag1 = mc.getRenderManager().options.thirdPersonView == 2;
            float f2 = entity.height + 0.5F - (flag ? 0.25F : 0.0F);
            int i = "deadmau5".equals(str) ? -10 : 0;
            IndicatiaEventHandler.drawNameplate(mc.getRenderManager().getFontRenderer(), str, (float)x, (float)y + f2, (float)z, i, f, f1, flag1, flag);
        }
    }

    private static void drawNameplate(FontRenderer fontRenderer, String str, float x, float y, float z, int verticalShift, float viewerYaw, float viewerPitch, boolean isThirdPersonFrontal, boolean isSneaking)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.025F, -0.025F, 0.025F);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);

        if (!isSneaking)
        {
            GlStateManager.disableDepth();
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        int i = fontRenderer.getStringWidth(str) / 2;
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer vertexbuffer = tessellator.getWorldRenderer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos(-i - 1, -1 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        vertexbuffer.pos(-i - 1, 8 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        vertexbuffer.pos(i + 1, 8 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        vertexbuffer.pos(i + 1, -1 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();

        if (!isSneaking)
        {
            fontRenderer.drawString(str, -fontRenderer.getStringWidth(str) / 2, verticalShift, 553648127);
            GlStateManager.enableDepth();
        }

        GlStateManager.depthMask(true);
        fontRenderer.drawString(str, -fontRenderer.getStringWidth(str) / 2, verticalShift, isSneaking ? 553648127 : -1);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}
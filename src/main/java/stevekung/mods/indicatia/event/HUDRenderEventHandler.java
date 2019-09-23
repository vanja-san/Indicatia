package stevekung.mods.indicatia.event;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import stevekung.mods.indicatia.config.ConfigManagerIN;
import stevekung.mods.indicatia.config.EnumEquipment;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.config.HealthStatusMode;
import stevekung.mods.indicatia.gui.config.GuiRenderPreview;
import stevekung.mods.indicatia.handler.ClientBlockBreakEvent;
import stevekung.mods.indicatia.handler.GrapplingHookEvent;
import stevekung.mods.indicatia.renderer.HUDInfo;
import stevekung.mods.indicatia.utils.ColorUtils;
import stevekung.mods.indicatia.utils.InfoUtils;
import stevekung.mods.indicatia.utils.JsonUtils;
import stevekung.mods.indicatia.utils.RenderUtils;

public class HUDRenderEventHandler
{
    private final Minecraft mc;
    private long lastBlockBreak = -1;
    private long lastGrapplingHookUse = -1;

    public HUDRenderEventHandler()
    {
        this.mc = Minecraft.getMinecraft();
    }

    @SubscribeEvent
    public void onGrapplingHookUse(GrapplingHookEvent event)
    {
        long now = System.currentTimeMillis();
        boolean isHook = EnumChatFormatting.getTextWithoutFormattingCodes(event.getItemStack().getDisplayName()).equals("Grappling Hook");

        if (now - this.lastGrapplingHookUse > ExtendedConfig.instance.grapplingHookDelay && isHook)
        {
            this.lastGrapplingHookUse = now;
        }
    }

    @SubscribeEvent
    public void onClientBlockBreak(ClientBlockBreakEvent event)
    {
        if (this.mc.thePlayer.getCurrentEquippedItem() != null && !EnumChatFormatting.getTextWithoutFormattingCodes(this.mc.thePlayer.getCurrentEquippedItem().getDisplayName()).equals("Jungle Axe"))
        {
            return;
        }

        long now = System.currentTimeMillis();
        Block block = event.getWorld().getBlockState(event.getPos()).getBlock();

        if (now - this.lastBlockBreak > ExtendedConfig.instance.jungleAxeDelay && block instanceof BlockLog)
        {
            this.lastBlockBreak = now;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreInfoRender(RenderGameOverlayEvent.Pre event)
    {
        GuiIngameForge.renderObjective = !this.mc.gameSettings.showDebugInfo;
        double jungleAxeDelay = this.getItemDelay(ExtendedConfig.instance.jungleAxeDelay, this.lastBlockBreak);
        double grapplingHookDelay = this.getItemDelay(ExtendedConfig.instance.grapplingHookDelay, this.lastGrapplingHookUse);

        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR || event.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS)
        {
            if (this.mc.currentScreen instanceof GuiRenderPreview)
            {
                event.setCanceled(true);
            }
        }
        if (event.type == RenderGameOverlayEvent.ElementType.TEXT)
        {
            if (this.mc.gameSettings.hideGUI || this.mc.gameSettings.showDebugInfo || this.mc.thePlayer == null && this.mc.theWorld == null || this.mc.currentScreen instanceof GuiRenderPreview)
            {
                return;
            }

            if (ExtendedConfig.instance.jungleAxeOverlay && jungleAxeDelay >= 0.01D)
            {
                this.renderCrosshairText(event, ColorUtils.stringToRGB(ExtendedConfig.instance.jungleAxeDelayColor).toColoredFont(), jungleAxeDelay);
            }
            if (ExtendedConfig.instance.grapplingHookOverlay && grapplingHookDelay >= 0.01D)
            {
                this.renderCrosshairText(event, ColorUtils.stringToRGB(ExtendedConfig.instance.grapplingHookDelayColor).toColoredFont(), grapplingHookDelay);
            }

            if (ConfigManagerIN.enableRenderInfo)
            {
                List<String> leftInfo = new LinkedList<>();
                List<String> rightInfo = new LinkedList<>();

                // left info
                if (ExtendedConfig.instance.ping && !this.mc.isSingleplayer())
                {
                    leftInfo.add(HUDInfo.getPing());

                    if (ExtendedConfig.instance.pingToSecond)
                    {
                        leftInfo.add(HUDInfo.getPingToSecond());
                    }
                }
                if (ExtendedConfig.instance.serverIP && !this.mc.isSingleplayer())
                {
                    if (this.mc.getCurrentServerData() != null)
                    {
                        leftInfo.add(HUDInfo.getServerIP(this.mc));
                    }
                }
                if (ExtendedConfig.instance.fps)
                {
                    leftInfo.add(HUDInfo.getFPS());
                }
                if (ExtendedConfig.instance.xyz)
                {
                    leftInfo.add(HUDInfo.getXYZ(this.mc));

                    if (this.mc.thePlayer.dimension == -1)
                    {
                        leftInfo.add(HUDInfo.getOverworldXYZFromNether(this.mc));
                    }
                }
                if (ExtendedConfig.instance.direction)
                {
                    leftInfo.add(HUDInfo.renderDirection(this.mc));
                }
                if (ExtendedConfig.instance.biome)
                {
                    leftInfo.add(HUDInfo.getBiome(this.mc));
                }

                // right info
                if (ExtendedConfig.instance.realTime)
                {
                    rightInfo.add(HUDInfo.getCurrentTime());
                }
                if (ExtendedConfig.instance.gameTime)
                {
                    rightInfo.add(HUDInfo.getCurrentGameTime(this.mc));
                }
                if (ExtendedConfig.instance.gameWeather && this.mc.theWorld.isRaining())
                {
                    rightInfo.add(HUDInfo.getGameWeather(this.mc));
                }
                if (ExtendedConfig.instance.moonPhase)
                {
                    rightInfo.add(InfoUtils.INSTANCE.getMoonPhase(this.mc));
                }

                // equipments
                if (!this.mc.thePlayer.isSpectator() && ExtendedConfig.instance.equipmentHUD)
                {
                    if (EnumEquipment.Position.getById(ExtendedConfig.instance.equipmentPosition).equalsIgnoreCase("hotbar"))
                    {
                        HUDInfo.renderHotbarEquippedItems(this.mc);
                    }
                    else
                    {
                        if (EnumEquipment.Direction.getById(ExtendedConfig.instance.equipmentDirection).equalsIgnoreCase("vertical"))
                        {
                            HUDInfo.renderVerticalEquippedItems(this.mc);
                        }
                        else
                        {
                            HUDInfo.renderHorizontalEquippedItems(this.mc);
                        }
                    }
                }

                if (ExtendedConfig.instance.potionHUD)
                {
                    HUDInfo.renderPotionHUD(this.mc);
                }

                // left info
                for (int i = 0; i < leftInfo.size(); ++i)
                {
                    ScaledResolution res = new ScaledResolution(this.mc);
                    String string = leftInfo.get(i);
                    float fontHeight = this.mc.fontRendererObj.FONT_HEIGHT + 1;
                    float yOffset = 3 + fontHeight * i;
                    float xOffset = res.getScaledWidth() - 2 - this.mc.fontRendererObj.getStringWidth(string);

                    if (!StringUtils.isNullOrEmpty(string))
                    {
                        this.mc.fontRendererObj.drawString(string, ExtendedConfig.instance.swapRenderInfo ? xOffset : 3.0625F, yOffset, 16777215, true);
                    }
                }

                // right info
                for (int i = 0; i < rightInfo.size(); ++i)
                {
                    ScaledResolution res = new ScaledResolution(this.mc);
                    String string = rightInfo.get(i);
                    float fontHeight = this.mc.fontRendererObj.FONT_HEIGHT + 1;
                    float yOffset = 3 + fontHeight * i;
                    float xOffset = res.getScaledWidth() - 2 - this.mc.fontRendererObj.getStringWidth(string);

                    if (!StringUtils.isNullOrEmpty(string))
                    {
                        this.mc.fontRendererObj.drawString(string, ExtendedConfig.instance.swapRenderInfo ? 3.0625F : xOffset, yOffset, 16777215, true);
                    }
                }
            }
        }
        if (event.type == RenderGameOverlayEvent.ElementType.CHAT)
        {
            if (this.mc.currentScreen instanceof GuiRenderPreview)
            {
                event.setCanceled(true);
                return;
            }
        }
        if (event.type == RenderGameOverlayEvent.ElementType.BOSSHEALTH)
        {
            event.setCanceled(true);

            if (ConfigManagerIN.enableRenderBossHealthStatus)
            {
                this.mc.getTextureManager().bindTexture(Gui.icons);
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.enableBlend();

                if (BossStatus.bossName != null && BossStatus.statusBarTime > 0)
                {
                    --BossStatus.statusBarTime;
                    ScaledResolution res = new ScaledResolution(this.mc);
                    int i = res.getScaledWidth();
                    int j = 182;
                    int k = i / 2 - j / 2;
                    int l = (int)(BossStatus.healthScale * (j + 1));
                    int i1 = 12;

                    if (ConfigManagerIN.enableRenderBossHealthBar)
                    {
                        this.mc.ingameGUI.drawTexturedModalRect(k, i1, 0, 74, j, 5);
                        this.mc.ingameGUI.drawTexturedModalRect(k, i1, 0, 74, j, 5);

                        if (l > 0)
                        {
                            this.mc.ingameGUI.drawTexturedModalRect(k, i1, 0, 79, l, 5);
                        }
                    }
                    String name = BossStatus.bossName;
                    this.mc.ingameGUI.getFontRenderer().drawStringWithShadow(name, i / 2 - this.mc.ingameGUI.getFontRenderer().getStringWidth(name) / 2, i1 - 10, 16777215);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    this.mc.getTextureManager().bindTexture(Gui.icons);
                }
                GlStateManager.disableBlend();
            }
        }
    }

    @SubscribeEvent
    public void onRenderHealthStatus(RenderLivingEvent.Specials.Post event)
    {
        EntityLivingBase entity = event.entity;
        float health = entity.getHealth();
        boolean halfHealth = health <= entity.getMaxHealth() / 2F;
        boolean halfHealth1 = health <= entity.getMaxHealth() / 4F;
        float range = entity.isSneaking() ? RendererLivingEntity.NAME_TAG_RANGE_SNEAK : RendererLivingEntity.NAME_TAG_RANGE;
        double distance = entity.getDistanceSqToEntity(this.mc.getRenderViewEntity());
        String mode = HealthStatusMode.getById(ExtendedConfig.instance.healthStatusMode);
        boolean flag = mode.equals("disable") ? false : mode.equals("pointed") ? entity == InfoUtils.INSTANCE.extendedPointedEntity : true;
        ChatStyle color = halfHealth ? JsonUtils.red() : halfHealth1 ? JsonUtils.darkRed() : JsonUtils.green();

        if (distance < range * range)
        {
            if (!this.mc.gameSettings.hideGUI && !entity.isInvisible() && flag && !(entity instanceof EntityPlayerSP || entity instanceof EntityArmorStand) && !InfoUtils.INSTANCE.isHypixel())
            {
                String heart = JsonUtils.create("\u2764 ").setChatStyle(color).getFormattedText();
                GlStateManager.alphaFunc(516, 0.1F);
                RenderUtils.renderEntityHealth(entity, heart + String.format("%.1f", health), event.x, event.y, event.z);
            }
        }
    }

    private double getItemDelay(int base, long delay)
    {
        long now = System.currentTimeMillis();
        DecimalFormat numberFormat = new DecimalFormat("0.0");
        double seconds = base / 1000.0D - (now - delay) / 1000.0D;

        if (seconds >= 0.01D)
        {
            return Double.valueOf(numberFormat.format(seconds));
        }
        return 0.0D;
    }

    private void renderCrosshairText(RenderGameOverlayEvent event, String color, double value)
    {
        float width = event.resolution.getScaledWidth() / 2 - 7 + 1.0625F;
        float height = event.resolution.getScaledHeight() / 2 + 6;
        this.mc.fontRendererObj.drawString(color + String.valueOf(value), width, height, 16777215, true);
    }
}
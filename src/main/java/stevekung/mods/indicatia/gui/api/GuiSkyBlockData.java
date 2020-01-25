package stevekung.mods.indicatia.gui.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.lwjgl.input.Mouse;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiScrollingList;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.core.IndicatiaMod;
import stevekung.mods.indicatia.event.ClientEventHandler;
import stevekung.mods.indicatia.integration.SkyblockAddonsGuiChest;
import stevekung.mods.indicatia.utils.*;

public class GuiSkyBlockData extends GuiScreen
{
    private static final ResourceLocation INVENTORY_TABS = new ResourceLocation("indicatia:textures/gui/tabs.png");
    private static final ResourceLocation XP_BARS = new ResourceLocation("indicatia:textures/gui/skill_xp_bar.png");
    private static final String[] REVENANT_HORROR_HEAD = new String[] {"0862e0b0-a14f-3f93-894f-013502936b59", "eyJ0aW1lc3RhbXAiOjE1Njg0NTc0MjAxMzcsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RiYWQ5OWVkM2M4MjBiNzk3ODE5MGFkMDhhOTM0YTY4ZGZhOTBkOTk4NjgyNWRhMWM5N2Y2ZjIxZjQ5YWQ2MjYifX19"};

    // Based stuff
    private boolean loadingApi = true;
    private boolean error = false;
    private String errorMessage;
    private String statusMessage;
    private boolean resize;
    private GuiButton doneButton;
    private GuiButton backButton;
    private final List<ProfileDataCallback> profiles;
    private final String sbProfileId;
    private final String sbProfileName;
    private final String username;
    private final String uuid;
    private final GameProfile profile;
    private final StopWatch watch = new StopWatch();
    private int percent;

    // API
    private static final Pattern STATS_PATTERN = Pattern.compile("(?<type>Strength|Crit Chance|Crit Damage|Health|Defense|Speed|Intelligence|True Defense): (?<value>(?:\\+|\\-)[0-9,]+)?(?:\\%){0,1}(?:(?: HP(?: \\(\\+[0-9,]+ HP\\)){0,1}(?: \\(\\w+ \\+[0-9,]+ HP\\)){0,1})|(?: \\(\\+[0-9,]+\\))|(?: \\(\\w+ \\+[0-9,]+(?:\\%){0,1}\\))){0,1}");
    private static final DecimalFormat FORMAT = new DecimalFormat("#,###,###,###,###");
    private static final DecimalFormat NUMBER_FORMAT_WITH_SYMBOL = new DecimalFormat("+#;-#");
    private static final DecimalFormat SKILL_AVG = new DecimalFormat("##.#");
    public static boolean renderSecondLayer;
    private GuiScrollingList currentSlot;
    private static final int MAX_FAIRY_SOULS = 190;
    private final List<SkyBlockInfo> infoList = new ArrayList<>();
    private final List<SkyBlockSkillInfo> skillLeftList = new ArrayList<>();
    private final List<SkyBlockSkillInfo> skillRightList = new ArrayList<>();
    private final List<SkyBlockSlayerInfo> slayerInfo = new ArrayList<>();
    private final List<SkyBlockStats> sbKillStats = new ArrayList<>();
    private final List<SkyBlockStats> sbDeathStats = new ArrayList<>();
    private final List<SkyBlockStats> sbOtherStats = new ArrayList<>();
    private final List<ItemStack> armorItems = new ArrayList<>();
    private final List<ItemStack> inventoryToStats = new ArrayList<>();
    private SkyBlockSkillInfo carpentrySkill;
    private int slayerTotalAmountSpent;
    private EntityOtherFakePlayer player;
    private String skillAvg;
    private boolean hasDayNightCrystal;
    private boolean hasFullSuperiorDragon;
    private boolean hasFullLapisArmor;
    private boolean hasFullMastiff;
    private boolean hasFullYoungDragon;
    private boolean hasFullSpeedster;

    // Info & Inventory
    private static final int SIZE = 36;
    private static final InventoryBasic TEMP_INVENTORY = new InventoryBasic("tmp", true, GuiSkyBlockData.SIZE);
    private static final InventoryBasic TEMP_ARMOR_INVENTORY = new InventoryBasic("tmp", true, 4);
    static final List<SkyBlockInventory> SKYBLOCK_INV = new ArrayList<>();
    private int selectedTabIndex = SkyBlockInventoryTabs.INVENTORY.getTabIndex();
    private float currentScroll;
    private boolean isScrolling;
    private boolean wasClicking;
    private final ContainerSkyBlock skyBlockContainer;
    private final ContainerArmor skyBlockArmorContainer;
    private final SkyblockAddonsGuiChest chest = new SkyblockAddonsGuiChest();

    // Player Bonus Stats
    private int totalFairySouls;
    private int farmingLevel;
    private int foragingLevel;
    private int miningLevel;
    private int fishingLevel;
    private int combatLevel;
    private int enchantingLevel;
    private int alchemyLevel;
    private int zombieSlayerLevel;
    private int spiderSlayerLevel;
    private int wolfSlayerLevel;
    private BonusStatTemplate allStat = new BonusStatTemplate(100, 0, 0, 0, 0, 100, 20, 50, 100);

    // GuiContainer fields
    private int xSize;
    private int ySize;
    private int guiLeft;
    private int guiTop;
    private Slot theSlot;

    public GuiSkyBlockData(List<ProfileDataCallback> profiles, ProfileDataCallback callback)
    {
        this.allowUserInput = true;
        this.skyBlockContainer = new ContainerSkyBlock();
        this.skyBlockArmorContainer = new ContainerArmor();
        this.profiles = profiles;
        this.sbProfileId = callback.getProfileId();
        this.sbProfileName = callback.getProfileName();
        this.username = callback.getUsername();
        this.uuid = callback.getUUID();
        this.profile = callback.getGameProfile();

        this.xSize = 202;
        this.ySize = 96;
    }

    @Override
    public void initGui()
    {
        this.buttonList.clear();

        if (!this.resize)
        {
            CommonUtils.runAsync(() ->
            {
                try
                {
                    this.watch.start();
                    this.getPlayerData();
                    this.watch.stop();
                    LoggerIN.info("API Download finished in: {}ms", this.watch.getTime());
                }
                catch (Throwable e)
                {
                    this.setErrorMessage(e.getMessage());
                    e.printStackTrace();
                }
            });
        }

        this.buttonList.add(this.doneButton = new GuiButton(0, this.width / 2 - 154, this.height - 27, 150, 20, LangUtils.translate("gui.close")));
        this.buttonList.add(this.backButton = new GuiButton(1, this.width / 2 + 4, this.height - 27, 150, 20, LangUtils.translate("gui.back")));
        GuiButton infoButton = new GuiButton(ViewButton.INFO.id, this.width / 2 - 185, 10, 80, 20, LangUtils.translate("gui.sb_view_info"));
        infoButton.enabled = false;
        this.buttonList.add(infoButton);
        this.buttonList.add(new GuiButton(ViewButton.SKILLS.id, this.width / 2 - 88, 10, 80, 20, LangUtils.translate("gui.sb_view_skills")));
        this.buttonList.add(new GuiButton(ViewButton.SLAYERS.id, this.width / 2 + 8, 10, 80, 20, LangUtils.translate("gui.sb_view_slayers")));
        this.buttonList.add(new GuiButton(ViewButton.OTHER_STATS.id, this.width / 2 + 104, 10, 80, 20, LangUtils.translate("gui.sb_view_other_stats")));

        GuiButton statKillsButton = new GuiButton(OtherStatsViewButton.KILLS.id, this.width / 2 - 124, this.height - 52, 80, 20, LangUtils.translate("gui.sb_other_stats.kills"));
        statKillsButton.enabled = false;
        this.buttonList.add(statKillsButton);
        this.buttonList.add(new GuiButton(OtherStatsViewButton.DEATHS.id, this.width / 2 - 40, this.height - 52, 80, 20, LangUtils.translate("gui.sb_other_stats.deaths")));
        this.buttonList.add(new GuiButton(OtherStatsViewButton.OTHER_STATS.id, this.width / 2 + 44, this.height - 52, 80, 20, LangUtils.translate("gui.sb_other_stats.others")));

        for (GuiButton viewButton : this.buttonList)
        {
            if (OtherStatsViewButton.getTypeForButton(viewButton) != null)
            {
                viewButton.visible = false;
            }
        }

        GuiButton basicInfoButton = new GuiButton(BasicInfoViewButton.INFO.id, this.width / 2 - 88, this.height - 52, 80, 20, LangUtils.translate("gui.sb_basic_info"));
        basicInfoButton.enabled = false;
        this.buttonList.add(basicInfoButton);
        this.buttonList.add(new GuiButton(BasicInfoViewButton.INVENTORY.id, this.width / 2 + 8, this.height - 52, 80, 20, LangUtils.translate("gui.sb_inventory")));

        for (GuiButton viewButton : this.buttonList)
        {
            if (BasicInfoViewButton.getTypeForButton(viewButton) != null)
            {
                viewButton.visible = true;
            }
        }

        int i = this.selectedTabIndex;
        this.selectedTabIndex = -1;
        this.setCurrentTab(SkyBlockInventoryTabs.tabArray[i]);

        this.guiLeft = (this.width - this.xSize) / 2 + 50;
        this.guiTop = (this.height - this.ySize) / 2 + 10;

        this.currentSlot = new InfoStats(this, this.width - 119, this.height, 44, this.height - 54, 59, 12, this.width, this.height, this.infoList);
        this.resize = false;
    }

    @Override
    public void updateScreen()
    {
        if (!this.watch.isStopped() && this.percent < 100)
        {
            this.percent = (int)(this.watch.getTime() * 100 / 2000);
        }
        if (this.percent > 100)
        {
            this.percent = 100;
        }
        if (this.player != null)
        {
            this.player.onUpdate();
        }
    }

    @Override
    public void onResize(Minecraft mc, int width, int height)
    {
        this.resize = true;

        if (!this.resize)
        {
            this.watch.reset();
        }
        super.onResize(mc, width, height);
    }

    @Override
    public void onGuiClosed()
    {
        TEMP_INVENTORY.clear();
        TEMP_ARMOR_INVENTORY.clear();
        SKYBLOCK_INV.clear();
        this.mc.getNetHandler().playerInfoMap.values().removeIf(network -> ((IViewerLoader)network).isLoadedFromViewer());
        GuiSkyBlockData.renderSecondLayer = false;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            this.actionPerformedViewInfo(button);
            this.actionPerformedOtherStats(button);
            this.actionPerformedBasicInfo(button);

            if (button.id == 0)
            {
                this.mc.displayGuiScreen(this.error ? new GuiSkyBlockAPIViewer(this.username, this.profiles) : null);
            }
            else if (button.id == 1)
            {
                this.mc.displayGuiScreen(new GuiSkyBlockAPIViewer(this.username, this.profiles));
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == 1)
        {
            this.mc.displayGuiScreen(null);

            if (this.mc.currentScreen == null)
            {
                this.mc.setIngameFocus();
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int state) throws IOException
    {
        if (this.loadingApi)
        {
            return;
        }
        else
        {
            if (state == 0)
            {
                int i = mouseX - this.guiLeft;
                int j = mouseY - this.guiTop;

                for (SkyBlockInventoryTabs tab : SkyBlockInventoryTabs.tabArray)
                {
                    if (this.isMouseOverTab(tab, i, j))
                    {
                        this.setCurrentTab(tab);
                        return;
                    }
                }
                super.mouseClicked(mouseX, mouseY, state);
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        if (this.loadingApi)
        {
            return;
        }
        else
        {
            super.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();

        if (this.loadingApi)
        {
            this.drawCenteredString(this.fontRendererObj, LangUtils.translate("Downloading SkyBlock stats"), this.width / 2, this.height / 2 - 20, 16777215);

            int i = this.width / 2 - 150;
            int j = this.width / 2 + 150;
            int k = this.height / 2 + 10;
            int l = k + 10;
            int j1 = MathHelper.floor_float(this.percent / 100.0F * (j - i));
            Gui.drawRect(i - 1, k - 1, j + 1, l + 1, -16777216);
            Gui.drawRect(i, k, i + j1, l, ColorUtils.to32BitColor(128, 85, 255, 85));
            this.drawCenteredString(this.fontRendererObj, this.percent + "%", this.width / 2, k + (l - k) / 2 - 9 / 2, 10526880);
            this.drawCenteredString(this.fontRendererObj, EnumChatFormatting.BLUE + "Status: " + EnumChatFormatting.RESET + this.statusMessage, this.width / 2, k + (l - k) / 2 - 9 / 2 + 20, 10526880);
        }
        else
        {
            if (this.error)
            {
                this.drawCenteredString(this.fontRendererObj, "SkyBlock API Viewer", this.width / 2, 20, 16777215);
                this.drawCenteredString(this.fontRendererObj, EnumChatFormatting.RED + this.errorMessage, this.width / 2, 100, 16777215);
                super.drawScreen(mouseX, mouseY, partialTicks);
            }
            else
            {
                if (this.currentSlot != null)
                {
                    this.currentSlot.drawScreen(mouseX, mouseY, partialTicks);
                }

                this.drawCenteredString(this.fontRendererObj, EnumChatFormatting.GOLD + this.username + "'s Profile: " + this.sbProfileName, this.width / 2, 34, 16777215);

                if (this.currentSlot instanceof EmptyStats)
                {
                    EmptyStats stat = (EmptyStats)this.currentSlot;

                    if (stat.getType() == EmptyStats.Type.INVENTORY)
                    {
                        boolean flag = Mouse.isButtonDown(0);
                        int i = this.guiLeft;
                        int j = this.guiTop;
                        int k = i + 182;
                        int l = j + 18;
                        int i1 = k + 14;
                        int j1 = l + 72;

                        if (!this.wasClicking && flag && mouseX >= k && mouseY >= l && mouseX < i1 && mouseY < j1)
                        {
                            this.isScrolling = this.needsScrollBars();
                        }

                        if (!flag)
                        {
                            this.isScrolling = false;
                        }

                        this.wasClicking = flag;

                        if (this.isScrolling)
                        {
                            this.currentScroll = (mouseY - l - 7.5F) / (j1 - l - 15.0F);
                            this.currentScroll = MathHelper.clamp_float(this.currentScroll, 0.0F, 1.0F);
                            this.skyBlockContainer.scrollTo(this.currentScroll);
                        }

                        this.drawTabsBackgroundLayer(partialTicks, mouseX, mouseY);
                        GlStateManager.disableRescaleNormal();
                        RenderHelper.disableStandardItemLighting();
                        GlStateManager.disableLighting();
                        GlStateManager.disableDepth();
                    }
                }

                super.drawScreen(mouseX, mouseY, partialTicks);

                if (this.currentSlot instanceof EmptyStats)
                {
                    EmptyStats stat = (EmptyStats)this.currentSlot;

                    if (stat.getType() == EmptyStats.Type.INVENTORY)
                    {
                        this.drawContainerSlot(mouseX, mouseY);

                        RenderHelper.disableStandardItemLighting();
                        this.drawTabsForegroundLayer();
                        RenderHelper.enableGUIStandardItemLighting();

                        for (SkyBlockInventoryTabs tab : SkyBlockInventoryTabs.tabArray)
                        {
                            if (tab == null)
                            {
                                continue;
                            }
                            if (this.renderTabsHoveringText(tab, mouseX, mouseY))
                            {
                                break;
                            }
                        }

                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        GlStateManager.disableLighting();

                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        GuiSkyBlockData.drawEntityOnScreen(this.width / 2 - 96, this.height / 2 + 40, 40, this.player);

                        if (this.theSlot != null && this.theSlot.getHasStack())
                        {
                            this.renderToolTip(this.theSlot.getStack(), mouseX, mouseY);
                        }
                        if (IndicatiaMod.isSkyblockAddonsLoaded)
                        {
                            this.chest.drawBackpacks(this, mouseX, mouseY, partialTicks);
                        }
                    }
                    else
                    {
                        int i = 0;

                        for (SkyBlockSkillInfo info : this.skillLeftList)
                        {
                            int x = this.width / 2 - 120;
                            int y = 53;
                            int barY = y + 20 + 32 * i;
                            int textY = y + 32 * i;
                            this.renderSkillBar(info.getName(), x, barY, x + 46, textY, info.getCurrentXp(), info.getXpRequired(), info.getCurrentLvl(), info.isReachLimit());
                            ++i;
                        }

                        i = 0;

                        for (SkyBlockSkillInfo info : this.skillRightList)
                        {
                            int x = this.width / 2 + 30;
                            int y = 53;
                            int barY = y + 20 + 32 * i;
                            int textY = y + 32 * i;
                            this.renderSkillBar(info.getName(), x, barY, x + 46, textY, info.getCurrentXp(), info.getXpRequired(), info.getCurrentLvl(), info.isReachLimit());
                            ++i;
                        }
                        int x = this.width / 2 - 46;
                        int y = 172;
                        this.renderSkillBar(this.carpentrySkill.getName(), x, y + 28, x + 46, y + 8, this.carpentrySkill.getCurrentXp(), this.carpentrySkill.getXpRequired(), this.carpentrySkill.getCurrentLvl(), this.carpentrySkill.isReachLimit());

                        if (this.skillAvg != null)
                        {
                            this.drawCenteredString(this.fontRendererObj, "Average Skill: " + this.skillAvg, this.width / 2 + 104, 199, 16777215);
                        }
                    }
                }
                else if (this.currentSlot instanceof SlayerStats)
                {
                    String total = EnumChatFormatting.GRAY + "Total Amount Spent: " + EnumChatFormatting.YELLOW + FORMAT.format(this.slayerTotalAmountSpent);
                    this.drawString(this.fontRendererObj, total, this.width - this.fontRendererObj.getStringWidth(total) - 60, this.height - 38, 16777215);
                }
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.enableDepth();
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();

        if (i != 0 && this.needsScrollBars())
        {
            int j = this.skyBlockContainer.itemList.size() / 9 - 4;

            if (i > 0)
            {
                i = 1;
            }
            if (i < 0)
            {
                i = -1;
            }
            this.currentScroll = (float)(this.currentScroll - (double)i / (double)j);
            this.currentScroll = MathHelper.clamp_float(this.currentScroll, 0.0F, 1.0F);
            this.skyBlockContainer.scrollTo(this.currentScroll);
        }
    }

    // Input
    private void actionPerformedViewInfo(GuiButton button)
    {
        ViewButton type = ViewButton.getTypeForButton(button);

        if (type != null)
        {
            for (GuiButton viewButton : this.buttonList)
            {
                if (ViewButton.getTypeForButton(viewButton) != null)
                {
                    viewButton.enabled = true;
                }
            }

            button.enabled = false;

            if (type.id == ViewButton.INFO.id)
            {
                this.currentSlot = new InfoStats(this, this.width - 119, this.height, 44, this.height - 54, 59, 12, this.width, this.height, this.infoList);
                this.hideOtherStatsButton();

                for (GuiButton viewButton : this.buttonList)
                {
                    if (BasicInfoViewButton.getTypeForButton(viewButton) != null)
                    {
                        viewButton.visible = true;
                        viewButton.enabled = viewButton.id != BasicInfoViewButton.INFO.id;
                    }
                }
            }
            else if (type.id == ViewButton.SKILLS.id)
            {
                this.currentSlot = new EmptyStats(this.mc, this.width - 119, this.height, 44, this.height - 30, 59, 12, this.width, this.height, EmptyStats.Type.SKILL);
                this.hideOtherStatsButton();
                this.hideBasicInfoButton();
            }
            else if (type.id == ViewButton.SLAYERS.id)
            {
                this.currentSlot = new SlayerStats(this, this.width - 119, this.height, 44, this.height - 42, 59, 16, this.width, this.height, this.slayerInfo);
                this.hideOtherStatsButton();
                this.hideBasicInfoButton();
            }
            else if (type.id == ViewButton.OTHER_STATS.id)
            {
                this.currentSlot = new OtherStats(this, this.width - 119, this.height, 44, this.height - 54, 59, 12, this.width, this.height, this.sbKillStats, SkyBlockStats.Type.KILLS);
                this.hideBasicInfoButton();

                for (GuiButton viewButton : this.buttonList)
                {
                    if (OtherStatsViewButton.getTypeForButton(viewButton) != null)
                    {
                        viewButton.visible = true;
                        viewButton.enabled = viewButton.id != OtherStatsViewButton.KILLS.id;
                    }
                }
            }
            else
            {
                this.currentSlot = null;
                this.hideOtherStatsButton();
                this.hideBasicInfoButton();
            }
        }
    }

    private void actionPerformedOtherStats(GuiButton button)
    {
        OtherStatsViewButton type = OtherStatsViewButton.getTypeForButton(button);

        if (type != null)
        {
            for (GuiButton viewButton : this.buttonList)
            {
                if (OtherStatsViewButton.getTypeForButton(viewButton) != null)
                {
                    viewButton.enabled = true;
                }
            }

            SkyBlockStats.Type statType = SkyBlockStats.Type.KILLS;
            List<SkyBlockStats> list = this.sbKillStats;

            if (type.id == OtherStatsViewButton.KILLS.id)
            {
                statType = SkyBlockStats.Type.KILLS;
                list = this.sbKillStats;
            }
            else if (type.id == OtherStatsViewButton.DEATHS.id)
            {
                statType = SkyBlockStats.Type.DEATHS;
                list = this.sbDeathStats;
            }
            else
            {
                statType = SkyBlockStats.Type.OTHERS;
                list = this.sbOtherStats;
            }
            this.currentSlot = new OtherStats(this, this.width - 119, this.height, 44, this.height - 54, 59, 12, this.width, this.height, list, statType);
            button.enabled = false;
        }
    }

    private void actionPerformedBasicInfo(GuiButton button)
    {
        BasicInfoViewButton type = BasicInfoViewButton.getTypeForButton(button);

        if (type != null)
        {
            for (GuiButton viewButton : this.buttonList)
            {
                if (BasicInfoViewButton.getTypeForButton(viewButton) != null)
                {
                    viewButton.enabled = true;
                }
            }

            if (type.id == BasicInfoViewButton.INFO.id)
            {
                this.currentSlot = new InfoStats(this, this.width - 119, this.height, 44, this.height - 54, 59, 12, this.width, this.height, this.infoList);
            }
            else
            {
                this.currentSlot = new EmptyStats(this.mc, this.width - 119, this.height, 44, this.height - 54, 59, 12, this.width, this.height, EmptyStats.Type.INVENTORY);
                this.setCurrentTab(SkyBlockInventoryTabs.INVENTORY);
            }
            button.enabled = false;
        }
    }

    private void setErrorMessage(String message)
    {
        this.error = true;
        this.loadingApi = false;
        this.errorMessage = message;
        this.backButton.visible = false;
        this.doneButton.xPosition = this.width / 2 - 75;
        this.doneButton.yPosition = this.height / 4 + 132;

        for (GuiButton button : this.buttonList)
        {
            if (button != this.doneButton)
            {
                button.visible = false;
            }
        }
    }

    private boolean isMouseOverTab(SkyBlockInventoryTabs tab, int mouseX, int mouseY)
    {
        int i = tab.getTabColumn();
        int j = 28 * i;
        int k = 0;

        if (i > 0)
        {
            j += i;
        }
        if (tab.isTabInFirstRow())
        {
            k = k - 26;
        }
        else
        {
            k = k + this.ySize;
        }
        boolean test = mouseX >= j + 1 && mouseX <= j + 27 && mouseY >= k && mouseY <= k + 26;
        return test;
    }

    private boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY)
    {
        return this.isPointInRegion(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mouseX, mouseY);
    }

    private boolean isPointInRegion(int left, int top, int right, int bottom, int mouseX, int mouseY)
    {
        int i = this.guiLeft;
        int j = this.guiTop;
        mouseX = mouseX - i;
        mouseY = mouseY - j;
        return mouseX >= left - 1 && mouseX < left + right + 1 && mouseY >= top - 1 && mouseY < top + bottom + 1;
    }

    private void setCurrentTab(SkyBlockInventoryTabs tab)
    {
        if (tab == null)
        {
            return;
        }
        this.selectedTabIndex = tab.getTabIndex();
        ContainerSkyBlock container = this.skyBlockContainer;
        container.itemList.clear();
        tab.displayAllItems(container.itemList);
        this.currentScroll = 0.0F;
        container.scrollTo(0.0F);
    }

    private boolean needsScrollBars()
    {
        if (SkyBlockInventoryTabs.tabArray[this.selectedTabIndex] == null)
        {
            return false;
        }
        return SkyBlockInventoryTabs.tabArray[this.selectedTabIndex].hasScrollBar() && this.skyBlockContainer.canScroll();
    }

    private void hideOtherStatsButton()
    {
        for (GuiButton viewButton : this.buttonList)
        {
            if (OtherStatsViewButton.getTypeForButton(viewButton) != null)
            {
                viewButton.visible = false;
            }
        }
    }

    private void hideBasicInfoButton()
    {
        for (GuiButton viewButton : this.buttonList)
        {
            if (BasicInfoViewButton.getTypeForButton(viewButton) != null)
            {
                viewButton.visible = false;
            }
        }
    }

    // Render
    private void renderSkillBar(String name, int xBar, int yBar, int xText, int yText, int playerXp, int xpRequired, int currentLvl, boolean reachLimit)
    {
        this.mc.getTextureManager().bindTexture(XP_BARS);
        GlStateManager.color(0.5F, 1.0F, 0.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(xBar, yBar, 0, 0, 91, 5, 91, 10);

        if (xpRequired > 0)
        {
            int filled = Math.min((int)Math.floor(playerXp * 92 / xpRequired), 91);

            if (filled > 0)
            {
                Gui.drawModalRectWithCustomSizedTexture(xBar, yBar, 0, 5, filled, 5, 91, 10);
            }

            this.drawCenteredString(this.fontRendererObj, EnumChatFormatting.GRAY + name + EnumChatFormatting.YELLOW + " " + currentLvl, xText, yText, 16777215);

            if (reachLimit)
            {
                this.drawCenteredString(this.fontRendererObj, NumberUtils.format(playerXp), xText, yText + 10, 16777215);
            }
            else
            {
                this.drawCenteredString(this.fontRendererObj, NumberUtils.format(playerXp) + "/" + NumberUtils.format(xpRequired), xText, yText + 10, 16777215);
            }
        }
        else
        {
            this.drawCenteredString(this.fontRendererObj, name, xText, yText + 8, 16777215);
        }
    }

    private void drawContainerSlot(int mouseX, int mouseY)
    {
        int i = this.guiLeft;
        int j = this.guiTop;
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(i, j, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        this.theSlot = null;
        int k = 240;
        int l = 240;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, k / 1.0F, l / 1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        for (int i1 = 0; i1 < this.skyBlockContainer.inventorySlots.size(); ++i1)
        {
            Slot slot = this.skyBlockContainer.inventorySlots.get(i1);
            this.drawSlot(slot);

            if (this.isMouseOverSlot(slot, mouseX, mouseY) && slot.canBeHovered())
            {
                this.theSlot = slot;
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                int j1 = slot.xDisplayPosition;
                int k1 = slot.yDisplayPosition;
                GlStateManager.colorMask(true, true, true, false);
                this.drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
                GlStateManager.colorMask(true, true, true, true);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }
        }

        for (int i1 = 0; i1 < this.skyBlockArmorContainer.inventorySlots.size(); ++i1)
        {
            Slot slot = this.skyBlockArmorContainer.inventorySlots.get(i1);

            if (this.isMouseOverSlot(slot, mouseX, mouseY) && slot.canBeHovered())
            {
                this.theSlot = slot;
            }
        }
        GlStateManager.popMatrix();
    }

    private void drawSlot(Slot slot)
    {
        int i = slot.xDisplayPosition;
        int j = slot.yDisplayPosition;
        ItemStack itemStack = slot.getStack();
        String s = null;
        this.zLevel = 100.0F;
        this.itemRender.zLevel = 100.0F;

        if (itemStack == null)
        {
            TextureAtlasSprite sprite = slot.getBackgroundSprite();

            if (sprite != null)
            {
                GlStateManager.disableLighting();
                this.mc.getTextureManager().bindTexture(slot.getBackgroundLocation());
                this.drawTexturedModalRect(i, j, sprite, 16, 16);
                GlStateManager.enableLighting();
            }
        }

        GlStateManager.enableDepth();

        if (ExtendedConfig.instance.showItemRarity)
        {
            RenderUtils.drawRarity(slot, false);
        }

        this.itemRender.renderItemAndEffectIntoGUI(itemStack, i, j);
        this.itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, itemStack, i, j, s);
        this.itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
    }

    private boolean renderTabsHoveringText(SkyBlockInventoryTabs tab, int mouseX, int mouseY)
    {
        int i = tab.getTabColumn();
        int j = 28 * i;
        int k = 0;

        if (i > 0)
        {
            j += i;
        }
        if (tab.isTabInFirstRow())
        {
            k = k - 28;
        }
        else
        {
            k = k + this.ySize - 2;
        }

        if (this.isPointInRegion(j + 2, k + 3, 25, 25, mouseX, mouseY))
        {
            this.drawHoveringText(Collections.singletonList(tab.getTranslatedTabLabel()), mouseX, mouseY);
            return true;
        }
        else
        {
            return false;
        }
    }

    private void drawTabsBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.enableGUIStandardItemLighting();
        SkyBlockInventoryTabs tab = SkyBlockInventoryTabs.tabArray[this.selectedTabIndex];

        for (SkyBlockInventoryTabs tab1 : SkyBlockInventoryTabs.tabArray)
        {
            this.mc.getTextureManager().bindTexture(INVENTORY_TABS);

            if (tab1 == null)
            {
                continue;
            }
            if (tab1.getTabIndex() != this.selectedTabIndex)
            {
                this.drawTab(tab1);
            }
        }

        this.mc.getTextureManager().bindTexture(new ResourceLocation("indicatia:textures/gui/tab_" + tab.getBackgroundTexture()));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.guiLeft + 182;
        int j = this.guiTop + 18;
        int k = j + 72;
        this.mc.getTextureManager().bindTexture(INVENTORY_TABS);

        if (tab.hasScrollBar())
        {
            this.drawTexturedModalRect(i, j + (int)((k - j - 17) * this.currentScroll), 232 + (this.needsScrollBars() ? 0 : 12), 0, 12, 15);
        }
        this.drawTab(tab);
    }

    private void drawTabsForegroundLayer()
    {
        SkyBlockInventoryTabs tab = SkyBlockInventoryTabs.tabArray[this.selectedTabIndex];

        if (tab != null)
        {
            GlStateManager.disableBlend();
            this.fontRendererObj.drawString(tab.getTranslatedTabLabel(), this.guiLeft + 12, this.guiTop + 6, 4210752);
        }
    }

    private void drawTab(SkyBlockInventoryTabs tab)
    {
        boolean flag = tab.getTabIndex() == this.selectedTabIndex;
        boolean flag1 = tab.isTabInFirstRow();
        int i = tab.getTabColumn();
        int j = i * 28;
        int k = 0;
        int l = this.guiLeft + 28 * i;
        int i1 = this.guiTop;
        int j1 = 32;

        if (flag)
        {
            k += 32;
        }
        if (i > 0)
        {
            l += i;
        }

        if (flag1)
        {
            i1 = i1 - 28;
        }
        else
        {
            k += 64;
            i1 = i1 + this.ySize - 4;
        }

        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
        this.drawTexturedModalRect(l, i1, j, k, 28, j1);
        this.zLevel = 100.0F;
        this.itemRender.zLevel = 100.0F;
        l = l + 6;
        i1 = i1 + 8 + (flag1 ? 1 : -1);
        GlStateManager.enableLighting();
        GlStateManager.enableRescaleNormal();
        ItemStack itemStack = tab.getIcon();
        this.itemRender.renderItemAndEffectIntoGUI(itemStack, l, i1);
        this.itemRender.renderItemOverlays(this.fontRendererObj, itemStack, l, i1);
        GlStateManager.disableLighting();
        this.itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
    }

    // Player Data
    private void getPlayerData() throws IOException
    {
        this.statusMessage = "Getting Player Data";

        URL url = new URL(SkyBlockAPIUtils.SKYBLOCK_PROFILE + this.sbProfileId);
        JsonObject obj = new JsonParser().parse(IOUtils.toString(url.openConnection().getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
        JsonElement profile = obj.get("profile");

        if (profile == null)
        {
            this.setErrorMessage("No API data returned, please try again later!");
            return;
        }

        JsonObject profiles = profile.getAsJsonObject().get("members").getAsJsonObject();
        JsonElement banking = profile.getAsJsonObject().get("banking");

        for (Map.Entry<String, JsonElement> entry : profiles.entrySet())
        {
            String userUUID = entry.getKey();

            if (userUUID.equals(this.uuid))
            {
                JsonObject currentUserProfile = profiles.get(userUUID).getAsJsonObject();
                this.getSkills(currentUserProfile);
                this.getStats(currentUserProfile);
                this.getSlayerInfo(currentUserProfile);
                this.getInventories(currentUserProfile);
                this.createFakePlayer();
                this.calculatePlayerStats(currentUserProfile);
                this.getItemStats(this.inventoryToStats, false);
                this.getItemStats(this.armorItems, true);
                this.applyBonuses();
                this.allStat.add(new BonusStatTemplate(0, 0, 0, this.allStat.getDefense() <= 0 ? this.allStat.getHealth() : Math.round(this.allStat.getHealth() * (1 + this.allStat.getDefense() / 100)), 0, 0, 0, 0, 0));
                this.getBasicInfo(currentUserProfile, banking);
                break;
            }
        }
        this.loadingApi = false;
    }

    private void applyBonuses()
    {
        if (this.hasDayNightCrystal)
        {
            this.allStat.addDefense(5);
            this.allStat.addStrength(5);
        }

        if (this.hasFullLapisArmor)
        {
            this.allStat.addHealth(60);
        }
        else if (this.hasFullMastiff)
        {
            this.allStat.addHealth(50 * this.allStat.getCritDamage());
        }
        else if (this.hasFullYoungDragon)
        {
            this.allStat.addSpeed(70);
        }
        else if (this.hasFullSpeedster)
        {
            this.allStat.addSpeed(20);
        }
        else if (this.hasFullSuperiorDragon)
        {
            this.allStat.setHealth((int)Math.round(this.allStat.getHealth() * 1.05D));
            this.allStat.setDefense((int)Math.round(this.allStat.getDefense() * 1.05D));
            this.allStat.setStrength((int)Math.round(this.allStat.getStrength() * 1.05D));
            this.allStat.setSpeed((int)Math.round(this.allStat.getSpeed() * 1.05D));
            this.allStat.setCritChance((int)Math.round(this.allStat.getCritChance() * 1.05D));
            this.allStat.setCritDamage((int)Math.round(this.allStat.getCritDamage() * 1.05D));
            this.allStat.setIntelligence((int)Math.round(this.allStat.getIntelligence() * 1.05D));
        }
    }

    private void calculatePlayerStats(JsonObject currentProfile)
    {
        JsonElement fairySouls = currentProfile.get("fairy_souls_collected");

        if (fairySouls != null)
        {
            this.totalFairySouls = fairySouls.getAsInt();
        }

        this.allStat.add(this.getFairySouls(this.totalFairySouls));
        this.allStat.add(this.calculateSkillBonus(PlayerStatsBonus.FARMING, this.farmingLevel));
        this.allStat.add(this.calculateSkillBonus(PlayerStatsBonus.FORAGING, this.foragingLevel));
        this.allStat.add(this.calculateSkillBonus(PlayerStatsBonus.MINING, this.miningLevel));
        this.allStat.add(this.calculateSkillBonus(PlayerStatsBonus.FISHING, this.fishingLevel));
        this.allStat.add(this.calculateSkillBonus(PlayerStatsBonus.COMBAT, this.combatLevel));
        this.allStat.add(this.calculateSkillBonus(PlayerStatsBonus.ENCHANTING, this.enchantingLevel));
        this.allStat.add(this.calculateSkillBonus(PlayerStatsBonus.ALCHEMY, this.alchemyLevel));
        this.allStat.add(this.calculateSkillBonus(PlayerStatsBonus.ZOMBIE_SLAYER, this.zombieSlayerLevel));
        this.allStat.add(this.calculateSkillBonus(PlayerStatsBonus.SPIDER_SLAYER, this.spiderSlayerLevel));
        this.allStat.add(this.calculateSkillBonus(PlayerStatsBonus.WOLF_SLAYER, this.wolfSlayerLevel));
    }

    private BonusStatTemplate calculateSkillBonus(PlayerStatsBonus.IBonusTemplate[] bonus, int skillLevel)
    {
        int healthTemp = 0;
        int defenseTemp = 0;
        int trueDefenseTemp = 0;
        int strengthTemp = 0;
        int speedTemp = 0;
        int critChanceTemp = 0;
        int critDamageTemp = 0;
        int intelligenceTemp = 0;

        for (int i = 0; i < bonus.length; ++i)
        {
            int levelToCheck = bonus[i].getLevel();
            int nextIndex = 0;
            boolean limit = true;

            if (nextIndex <= i)
            {
                nextIndex = i + 1; // check level at next index of json
            }

            if (nextIndex >= bonus.length)
            {
                nextIndex = bonus.length - 1;
                limit = false;
            }

            int levelToCheck2 = bonus[nextIndex].getLevel();

            if (levelToCheck <= skillLevel)
            {
                int health = bonus[i].getHealth();
                int defense = bonus[i].getDefense();
                int trueDefense = bonus[i].getTrueDefense();
                int strength = bonus[i].getStrength();
                int speed = bonus[i].getSpeed();
                int critChance = bonus[i].getCritChance();
                int critDamage = bonus[i].getCritDamage();
                int intelligence = bonus[i].getIntelligence();

                for (int level = levelToCheck; level <= skillLevel; level++)
                {
                    if (level >= levelToCheck2 && limit)
                    {
                        break;
                    }
                    healthTemp += health;
                    defenseTemp += defense;
                    trueDefenseTemp += trueDefense;
                    strengthTemp += strength;
                    speedTemp += speed;
                    critChanceTemp += critChance;
                    critDamageTemp += critDamage;
                    intelligenceTemp += intelligence;
                }
            }
        }
        return new BonusStatTemplate(healthTemp, defenseTemp, trueDefenseTemp, 0, strengthTemp, speedTemp, critChanceTemp, critDamageTemp, intelligenceTemp);
    }

    private void getHealthFromCake(NBTTagCompound extraAttrib)
    {
        List<ItemStack> itemStack1 = new ArrayList<>();

        try
        {
            NBTTagCompound compound1 = CompressedStreamTools.readCompressed(new ByteArrayInputStream(extraAttrib.getByteArray("new_year_cake_bag_data")));
            NBTTagList list = compound1.getTagList("i", 10);
            List<Integer> cakeYears = new ArrayList<>();

            for (int i = 0; i < list.tagCount(); ++i)
            {
                itemStack1.add(ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i)));
            }

            for (ItemStack cake : itemStack1)
            {
                if (cake != null && cake.hasTagCompound())
                {
                    int year = cake.getTagCompound().getCompoundTag("ExtraAttributes").getInteger("new_years_cake");

                    if (!cakeYears.contains(year))
                    {
                        cakeYears.add(year);
                    }
                }
            }
            this.allStat.addHealth(cakeYears.size());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void getItemStats(List<ItemStack> inventory, boolean armor)
    {
        int healthTemp = 0;
        int defenseTemp = 0;
        int trueDefenseTemp = 0;
        int strengthTemp = 0;
        int speedTemp = 0;
        int critChanceTemp = 0;
        int critDamageTemp = 0;
        int intelligenceTemp = 0;

        for (ItemStack itemStack : inventory)
        {
            if (itemStack != null && itemStack.hasTagCompound())
            {
                NBTTagCompound compound = itemStack.getTagCompound().getCompoundTag("display");
                NBTTagCompound extraAttrib = itemStack.getTagCompound().getCompoundTag("ExtraAttributes");
                String itemId = extraAttrib.getString("id");

                if (itemId.equals("FROZEN_CHICKEN"))
                {
                    this.allStat.addSpeed(1);
                }
                else if (itemId.equals("SPEED_TALISMAN"))
                {
                    this.allStat.addSpeed(3);
                }
                else if (itemId.equals("NEW_YEAR_CAKE_BAG"))
                {
                    this.getHealthFromCake(extraAttrib);
                }

                if (compound.getTagId("Lore") == 9)
                {
                    NBTTagList list = compound.getTagList("Lore", 8);

                    if (list.tagCount() > 0)
                    {
                        for (int j1 = 0; j1 < list.tagCount(); ++j1)
                        {
                            String lore = EnumChatFormatting.getTextWithoutFormattingCodes(list.getStringTagAt(j1));
                            String lastLore = EnumChatFormatting.getTextWithoutFormattingCodes(list.getStringTagAt(list.tagCount() - 1));
                            Matcher matcher = STATS_PATTERN.matcher(lore);

                            if (!armor && (lastLore.endsWith(" BOOTS") || lastLore.endsWith(" LEGGINGS") || lastLore.endsWith(" CHESTPLATE") || lastLore.endsWith(" HELMET") || !lastLore.endsWith(" ACCESSORY")))
                            {
                                continue;
                            }

                            if (matcher.matches())
                            {
                                String type = matcher.group("type");
                                String value = matcher.group("value").replace(",", "");
                                int valueInt = 0;

                                try
                                {
                                    valueInt = NUMBER_FORMAT_WITH_SYMBOL.parse(value).intValue();
                                }
                                catch (Exception e) {}

                                switch (type)
                                {
                                case "Health":
                                    healthTemp += valueInt;
                                    break;
                                case "Defense":
                                    defenseTemp += valueInt;
                                    break;
                                case "True Defense":
                                    trueDefenseTemp += valueInt;
                                    break;
                                case "Strength":
                                    strengthTemp += valueInt;
                                    break;
                                case "Speed":
                                    speedTemp += valueInt;
                                    break;
                                case "Crit Chance":
                                    critChanceTemp += valueInt;
                                    break;
                                case "Crit Damage":
                                    critDamageTemp += valueInt;
                                    break;
                                case "Intelligence":
                                    intelligenceTemp += valueInt;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        this.allStat.add(new BonusStatTemplate(healthTemp, defenseTemp, trueDefenseTemp, 0, strengthTemp, speedTemp, critChanceTemp, critDamageTemp, intelligenceTemp));
    }

    private void getBasicInfo(JsonObject currentProfile, JsonElement banking)
    {
        JsonElement deathCount = currentProfile.get("death_count");
        JsonElement purse = currentProfile.get("coin_purse");
        JsonElement lastSave = currentProfile.get("last_save");
        JsonElement firstJoin = currentProfile.get("first_join");
        int deathCounts = 0;
        float coins = 0.0F;
        long lastSaveMillis = -1;
        long firstJoinMillis = -1;

        if (deathCount != null)
        {
            deathCounts = deathCount.getAsInt();
        }
        if (purse != null)
        {
            coins = purse.getAsFloat();
        }
        if (lastSave != null)
        {
            lastSaveMillis = lastSave.getAsLong();
        }
        if (firstJoin != null)
        {
            firstJoinMillis = firstJoin.getAsLong();
        }

        String heath = ColorUtils.stringToRGB("239,83,80").toColoredFont();
        String defense = ColorUtils.stringToRGB("156,204,101").toColoredFont();
        String trueDefense = ColorUtils.stringToRGB("255,255,255").toColoredFont();
        String strength = ColorUtils.stringToRGB("181,33,30").toColoredFont();
        String speed = ColorUtils.stringToRGB("255,255,255").toColoredFont();
        String critChance = ColorUtils.stringToRGB("121,134,203").toColoredFont();
        String critDamage = ColorUtils.stringToRGB("70,90,201").toColoredFont();
        String intelligence = ColorUtils.stringToRGB("129,212,250").toColoredFont();
        String fairySoulsColor = ColorUtils.stringToRGB("203,54,202").toColoredFont();

        this.infoList.add(new SkyBlockInfo(heath + "\u2764 Health", heath + this.allStat.getHealth()));
        this.infoList.add(new SkyBlockInfo(heath + "\u2665 Effective Health", heath + this.allStat.getEffectiveHealth()));
        this.infoList.add(new SkyBlockInfo(defense + "\u2748 Defense", defense + this.allStat.getDefense()));
        this.infoList.add(new SkyBlockInfo(trueDefense + "\u2742 True Defense", trueDefense + this.allStat.getTrueDefense()));
        this.infoList.add(new SkyBlockInfo(strength + "\u2741 Strength", strength + this.allStat.getStrength()));
        this.infoList.add(new SkyBlockInfo(speed + "\u2726 Speed", speed + this.allStat.getSpeed()));
        this.infoList.add(new SkyBlockInfo(critChance + "\u2623 Crit Chance", critChance + this.allStat.getCritChance()));
        this.infoList.add(new SkyBlockInfo(critDamage + "\u2620 Crit Damage", critDamage + this.allStat.getCritDamage()));
        this.infoList.add(new SkyBlockInfo(intelligence + "\u270E Intelligence", intelligence + this.allStat.getIntelligence()));

        this.infoList.add(new SkyBlockInfo("", ""));

        this.infoList.add(new SkyBlockInfo(fairySoulsColor + "Fairy Souls Collected", fairySoulsColor + this.totalFairySouls + "/" + GuiSkyBlockData.MAX_FAIRY_SOULS));

        Date firstJoinDate = new Date(firstJoinMillis);
        Date lastSaveDate = new Date(lastSaveMillis);
        String lastLogout = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(lastSaveDate);
        String firstJoinDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(firstJoinDate);

        this.infoList.add(new SkyBlockInfo("Joined", firstJoinMillis != -1 ? CommonUtils.getRelativeTime(firstJoinDate.getTime()) : EnumChatFormatting.RED + "No first join data!"));
        this.infoList.add(new SkyBlockInfo("Joined (Date)", firstJoinMillis != -1 ? firstJoinDateFormat : EnumChatFormatting.RED + "No first join data!"));
        this.infoList.add(new SkyBlockInfo("Last Updated", lastSaveMillis != -1 ? CommonUtils.getRelativeTime(lastSaveDate.getTime()) : EnumChatFormatting.RED + "No last save data!"));
        this.infoList.add(new SkyBlockInfo("Last Updated (Date)", lastSaveMillis != -1 ? lastLogout : EnumChatFormatting.RED + "No last save data!"));

        this.infoList.add(new SkyBlockInfo("Death Count", String.valueOf(deathCounts)));

        if (banking != null)
        {
            double balance = banking.getAsJsonObject().get("balance").getAsDouble();
            this.infoList.add(new SkyBlockInfo("Banking Account", FORMAT.format(balance)));
        }
        else
        {
            this.infoList.add(new SkyBlockInfo("Banking Account", EnumChatFormatting.RED + "API is not enabled!"));
        }
        this.infoList.add(new SkyBlockInfo("Purse", FORMAT.format(coins)));
    }

    private BonusStatTemplate getFairySouls(int fairySouls)
    {
        int healthBase = 0;
        int defenseBase = 0;
        int strengthBase = 0;
        int speedBase = 0;

        for (PlayerStatsBonus.FairySouls progress : PlayerStatsBonus.FAIRY_SOULS)
        {
            int soulToCheck = progress.getCount();
            int health = progress.getHealth();
            int defense = progress.getDefense();
            int strength = progress.getStrength();
            int speed = progress.getSpeed();

            if (soulToCheck <= fairySouls)
            {
                healthBase += health;
                defenseBase += defense;
                strengthBase += strength;
                speedBase += speed;
            }
        }
        return new BonusStatTemplate(healthBase, defenseBase, 0, 0, strengthBase, speedBase, 0, 0, 0);
    }

    private String replaceStatsString(String statName, String replace)
    {
        String original = statName.replace(replace + "_", "").replace("_", " ");
        return original.equals(replace) ? "Total " + replace : WordUtils.capitalize(original) + " " + replace;
    }

    private void getSkills(JsonObject currentProfile)
    {
        this.skillLeftList.add(this.checkSkill(currentProfile.get("experience_skill_farming"), SkillType.FARMING));
        this.skillLeftList.add(this.checkSkill(currentProfile.get("experience_skill_foraging"), SkillType.FORAGING));
        this.skillLeftList.add(this.checkSkill(currentProfile.get("experience_skill_mining"), SkillType.MINING));
        this.skillLeftList.add(this.checkSkill(currentProfile.get("experience_skill_fishing"), SkillType.FISHING));

        this.skillRightList.add(this.checkSkill(currentProfile.get("experience_skill_combat"), SkillType.COMBAT));
        this.skillRightList.add(this.checkSkill(currentProfile.get("experience_skill_enchanting"), SkillType.ENCHANTING));
        this.skillRightList.add(this.checkSkill(currentProfile.get("experience_skill_alchemy"), SkillType.ALCHEMY));
        this.skillRightList.add(this.checkSkill(currentProfile.get("experience_skill_runecrafting"), SkillType.RUNECRAFTING, SkillProgress.RUNE_SKILL));

        this.carpentrySkill = this.checkSkill(currentProfile.get("experience_skill_carpentry"), SkillType.CARPENTRY);

        float avg = 0;
        int count = 0;
        List<SkyBlockSkillInfo> skills = new ArrayList<>();
        skills.addAll(this.skillLeftList);
        skills.addAll(this.skillRightList);
        skills.add(this.carpentrySkill);

        for (SkyBlockSkillInfo skill : skills)
        {
            avg += skill.getCurrentLvl() + skill.getSkillProgress();
            ++count;
        }
        if (avg > 0)
        {
            this.skillAvg = SKILL_AVG.format(avg / count);
        }
    }

    private SkyBlockSkillInfo checkSkill(JsonElement element, SkillType type)
    {
        return this.checkSkill(element, type, SkillProgress.SKILL);
    }

    private SkyBlockSkillInfo checkSkill(JsonElement element, SkillType type, SkillProgress[] progress)
    {
        if (element != null)
        {
            int playerXp = (int)element.getAsFloat();
            int xpRequired = 0;
            int currentLvl = 0;
            int levelToCheck = 0;
            int xpTotal = 0;
            float xpToNextLvl = 0;
            int currentXp = 0;
            float skillProgress = 0;

            for (int x = 0; x < progress.length; ++x)
            {
                if (playerXp >= xpTotal)
                {
                    xpTotal += progress[x].getXp();
                    currentLvl = x;
                    levelToCheck = progress[x].getLevel();

                    if (levelToCheck <= progress.length)
                    {
                        xpRequired = (int)progress[x].getXp();
                    }
                }
            }

            if (levelToCheck < progress.length)
            {
                xpToNextLvl = xpTotal - playerXp;
                currentXp = (int)(xpRequired - xpToNextLvl);
            }
            else
            {
                currentLvl = progress.length;
                currentXp = playerXp - xpTotal;
            }

            if (progress == SkillProgress.RUNE_SKILL && currentXp <= 0) // fix for runecrafting 23 to 24
            {
                if (levelToCheck <= progress.length)
                {
                    xpToNextLvl = xpTotal - playerXp;
                    currentXp = (int)(xpRequired - xpToNextLvl);
                    currentLvl = progress.length - 1;
                }
            }
            if (type != SkillType.RUNECRAFTING && type != SkillType.CARPENTRY)
            {
                skillProgress = Math.max(0, Math.min(currentXp / xpToNextLvl, 1));
            }
            this.setSkillLevel(type, currentLvl);
            return new SkyBlockSkillInfo(type.getName(), currentXp, xpRequired, currentLvl, skillProgress, xpToNextLvl <= 0);
        }
        else
        {
            return new SkyBlockSkillInfo(EnumChatFormatting.RED + "API is not enabled!", 0, 0, 0, 0, false);
        }
    }

    private void setSkillLevel(SkillType type, int currentLevel)
    {
        switch (type)
        {
        case FARMING:
            this.farmingLevel = currentLevel;
            break;
        case FORAGING:
            this.foragingLevel = currentLevel;
            break;
        case MINING:
            this.miningLevel = currentLevel;
            break;
        case FISHING:
            this.fishingLevel = currentLevel;
            break;
        case COMBAT:
            this.combatLevel = currentLevel;
            break;
        case ENCHANTING:
            this.enchantingLevel = currentLevel;
            break;
        case ALCHEMY:
            this.alchemyLevel = currentLevel;
            break;
        default:
            break;
        }
    }

    private void getStats(JsonObject currentProfile)
    {
        JsonObject stats = currentProfile.get("stats").getAsJsonObject();

        for (Map.Entry<String, JsonElement> stat : stats.entrySet())
        {
            String statName = stat.getKey();
            float value = stat.getValue().getAsFloat();

            if (statName.startsWith("kills"))
            {
                this.sbKillStats.add(new SkyBlockStats(this.replaceStatsString(statName, "kills"), value));
            }
            else if (statName.startsWith("deaths"))
            {
                this.sbDeathStats.add(new SkyBlockStats(this.replaceStatsString(statName, "deaths"), value));
            }
            else
            {
                if (statName.equals("chicken_race_best_time_2"))
                {
                    statName = "chicken_race_best_time";
                }
                this.sbOtherStats.add(new SkyBlockStats(WordUtils.capitalize(statName.replace("_", " ")), value));
            }
        }
    }

    private long checkSkyBlockItem(List<ItemStack> list, String type)
    {
        return list.stream().filter(armor -> armor != null && armor.hasTagCompound() && armor.getTagCompound().getCompoundTag("ExtraAttributes").getString("id").startsWith(type)).count();
    }

    private void getInventories(JsonObject currentProfile)
    {
        this.armorItems.addAll(SkyBlockAPIUtils.decodeItem(currentProfile, "inv_armor"));

        if (this.checkSkyBlockItem(this.armorItems, "SUPERIOR_DRAGON_") == 4)
        {
            this.hasFullSuperiorDragon = true;
        }
        else if (this.checkSkyBlockItem(this.armorItems, "LAPIS_ARMOR_") == 4)
        {
            this.hasFullLapisArmor = true;
        }
        else if (this.checkSkyBlockItem(this.armorItems, "MASTIFF_") == 4)
        {
            this.hasFullMastiff = true;
        }
        else if (this.checkSkyBlockItem(this.armorItems, "YOUNG_DRAGON_") == 4)
        {
            this.hasFullYoungDragon = true;
        }
        else if (this.checkSkyBlockItem(this.armorItems, "SPEEDSTER_") == 4)
        {
            this.hasFullSpeedster = true;
        }

        for (int i = 0; i < 4; ++i)
        {
            GuiSkyBlockData.TEMP_ARMOR_INVENTORY.setInventorySlotContents(i, this.armorItems.get(i));
        }

        List<ItemStack> mainInventory = SkyBlockAPIUtils.decodeItem(currentProfile, "inv_contents");
        List<ItemStack> accessoryInventory = SkyBlockAPIUtils.decodeItem(currentProfile, "talisman_bag");

        SKYBLOCK_INV.add(new SkyBlockInventory(mainInventory, SkyBlockInventoryTabs.INVENTORY));
        SKYBLOCK_INV.add(new SkyBlockInventory(SkyBlockAPIUtils.decodeItem(currentProfile, "ender_chest_contents"), SkyBlockInventoryTabs.ENDER_CHEST));
        SKYBLOCK_INV.add(new SkyBlockInventory(accessoryInventory, SkyBlockInventoryTabs.ACCESSORY));
        SKYBLOCK_INV.add(new SkyBlockInventory(SkyBlockAPIUtils.decodeItem(currentProfile, "potion_bag"), SkyBlockInventoryTabs.POTION));
        SKYBLOCK_INV.add(new SkyBlockInventory(SkyBlockAPIUtils.decodeItem(currentProfile, "fishing_bag"), SkyBlockInventoryTabs.FISHING));
        SKYBLOCK_INV.add(new SkyBlockInventory(SkyBlockAPIUtils.decodeItem(currentProfile, "quiver"), SkyBlockInventoryTabs.QUIVER));
        SKYBLOCK_INV.add(new SkyBlockInventory(SkyBlockAPIUtils.decodeItem(currentProfile, "candy_inventory_contents"), SkyBlockInventoryTabs.CANDY));

        this.inventoryToStats.addAll(mainInventory);
        this.inventoryToStats.addAll(accessoryInventory);

        if (this.checkSkyBlockItem(this.inventoryToStats, "NIGHT_CRYSTAL") == 1 || this.checkSkyBlockItem(this.inventoryToStats, "DAY_CRYSTAL") == 1)
        {
            this.hasDayNightCrystal = true;
        }
    }

    private void getSlayerInfo(JsonObject currentProfile)
    {
        JsonElement slayerBosses = currentProfile.get("slayer_bosses");

        if (slayerBosses != null)
        {
            this.slayerInfo.addAll(this.getSlayer(slayerBosses, SlayerType.ZOMBIE));
            this.slayerInfo.addAll(this.getSlayer(slayerBosses, SlayerType.SPIDER));
            this.slayerInfo.addAll(this.getSlayer(slayerBosses, SlayerType.WOLF));
        }
        else
        {
            this.slayerInfo.add(new SkyBlockSlayerInfo(EnumChatFormatting.RED + "Slayer Info: Slayer data not available!"));
        }
    }

    private void createFakePlayer()
    {
        if (this.mc.getNetHandler().getPlayerInfo(this.profile.getName()) == null)
        {
            this.mc.getNetHandler().playerInfoMap.put(this.profile.getId(), ((IViewerLoader)new NetworkPlayerInfo(this.profile)).setLoadedFromViewer(true)); // hack into map to show their skin :D
        }
        this.player = new EntityOtherFakePlayer(this.mc.theWorld, this.profile);
        GuiSkyBlockData.renderSecondLayer = true;

        for (int i = 0; i < this.armorItems.size(); i++)
        {
            ItemStack armor = this.armorItems.get(i);

            if (armor == null)
            {
                continue;
            }

            int index = EntityLiving.getArmorPosition(armor);

            if (armor.getItem() instanceof ItemBlock)
            {
                index = 4;
            }
            this.player.setCurrentItemOrArmor(index, armor);
        }
    }

    private List<SkyBlockSlayerInfo> getSlayer(JsonElement element, SlayerType type)
    {
        List<SkyBlockSlayerInfo> list = new ArrayList<>();
        JsonElement slayer = element.getAsJsonObject().get(type.name().toLowerCase());
        JsonElement xp = slayer.getAsJsonObject().get("xp");

        if (xp != null)
        {
            int playerSlayerXp = xp.getAsInt();
            int xpRequired = 0;
            int slayerLvl = 0;
            int levelToCheck = 0;
            int xpToNextLvl = 0;

            for (SkillProgress skill : SkillProgress.SLAYER_SKILL)
            {
                int slayerXp = (int)skill.getXp();

                if (slayerXp <= playerSlayerXp)
                {
                    levelToCheck = skill.getLevel();

                    if (levelToCheck < SkillProgress.SLAYER_SKILL.length)
                    {
                        xpRequired = (int)SkillProgress.SLAYER_SKILL[levelToCheck].getXp();
                    }
                    ++slayerLvl;
                }
            }

            if (levelToCheck < SkillProgress.SLAYER_SKILL.length)
            {
                levelToCheck += 1;
                xpToNextLvl = xpRequired - playerSlayerXp;
            }
            else
            {
                levelToCheck = SkillProgress.SLAYER_SKILL.length;
            }

            this.setSlayerSkillLevel(type, slayerLvl);

            list.add(new SkyBlockSlayerInfo(EnumChatFormatting.GRAY + type.getName() + " Slayer: " + EnumChatFormatting.YELLOW + "LVL " + slayerLvl));
            list.add(new SkyBlockSlayerInfo(EnumChatFormatting.GRAY + "EXP: " + EnumChatFormatting.LIGHT_PURPLE + (xpToNextLvl == 0 ? FORMAT.format(playerSlayerXp) : FORMAT.format(playerSlayerXp) + EnumChatFormatting.DARK_PURPLE + "/" + EnumChatFormatting.LIGHT_PURPLE + FORMAT.format(xpRequired))));

            if (xpToNextLvl != 0)
            {
                list.add(new SkyBlockSlayerInfo(EnumChatFormatting.GRAY + "XP to " + EnumChatFormatting.YELLOW + "LVL " + levelToCheck + ": " + EnumChatFormatting.LIGHT_PURPLE + FORMAT.format(xpToNextLvl)));
            }

            list.add(SkyBlockSlayerInfo.createMobAndXp(type.getName(), playerSlayerXp + "," + xpRequired + "," + xpToNextLvl));
            int amount = 0;

            for (int i = 1; i <= 4; i++)
            {
                JsonElement kill = slayer.getAsJsonObject().get("boss_kills_tier_" + (i - 1));
                int kills = this.getSlayerKill(kill);
                amount += this.getSlayerPrice(kills, i - 1);
                list.add(new SkyBlockSlayerInfo(EnumChatFormatting.GRAY + "Tier " + i + ": " + EnumChatFormatting.YELLOW + this.formatSlayerKill(this.getSlayerKill(kill))));
            }
            this.slayerTotalAmountSpent += amount;
            list.add(new SkyBlockSlayerInfo(EnumChatFormatting.GRAY + "Amount Spent: " + EnumChatFormatting.YELLOW + FORMAT.format(amount)));
            list.add(SkyBlockSlayerInfo.empty());
            return list;
        }
        else
        {
            return Collections.singletonList(new SkyBlockSlayerInfo(EnumChatFormatting.RED + "Slayer Info: No " + type.name().toLowerCase() + " slayer data!"));
        }
    }

    private void setSlayerSkillLevel(SlayerType type, int currentLevel)
    {
        switch (type)
        {
        case ZOMBIE:
            this.zombieSlayerLevel = currentLevel;
            break;
        case SPIDER:
            this.spiderSlayerLevel = currentLevel;
            break;
        case WOLF:
            this.wolfSlayerLevel = currentLevel;
            break;
        default:
            break;
        }
    }

    private int getSlayerKill(JsonElement element)
    {
        if (element != null)
        {
            int kills = element.getAsInt();
            return kills;
        }
        return 0;
    }

    private int getSlayerPrice(int kills, int index)
    {
        int price = 0;

        switch (index)
        {
        default:
        case 0:
            price = 100;
            break;
        case 1:
            price = 2000;
            break;
        case 2:
            price = 10000;
            break;
        case 3:
            price = 50000;
            break;
        }
        return kills * price;
    }

    private String formatSlayerKill(int kills)
    {
        return FORMAT.format(kills) + " kill" + (kills <= 1 ? "" : "s");
    }

    private static void drawEntityOnScreen(int posX, int posY, int scale, EntityLivingBase entity)
    {
        GlStateManager.enableColorMaterial();
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, 50.0F);
        GlStateManager.scale(-scale, scale, scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(ClientEventHandler.renderPartialTicks, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        entity.rotationYaw = (float)(Math.atan(0) * 40.0F);
        entity.rotationYawHead = entity.rotationYaw;
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setRenderShadow(false);
        rendermanager.doRenderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        rendermanager.setRenderShadow(true);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    class SkyBlockInventory
    {
        private final List<ItemStack> items;
        private final SkyBlockInventoryTabs tab;

        public SkyBlockInventory(List<ItemStack> items, SkyBlockInventoryTabs tab)
        {
            this.items = items;
            this.tab = tab;
        }

        public List<ItemStack> getItems()
        {
            return this.items;
        }

        public SkyBlockInventoryTabs getTab()
        {
            return this.tab;
        }
    }

    static class ContainerArmor extends Container
    {
        public ContainerArmor()
        {
            this.addSlotToContainer(new Slot(GuiSkyBlockData.TEMP_ARMOR_INVENTORY, 0, -52, 65));
            this.addSlotToContainer(new Slot(GuiSkyBlockData.TEMP_ARMOR_INVENTORY, 1, -52, 47));
            this.addSlotToContainer(new Slot(GuiSkyBlockData.TEMP_ARMOR_INVENTORY, 2, -52, 26));
            this.addSlotToContainer(new Slot(GuiSkyBlockData.TEMP_ARMOR_INVENTORY, 3, -52, 2));
        }

        @Override
        public boolean canInteractWith(EntityPlayer player)
        {
            return false;
        }

        @Override
        protected void retrySlotClick(int index, int clickedButton, boolean mode, EntityPlayer player) {}

        @Override
        public ItemStack transferStackInSlot(EntityPlayer player, int index)
        {
            return null;
        }

        @Override
        public boolean canMergeSlot(ItemStack itemStack, Slot slot)
        {
            return false;
        }

        @Override
        public boolean canDragIntoSlot(Slot slot)
        {
            return false;
        }
    }

    static class ContainerSkyBlock extends Container
    {
        public final List<ItemStack> itemList = new ArrayList<>();

        public ContainerSkyBlock()
        {
            for (int columns = 0; columns < 4; ++columns)
            {
                for (int rows = 0; rows < 9; ++rows)
                {
                    this.addSlotToContainer(new Slot(GuiSkyBlockData.TEMP_INVENTORY, columns * 9 + rows, 13 + rows * 18, 18 + columns * 18));
                }
            }
            this.scrollTo(0.0F);
        }

        @Override
        public boolean canInteractWith(EntityPlayer player)
        {
            return false;
        }

        @Override
        protected void retrySlotClick(int index, int clickedButton, boolean mode, EntityPlayer player) {}

        @Override
        public ItemStack transferStackInSlot(EntityPlayer player, int index)
        {
            return null;
        }

        @Override
        public boolean canMergeSlot(ItemStack itemStack, Slot slot)
        {
            return false;
        }

        @Override
        public boolean canDragIntoSlot(Slot slot)
        {
            return false;
        }

        public boolean canScroll()
        {
            return this.itemList.size() > GuiSkyBlockData.SIZE;
        }

        public void scrollTo(float scroll)
        {
            int i = (this.itemList.size() + 9 - 1) / 9 - 4;
            int j = (int)(scroll * i + 0.5D);

            if (j < 0)
            {
                j = 0;
            }

            for (int k = 0; k < 4; ++k)
            {
                for (int l = 0; l < 9; ++l)
                {
                    int i1 = l + (k + j) * 9;

                    if (i1 >= 0 && i1 < this.itemList.size())
                    {
                        GuiSkyBlockData.TEMP_INVENTORY.setInventorySlotContents(l + k * 9, this.itemList.get(i1));
                    }
                    else
                    {
                        GuiSkyBlockData.TEMP_INVENTORY.setInventorySlotContents(l + k * 9, null);
                    }
                }
            }
        }
    }

    class SkyBlockInfo
    {
        private final String title;
        private final String value;

        public SkyBlockInfo(String title, String value)
        {
            this.title = title;
            this.value = value;
        }

        public String getTitle()
        {
            return this.title;
        }

        public String getValue()
        {
            return this.value;
        }
    }

    class SkyBlockSkillInfo
    {
        private final String name;
        private final int currentXp;
        private final int xpRequired;
        private final int currentLvl;
        private final float skillProgress;
        private final boolean reachLimit;

        public SkyBlockSkillInfo(String name, int currentXp, int xpRequired, int currentLvl, float skillProgress, boolean reachLimit)
        {
            this.name = name;
            this.currentXp = currentXp;
            this.xpRequired = xpRequired;
            this.currentLvl = currentLvl;
            this.skillProgress = skillProgress;
            this.reachLimit = reachLimit;
        }

        public String getName()
        {
            return this.name;
        }

        public int getCurrentXp()
        {
            return this.currentXp;
        }

        public int getXpRequired()
        {
            return this.xpRequired;
        }

        public int getCurrentLvl()
        {
            return this.currentLvl;
        }

        public float getSkillProgress()
        {
            return this.skillProgress;
        }

        public boolean isReachLimit()
        {
            return this.reachLimit;
        }
    }

    static class SkyBlockSlayerInfo
    {
        private final String text;
        private String xp;
        private Type type = Type.TEXT;

        public SkyBlockSlayerInfo(String text)
        {
            this.text = text;
        }

        public SkyBlockSlayerInfo(String text, String xp, Type type)
        {
            this(text);
            this.xp = xp;
            this.type = type;
        }

        public String getText()
        {
            return this.text;
        }

        public String getXp()
        {
            return this.xp;
        }

        public Type getType()
        {
            return this.type;
        }

        public static SkyBlockSlayerInfo createMobAndXp(String slayerType, String xp)
        {
            return new SkyBlockSlayerInfo(slayerType, xp, Type.XP_AND_MOB);
        }

        public static SkyBlockSlayerInfo empty()
        {
            return new SkyBlockSlayerInfo("");
        }

        public enum Type
        {
            TEXT, XP_AND_MOB;
        }
    }

    static class EmptyStats extends GuiScrollingList
    {
        private final Type type;

        public EmptyStats(Minecraft mc, int width, int height, int top, int bottom, int left, int entryHeight, int parentWidth, int parentHeight, Type type)
        {
            super(mc, width, height, top, bottom, left, entryHeight, parentWidth, parentHeight);
            this.type = type;
        }

        @Override
        protected int getSize()
        {
            return 1;
        }

        @Override
        protected void drawSlot(int index, int right, int top, int height, Tessellator tess) {}

        @Override
        protected void drawBackground() {}

        @Override
        protected void elementClicked(int index, boolean doubleClick) {}

        @Override
        protected boolean isSelected(int index)
        {
            return false;
        }

        public Type getType()
        {
            return this.type;
        }

        enum Type
        {
            INVENTORY, SKILL;
        }
    }

    class InfoStats extends GuiScrollingList
    {
        private final List<SkyBlockInfo> stats;
        private final GuiSkyBlockData parent;

        public InfoStats(GuiSkyBlockData parent, int width, int height, int top, int bottom, int left, int entryHeight, int parentWidth, int parentHeight, List<SkyBlockInfo> stats)
        {
            super(parent.mc, width, height, top, bottom, left, entryHeight, parentWidth, parentHeight);
            this.stats = stats;
            this.parent = parent;
        }

        @Override
        protected int getSize()
        {
            return this.stats.size();
        }

        @Override
        protected void drawSlot(int index, int right, int top, int height, Tessellator tess)
        {
            SkyBlockInfo stat = this.stats.get(index);
            this.parent.drawString(this.parent.mc.fontRendererObj, stat.getTitle(), this.left + 3, top, index % 2 == 0 ? 16777215 : 9474192);
            this.parent.drawString(this.parent.mc.fontRendererObj, stat.getValue(), this.right - this.parent.mc.fontRendererObj.getStringWidth(stat.getValue()) - 10, top, index % 2 == 0 ? 16777215 : 9474192);
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick) {}

        @Override
        protected void drawBackground() {}

        @Override
        protected boolean isSelected(int index)
        {
            return false;
        }
    }

    class SlayerStats extends GuiScrollingList
    {
        private final List<SkyBlockSlayerInfo> stats;
        private final GuiSkyBlockData parent;

        public SlayerStats(GuiSkyBlockData parent, int width, int height, int top, int bottom, int left, int entryHeight, int parentWidth, int parentHeight, List<SkyBlockSlayerInfo> stats)
        {
            super(parent.mc, width, height, top, bottom, left, entryHeight, parentWidth, parentHeight);
            this.stats = stats;
            this.parent = parent;

            if (this.stats.size() <= 3)
            {
                this.setHeaderInfo(false, 0);
            }
            else
            {
                this.setHeaderInfo(true, 16);
            }
        }

        @Override
        protected int getSize()
        {
            return this.stats.size();
        }

        @Override
        protected void drawSlot(int index, int right, int top, int height, Tessellator tess)
        {
            SkyBlockSlayerInfo stat = this.stats.get(index);

            switch (stat.getType())
            {
            case XP_AND_MOB:
                if (stat.getText().equals("Zombie"))
                {
                    EntityZombie zombie = new EntityZombie(this.parent.mc.theWorld);
                    ItemStack heldItem = new ItemStack(Items.diamond_hoe);
                    ItemStack helmet = RenderUtils.getSkullItemStack(GuiSkyBlockData.REVENANT_HORROR_HEAD[0], GuiSkyBlockData.REVENANT_HORROR_HEAD[1]);
                    ItemStack chestplate = new ItemStack(Items.diamond_chestplate);
                    ItemStack leggings = new ItemStack(Items.chainmail_leggings);
                    ItemStack boots = new ItemStack(Items.diamond_boots);
                    zombie.setCurrentItemOrArmor(0, heldItem);
                    zombie.setCurrentItemOrArmor(1, boots);
                    zombie.setCurrentItemOrArmor(2, leggings);
                    zombie.setCurrentItemOrArmor(3, chestplate);
                    zombie.setCurrentItemOrArmor(4, helmet);
                    GuiSkyBlockData.drawEntityOnScreen(this.left + 80, top + 60, 40, zombie);
                }
                else if (stat.getText().equals("Spider"))
                {
                    EntitySpider spider = new EntitySpider(this.parent.mc.theWorld);
                    EntityCaveSpider cave = new EntityCaveSpider(this.parent.mc.theWorld);
                    GuiSkyBlockData.drawEntityOnScreen(this.left + 80, top + 40, 40, cave);
                    GuiSkyBlockData.drawEntityOnScreen(this.left + 80, top + 60, 40, spider);
                    GlStateManager.blendFunc(770, 771);
                }
                else
                {
                    EntityWolf wolf = new EntityWolf(this.parent.mc.theWorld);
                    wolf.setAngry(true);
                    GuiSkyBlockData.drawEntityOnScreen(this.left + 80, top + 60, 40, wolf);
                }

                this.parent.mc.getTextureManager().bindTexture(XP_BARS);
                GlStateManager.color(0.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableBlend();

                String[] xpSplit = stat.getXp().split(",");
                int playerSlayerXp = Integer.valueOf(xpSplit[0]);
                int xpRequired = Integer.valueOf(xpSplit[1]);

                int filled = Math.min((int)Math.floor(playerSlayerXp * 92 / xpRequired), 91);
                Gui.drawModalRectWithCustomSizedTexture(this.right - 120, top, 0, 0, 91, 5, 91, 10);

                if (filled > 0)
                {
                    Gui.drawModalRectWithCustomSizedTexture(this.right - 120, top, 0, 5, filled, 5, 91, 10);
                }

                GlStateManager.enableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                break;
            default:
                if (this.getSize() <= 3)
                {
                    this.parent.drawString(this.parent.mc.fontRendererObj, stat.getText(), this.left + 8, top, 16777215);
                }
                else
                {
                    this.parent.drawString(this.parent.mc.fontRendererObj, stat.getText(), this.right - this.parent.mc.fontRendererObj.getStringWidth(stat.getText()) - 30, top, 16777215);
                }
                break;
            }
        }

        @Override
        protected void drawScreen(int mouseX, int mouseY) {}

        @Override
        protected void elementClicked(int index, boolean doubleClick) {}

        @Override
        protected void drawBackground() {}

        @Override
        protected boolean isSelected(int index)
        {
            return false;
        }
    }

    class OtherStats extends GuiScrollingList
    {
        private final List<SkyBlockStats> stats;
        private final GuiSkyBlockData parent;

        public OtherStats(GuiSkyBlockData parent, int width, int height, int top, int bottom, int left, int entryHeight, int parentWidth, int parentHeight, List<SkyBlockStats> stats, SkyBlockStats.Type type)
        {
            super(parent.mc, width, height, top, bottom, left, entryHeight, parentWidth, parentHeight);
            stats.sort((stat1, stat2) -> type == SkyBlockStats.Type.OTHERS ? new CompareToBuilder().append(stat1.getName(), stat2.getName()).build() : new CompareToBuilder().append(stat2.getValue(), stat1.getValue()).build());

            if (stats.isEmpty())
            {
                stats.add(new SkyBlockStats("Empty " + type.name().toLowerCase() + " data!", 0.0F));
            }

            this.stats = stats;
            this.parent = parent;
        }

        @Override
        protected int getSize()
        {
            return this.stats.size();
        }

        @Override
        protected void drawSlot(int index, int right, int top, int height, Tessellator tess)
        {
            SkyBlockStats stat = this.stats.get(index);
            this.parent.drawString(this.parent.mc.fontRendererObj, stat.getName(), this.left + 3, top, index % 2 == 0 ? 16777215 : 9474192);

            if (this.stats.size() > 1)
            {
                this.parent.drawString(this.parent.mc.fontRendererObj, stat.getValueByString(), this.right - this.parent.mc.fontRendererObj.getStringWidth(stat.getValueByString()) - 10, top, index % 2 == 0 ? 16777215 : 9474192);
            }
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick) {}

        @Override
        protected void drawBackground() {}

        @Override
        protected boolean isSelected(int index)
        {
            return false;
        }
    }

    public class BonusStatTemplate
    {
        private int health;
        private int defense;
        private int trueDefense;
        private int effectiveHealth;
        private int strength;
        private int speed;
        private int critChance;
        private int critDamage;
        private int intelligence;

        public BonusStatTemplate(int health, int defense, int trueDefense, int effectiveHealth, int strength, int speed, int critChance, int critDamage, int intelligence)
        {
            this.health = health;
            this.defense = defense;
            this.trueDefense = trueDefense;
            this.effectiveHealth = effectiveHealth;
            this.strength = strength;
            this.speed = speed;
            this.critChance = critChance;
            this.critDamage = critDamage;
            this.intelligence = intelligence;
        }

        public BonusStatTemplate add(BonusStatTemplate toAdd)
        {
            this.health += toAdd.health;
            this.defense += toAdd.defense;
            this.trueDefense += toAdd.trueDefense;
            this.effectiveHealth += toAdd.effectiveHealth;
            this.strength += toAdd.strength;
            this.speed += toAdd.speed;
            this.critChance += toAdd.critChance;
            this.critDamage += toAdd.critDamage;
            this.intelligence += toAdd.intelligence;
            return new BonusStatTemplate(this.health, this.defense, this.trueDefense, this.effectiveHealth, this.strength, this.speed, this.critChance, this.critDamage, this.intelligence);
        }

        public int getHealth()
        {
            return this.health;
        }

        public int getDefense()
        {
            if (this.defense <= 0)
            {
                return 0;
            }
            return this.defense;
        }

        public int getTrueDefense()
        {
            return this.trueDefense;
        }

        public int getEffectiveHealth()
        {
            return this.effectiveHealth;
        }

        public int getStrength()
        {
            return this.strength;
        }

        public int getSpeed()
        {
            return this.speed;
        }

        public int getCritChance()
        {
            if (this.critChance > 100)
            {
                return 100;
            }
            return this.critChance;
        }

        public int getCritDamage()
        {
            return this.critDamage;
        }

        public int getIntelligence()
        {
            return this.intelligence;
        }

        public void setHealth(int health)
        {
            this.health = health;
        }

        public void setDefense(int defense)
        {
            this.defense = defense;
        }

        public void setTrueDefense(int trueDefense)
        {
            this.trueDefense = trueDefense;
        }

        public void setEffectiveHealth(int effectiveHealth)
        {
            this.effectiveHealth = effectiveHealth;
        }

        public void setStrength(int strength)
        {
            this.strength = strength;
        }

        public void setSpeed(int speed)
        {
            this.speed = speed;
        }

        public void setCritChance(int critChance)
        {
            this.critChance = critChance;
        }

        public void setCritDamage(int critDamage)
        {
            this.critDamage = critDamage;
        }

        public void setIntelligence(int intelligence)
        {
            this.intelligence = intelligence;
        }

        public BonusStatTemplate addHealth(int health)
        {
            this.health += health;
            return this;
        }

        public BonusStatTemplate addDefense(int defense)
        {
            this.defense += defense;
            return this;
        }

        public BonusStatTemplate addTrueDefense(int trueDefense)
        {
            this.trueDefense += trueDefense;
            return this;
        }

        public BonusStatTemplate addEffectiveHealth(int effectiveHealth)
        {
            this.effectiveHealth += effectiveHealth;
            return this;
        }

        public BonusStatTemplate addStrength(int strength)
        {
            this.strength += strength;
            return this;
        }

        public BonusStatTemplate addSpeed(int speed)
        {
            this.speed += speed;
            return this;
        }

        public BonusStatTemplate addCritChance(int critChance)
        {
            this.critChance += critChance;
            return this;
        }

        public BonusStatTemplate addCritDamage(int critDamage)
        {
            this.critDamage += critDamage;
            return this;
        }

        public BonusStatTemplate addIntelligence(int intelligence)
        {
            this.intelligence += intelligence;
            return this;
        }
    }

    private enum SkillType
    {
        FARMING("Farming"),
        FORAGING("Foraging"),
        MINING("Mining"),
        FISHING("Fishing"),
        COMBAT("Combat"),
        ENCHANTING("Enchanting"),
        ALCHEMY("Alchemy"),
        RUNECRAFTING("Runecrafting"),
        CARPENTRY("Carpentry");

        private final String name;

        private SkillType(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return this.name;
        }
    }

    private enum SlayerType
    {
        ZOMBIE("Zombie"),
        SPIDER("Spider"),
        WOLF("Wolf");

        private final String name;

        private SlayerType(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return this.name;
        }
    }

    private enum ViewButton
    {
        INFO(10),
        SKILLS(11),
        SLAYERS(12),
        OTHER_STATS(13);

        private int id;
        protected static final ViewButton[] VALUES = ViewButton.values();

        private ViewButton(int id)
        {
            this.id = id;
        }

        public static ViewButton getTypeForButton(GuiButton button)
        {
            for (ViewButton viewButton : ViewButton.VALUES)
            {
                if (viewButton.id == button.id)
                {
                    return viewButton;
                }
            }
            return null;
        }
    }

    private enum OtherStatsViewButton
    {
        KILLS(20),
        DEATHS(21),
        OTHER_STATS(22);

        private int id;
        protected static final OtherStatsViewButton[] VALUES = OtherStatsViewButton.values();

        private OtherStatsViewButton(int id)
        {
            this.id = id;
        }

        public static OtherStatsViewButton getTypeForButton(GuiButton button)
        {
            for (OtherStatsViewButton viewButton : OtherStatsViewButton.VALUES)
            {
                if (viewButton.id == button.id)
                {
                    return viewButton;
                }
            }
            return null;
        }
    }

    private enum BasicInfoViewButton
    {
        INFO(30),
        INVENTORY(31);

        private int id;
        protected static final BasicInfoViewButton[] VALUES = BasicInfoViewButton.values();

        private BasicInfoViewButton(int id)
        {
            this.id = id;
        }

        public static BasicInfoViewButton getTypeForButton(GuiButton button)
        {
            for (BasicInfoViewButton viewButton : BasicInfoViewButton.VALUES)
            {
                if (viewButton.id == button.id)
                {
                    return viewButton;
                }
            }
            return null;
        }
    }
}
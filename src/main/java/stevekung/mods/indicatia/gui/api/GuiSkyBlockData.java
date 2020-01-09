package stevekung.mods.indicatia.gui.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.lwjgl.input.Mouse;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiScrollingList;
import stevekung.mods.indicatia.event.ClientEventHandler;
import stevekung.mods.indicatia.utils.*;

public class GuiSkyBlockData extends GuiScreen
{
    private static final ResourceLocation INVENTORY_TABS = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
    private static final ResourceLocation XP_BARS = new ResourceLocation("indicatia:textures/gui/skill_xp_bar.png");
    private static final String[] REVENANT_HORROR_HEAD = new String[] {"0862e0b0-a14f-3f93-894f-013502936b59", "eyJ0aW1lc3RhbXAiOjE1Njg0NTc0MjAxMzcsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RiYWQ5OWVkM2M4MjBiNzk3ODE5MGFkMDhhOTM0YTY4ZGZhOTBkOTk4NjgyNWRhMWM5N2Y2ZjIxZjQ5YWQ2MjYifX19"};

    // Based stuff
    private boolean loadingApi = true;
    private boolean loadingArmorItems = true;
    private boolean resize;
    private GuiButton doneButton;
    private GuiButton backButton;
    private final List<SkyBlockFallbackData> profiles;
    private final String sbProfileId;
    private final String sbProfileName;
    private final String username;
    private final String uuid;
    private final GameProfile profile;
    private final StopWatch watch = new StopWatch();
    private int percent;

    // API
    private static final DecimalFormat FORMAT = new DecimalFormat("#,###,###,###,###");
    public static boolean renderSecondLayer;
    private GuiScrollingList currentSlot;
    private static final int MAX_FAIRY_SOULS = 190;
    private final List<SkyBlockInfo> infoList = new CopyOnWriteArrayList<>();
    private final List<SkyBlockSlayerInfo> slayerInfo = new CopyOnWriteArrayList<>();
    private List<SkyBlockStats> sbKillStats = new ArrayList<>();
    private List<SkyBlockStats> sbDeathStats = new ArrayList<>();
    private List<SkyBlockStats> sbOtherStats = new ArrayList<>();
    private List<ItemStack> armorItems = new ArrayList<>();
    private int selectedView = -1;
    private int selectedOtherStatsView = -1;

    // Info & Inventory
    private static final int SIZE = 54;
    private static final InventoryBasic TEMP_INVENTORY = new InventoryBasic("tmp", true, GuiSkyBlockData.SIZE);
    static final List<SkyBlockInventory> SKYBLOCK_INV = new ArrayList<>();
    private int selectedTabIndex = SkyBlockInventoryTabs.INFO.getTabIndex();
    private float currentScroll;
    private boolean isScrolling;
    private boolean wasClicking;
    private final ContainerSkyBlock inventorySlots;

    // GuiContainer fields
    private int xSize;
    private int ySize;
    private int guiLeft;
    private int guiTop;
    private Slot theSlot;

    public GuiSkyBlockData(List<SkyBlockFallbackData> profiles, String sbProfileId, String sbProfileName, String username, String uuid, GameProfile profile)
    {
        this.allowUserInput = true;
        this.inventorySlots = new ContainerSkyBlock();
        this.profiles = profiles;
        this.sbProfileId = sbProfileId;
        this.sbProfileName = sbProfileName;
        this.username = username;
        this.uuid = uuid;
        this.profile = profile;

        this.xSize = 225;
        this.ySize = 132;
    }

    @Override
    public void initGui()
    {
        this.buttonList.clear();
        this.infoList.clear();

        if (!this.resize)
        {
            CommonUtils.POOL.execute(() ->
            {
                this.watch.start();

                try
                {
                    this.getPlayerData();
                }
                catch (IOException | JsonSyntaxException | JsonIOException e)
                {
                    e.printStackTrace();
                    this.loadingApi = false;
                }
                this.watch.stop();
            });
        }

        this.buttonList.add(this.doneButton = new GuiButton(0, this.width / 2 - 154, this.height - 27, 150, 20, LangUtils.translate("gui.done")));
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

        int i = this.selectedTabIndex;
        this.selectedTabIndex = -1;
        this.setCurrentTab(SkyBlockInventoryTabs.tabArray[i]);

        this.guiLeft = (this.width - this.xSize) / 2 + 50;
        this.guiTop = (this.height - this.ySize) / 2 + 2;

        this.currentSlot = new InfoStats(this.mc, this.width - 119, this.height, 44, this.height - 30, 59, 16, this.width, this.height, this.armorItems, this.profile);
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
        if (this.loadingArmorItems)
        {
            this.currentSlot = new InfoStats(this.mc, this.width - 119, this.height, 44, this.height - 30, 59, 16, this.width, this.height, this.armorItems, this.profile);
            this.loadingArmorItems = false;
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

        if (this.selectedView != -1)
        {
            if (this.selectedView == ViewButton.INFO.id)
            {
                this.currentSlot = new InfoStats(this.mc, width - 119, height, 44, height - 30, 59, 12, width, height, this.armorItems, this.profile);
            }
            else if (this.selectedView == ViewButton.OTHER_STATS.id)
            {
                this.currentSlot = new OtherStats(this, width - 119, height, 44, height - 30, 59, 12, width, height, this.sbKillStats, SkyBlockStats.Type.KILLS);
            }
            else
            {
                this.currentSlot = null;
            }
        }
        if (this.selectedOtherStatsView != -1)
        {
            if (this.selectedOtherStatsView == OtherStatsViewButton.KILLS.id)
            {
                List<SkyBlockStats> sbKillStats = this.sbKillStats;
                this.sbKillStats = sbKillStats;
            }
            else if (this.selectedOtherStatsView == OtherStatsViewButton.DEATHS.id)
            {
                List<SkyBlockStats> sbDeathStats = this.sbDeathStats;
                this.sbDeathStats = sbDeathStats;
            }
            else
            {
                List<SkyBlockStats> sbOtherStats = this.sbOtherStats;
                this.sbOtherStats = sbOtherStats;
            }
        }
        super.onResize(mc, width, height);
    }

    @Override
    public void onGuiClosed()
    {
        TEMP_INVENTORY.clear();
        SKYBLOCK_INV.clear();
        GuiSkyBlockData.renderSecondLayer = false;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            this.actionPerformedViewInfo(button);
            this.actionPerformedOtherStats(button);

            if (button.id == 0)
            {
                this.mc.displayGuiScreen(null);
            }
            else if (button.id == 1)
            {
                this.mc.displayGuiScreen(new GuiSkyBlockProfileSelection(this.username, this.profiles));
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
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (this.loadingApi)
        {
            return;
        }
        else
        {
            if (mouseButton == 0)
            {
                int i = mouseX - this.guiLeft;
                int j = mouseY - this.guiTop;

                for (SkyBlockInventoryTabs tab : SkyBlockInventoryTabs.tabArray)
                {
                    if (this.isMouseOverTab(tab, i, j))
                    {
                        return;
                    }
                }
            }
            super.mouseClicked(mouseX, mouseY, mouseButton);
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
        }
        else
        {
            if (this.currentSlot != null)
            {
                this.currentSlot.drawScreen(mouseX, mouseY, partialTicks);
            }

            this.drawCenteredString(this.fontRendererObj, EnumChatFormatting.GOLD + this.username + "'s Profile: " + this.sbProfileName, this.width / 2, 34, 16777215);

            if (this.currentSlot instanceof InfoStats)
            {
                boolean flag = Mouse.isButtonDown(0);
                int i = this.guiLeft;
                int j = this.guiTop;
                int k = i + 175;
                int l = j + 18;
                int i1 = k + 14;
                int j1 = l + 112;

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
                    this.inventorySlots.scrollTo(this.currentScroll);
                }

                this.drawTabsBackgroundLayer(partialTicks, mouseX, mouseY);
                GlStateManager.disableRescaleNormal();
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
            }

            super.drawScreen(mouseX, mouseY, partialTicks);

            if (this.currentSlot instanceof InfoStats)
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

                if (this.theSlot != null && this.theSlot.getHasStack())
                {
                    ItemStack itemStack1 = this.theSlot.getStack();
                    this.renderToolTip(itemStack1, mouseX, mouseY);
                }
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
            if (state == 0)
            {
                int i = mouseX - this.guiLeft;
                int j = mouseY - this.guiTop;

                for (SkyBlockInventoryTabs tab : SkyBlockInventoryTabs.tabArray)
                {
                    if (tab != null && this.isMouseOverTab(tab, i, j))
                    {
                        this.setCurrentTab(tab);
                        return;
                    }
                }
            }
            super.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();

        if (i != 0 && this.needsScrollBars())
        {
            int j = this.inventorySlots.itemList.size() / 9 - 6;

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
            this.inventorySlots.scrollTo(this.currentScroll);
        }
    }

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
            this.selectedView = type.id;

            if (type.id == ViewButton.INFO.id)
            {
                this.currentSlot = new InfoStats(this.mc, this.width - 119, this.height, 44, this.height - 30, 59, 16, this.width, this.height, this.armorItems, this.profile);
                this.hideOtherStatsButton();
            }
            else if (type.id == ViewButton.SLAYERS.id)
            {
                this.currentSlot = new SlayerStats(this, this.width - 119, this.height, 44, this.height - 30, 59, 16, this.width, this.height, this.slayerInfo);
                this.hideOtherStatsButton();
            }
            else if (type.id == ViewButton.OTHER_STATS.id)
            {
                this.currentSlot = new OtherStats(this, this.width - 119, this.height, 44, this.height - 54, 59, 12, this.width, this.height, this.sbKillStats, SkyBlockStats.Type.KILLS);

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
            this.selectedOtherStatsView = type.id;
        }
    }

    private boolean isMouseOverTab(SkyBlockInventoryTabs tab, int mouseX, int mouseY)//TODO
    {
        int i = tab.getTabColumn();
        int j = 32 * i;
        int k = 0;

        if (i == 5)
        {
            j = 32 * i + 2;
        }

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
        boolean test = mouseX >= j && mouseX <= j + 28 && mouseY >= k && mouseY <= k + 32;
        return test;
    }

    private boolean renderTabsHoveringText(SkyBlockInventoryTabs tab, int mouseX, int mouseY)
    {
        int i = tab.getTabColumn();
        int j = 32 * i;
        int k = 0;

        if (i == 5)
        {
            j = 32 * i + 2;
        }

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

        for (int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); ++i1)
        {
            Slot slot = this.inventorySlots.inventorySlots.get(i1);
            SkyBlockInventoryTabs tab = SkyBlockInventoryTabs.tabArray[this.selectedTabIndex];

            if (tab != null && tab != SkyBlockInventoryTabs.INFO)
            {
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
        this.itemRender.renderItemAndEffectIntoGUI(itemStack, i, j);
        this.itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, itemStack, i, j, s);
        this.itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
    }

    private boolean isMouseOverSlot(Slot slotIn, int mouseX, int mouseY)
    {
        return this.isPointInRegion(slotIn.xDisplayPosition, slotIn.yDisplayPosition, 16, 16, mouseX, mouseY);
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
        int i = this.guiLeft + 175;
        int j = this.guiTop + 18;
        int k = j + 112;
        this.mc.getTextureManager().bindTexture(INVENTORY_TABS);

        if (tab != SkyBlockInventoryTabs.INFO && tab.hasScrollBar())
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
            this.fontRendererObj.drawString(tab.getTranslatedTabLabel(), this.guiLeft + 8, this.guiTop + 6, 4210752);
        }
    }

    private boolean isPointInRegion(int left, int top, int right, int bottom, int pointX, int pointY)
    {
        int i = this.guiLeft;
        int j = this.guiTop;
        pointX = pointX - i;
        pointY = pointY - j;
        return pointX >= left - 1 && pointX < left + right + 1 && pointY >= top - 1 && pointY < top + bottom + 1;
    }

    private void drawTab(SkyBlockInventoryTabs tab)
    {
        boolean flag = tab.getTabIndex() == this.selectedTabIndex;
        boolean flag1 = tab.isTabInFirstRow();
        int i = tab.getTabColumn();
        int j = i * 28;
        int k = 0;
        int l = this.guiLeft + 32 * i;
        int i1 = this.guiTop;
        int j1 = 32;

        if (i == 5)
        {
            l = this.guiLeft + 32 * i + 2;
        }

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

    private void setCurrentTab(SkyBlockInventoryTabs tab)
    {
        if (tab == null)
        {
            return;
        }
        this.selectedTabIndex = tab.getTabIndex();
        ContainerSkyBlock container = this.inventorySlots;
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
        return SkyBlockInventoryTabs.tabArray[this.selectedTabIndex].hasScrollBar() && this.inventorySlots.canScroll();
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

    private String replaceStatsString(String statName, String replace)
    {
        String original = statName.replace(replace + "_", "").replace("_", " ");
        return original.equals(replace) ? "Total " + replace : WordUtils.capitalize(original) + " " + replace;
    }

    private void getPlayerData() throws IOException
    {
        URL url = new URL(SkyBlockAPIUtils.SKYBLOCK_PROFILE + this.sbProfileId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), StandardCharsets.UTF_8));
        JsonElement element = new JsonParser().parse(reader);
        JsonObject obj = element.getAsJsonObject();

        //LoggerIN.info("");
        //LoggerIN.info("SkyBlockData: {}", element);

        JsonObject profiles = obj.get("profile").getAsJsonObject().get("members").getAsJsonObject();
        JsonElement banking = obj.get("profile").getAsJsonObject().get("banking");

        for (Map.Entry<String, JsonElement> entry : profiles.entrySet())
        {
            String userUUID = entry.getKey();

            if (userUUID.equals(this.uuid))
            {
                //LoggerIN.info("Name: {}, UserInProfileUUID: {}", this.username, userUUID);

                JsonObject currentUserProfile = profiles.get(userUUID).getAsJsonObject();
                JsonElement fairySouls = currentUserProfile.get("fairy_souls_collected");
                int collectedSouls = 0;

                if (fairySouls != null)
                {
                    collectedSouls = fairySouls.getAsInt();
                }

                this.infoList.add(new SkyBlockInfo("Fairy Souls Collected", collectedSouls + "/" + GuiSkyBlockData.MAX_FAIRY_SOULS));

                long lastSave = currentUserProfile.get("last_save").getAsLong();
                Date past = new Date(lastSave);
                Date now = new Date();
                String startDate = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(lastSave));

                long minutes = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
                long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
                long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());

                if (minutes <= 60)
                {
                    this.infoList.add(new SkyBlockInfo("Last Updated", minutes + " minutes ago"));
                }
                else if (hours <= 24)
                {
                    this.infoList.add(new SkyBlockInfo("Last Updated", hours + " hours ago"));
                }
                else if (days <= 30)
                {
                    this.infoList.add(new SkyBlockInfo("Last Updated", days + " days ago"));
                }

                this.infoList.add(new SkyBlockInfo("Last Updated", startDate));

                if (banking != null)
                {
                    double balance = banking.getAsJsonObject().get("balance").getAsDouble();
                    this.infoList.add(new SkyBlockInfo("Banking Account", FORMAT.format(balance)));
                }
                else
                {
                    this.infoList.add(new SkyBlockInfo("Banking Account", "API is not enabled!"));
                }

                JsonElement slayerBosses = currentUserProfile.get("slayer_bosses");

                if (slayerBosses != null)
                {
                    this.slayerInfo.addAll(GuiSkyBlockData.getSlayer(slayerBosses, "Zombie"));
                    this.slayerInfo.addAll(GuiSkyBlockData.getSlayer(slayerBosses, "Spider"));
                    this.slayerInfo.addAll(GuiSkyBlockData.getSlayer(slayerBosses, "Wolf"));
                }
                else
                {
                    this.slayerInfo.add(new SkyBlockSlayerInfo("Slayer Info: Slayer data not available!"));
                }

                JsonObject stats = currentUserProfile.get("stats").getAsJsonObject();

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
                this.armorItems.addAll(SkyBlockAPIUtils.decodeItem(currentUserProfile, "inv_armor"));
                SKYBLOCK_INV.add(new SkyBlockInventory(SkyBlockAPIUtils.decodeItem(currentUserProfile, "inv_contents"), SkyBlockInventoryTabs.INVENTORY));
                SKYBLOCK_INV.add(new SkyBlockInventory(SkyBlockAPIUtils.decodeItem(currentUserProfile, "ender_chest_contents"), SkyBlockInventoryTabs.ENDER_CHEST));
                SKYBLOCK_INV.add(new SkyBlockInventory(SkyBlockAPIUtils.decodeItem(currentUserProfile, "talisman_bag"), SkyBlockInventoryTabs.ACCESSORY));
                SKYBLOCK_INV.add(new SkyBlockInventory(SkyBlockAPIUtils.decodeItem(currentUserProfile, "potion_bag"), SkyBlockInventoryTabs.POTION));
                SKYBLOCK_INV.add(new SkyBlockInventory(SkyBlockAPIUtils.decodeItem(currentUserProfile, "fishing_bag"), SkyBlockInventoryTabs.FISHING));
                SKYBLOCK_INV.add(new SkyBlockInventory(SkyBlockAPIUtils.decodeItem(currentUserProfile, "quiver"), SkyBlockInventoryTabs.QUIVER));
                SKYBLOCK_INV.add(new SkyBlockInventory(SkyBlockAPIUtils.decodeItem(currentUserProfile, "candy_inventory_contents"), SkyBlockInventoryTabs.CANDY));
                break;
            }
        }
        if (!this.armorItems.isEmpty())
        {
            this.loadingArmorItems = true;
        }
        this.loadingApi = false;
    }

    private static List<SkyBlockSlayerInfo> getSlayer(JsonElement element, String name)
    {
        List<SkyBlockSlayerInfo> list = new ArrayList<>();
        JsonElement slayer = element.getAsJsonObject().get(name.toLowerCase());
        JsonElement xp = slayer.getAsJsonObject().get("xp");

        if (xp != null)
        {
            list.add(new SkyBlockSlayerInfo(name + " Slayer"));
            list.add(SkyBlockSlayerInfo.createMob(name));

            int playerSlayerXp = xp.getAsInt();
            int maxSlayerXp = 0;
            int slayerLvl = 0;

            for (SlayerSkill slayerSkills : SlayerSkill.SKILLS)
            {
                int slayerXp = slayerSkills.getXp();

                if (slayerXp <= playerSlayerXp)
                {
                    maxSlayerXp = slayerXp;
                    slayerLvl++;
                }
            }

            list.add(SkyBlockSlayerInfo.createXp(slayerLvl + "," + playerSlayerXp + "," + maxSlayerXp));

            for (int i = 1; i <= 4; i++)
            {
                JsonElement kills = slayer.getAsJsonObject().get("boss_kills_tier_" + (i - 1));
                list.add(new SkyBlockSlayerInfo("Tier " + i + ": " + (kills != null ? FORMAT.format(kills.getAsInt()) : 0) + " kills"));
            }
            for (int i = 0; i < 2; i++)
            {
                list.add(SkyBlockSlayerInfo.empty());
            }
            return list;
        }
        else
        {
            return Collections.singletonList(new SkyBlockSlayerInfo("Slayer Info: No " + name.toLowerCase() + " slayer data!"));
        }
    }

    private static void drawEntityOnScreen(int posX, int posY, int scale, EntityLivingBase entity)
    {
        GlStateManager.enableColorMaterial();
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

    static class ContainerSkyBlock extends Container
    {
        public final List<ItemStack> itemList = new ArrayList<>();

        public ContainerSkyBlock()
        {
            for (int columns = 0; columns < 6; ++columns)
            {
                for (int rows = 0; rows < 9; ++rows)
                {
                    this.addSlotToContainer(new Slot(GuiSkyBlockData.TEMP_INVENTORY, columns * 9 + rows, 9 + rows * 18, 18 + columns * 18));
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
            int i = (this.itemList.size() + 9 - 1) / 9 - 6;
            int j = (int)(scroll * i + 0.5D);

            if (j < 0)
            {
                j = 0;
            }

            for (int k = 0; k < 6; ++k)
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
        private EnumChatFormatting headColor;
        private EnumChatFormatting valueColor;
        private final String head;
        private final String value;

        public SkyBlockInfo(String head, String value)
        {
            this(EnumChatFormatting.AQUA, EnumChatFormatting.BLUE, head, value);
        }

        public SkyBlockInfo(EnumChatFormatting headColor, EnumChatFormatting valueColor, String head, String value)
        {
            this.headColor = headColor;
            this.valueColor = valueColor;
            this.head = head;
            this.value = value;
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append(this.headColor);
            builder.append(this.head);
            builder.append(": ");
            builder.append(this.valueColor);
            builder.append(this.value);
            return builder.toString();
        }
    }

    static class SkyBlockSlayerInfo
    {
        private final String text;
        private Type type = Type.TEXT;

        public SkyBlockSlayerInfo(String text)
        {
            this.text = text;
        }

        public SkyBlockSlayerInfo(String text, Type type)
        {
            this(text);
            this.type = type;
        }

        public String getText()
        {
            return this.text;
        }

        public Type getType()
        {
            return this.type;
        }

        public static SkyBlockSlayerInfo createMob(String slayerType)
        {
            return new SkyBlockSlayerInfo(slayerType, Type.MOB);
        }

        public static SkyBlockSlayerInfo createXp(String xp)
        {
            return new SkyBlockSlayerInfo(xp, Type.LEVEL);
        }

        public static SkyBlockSlayerInfo empty()
        {
            return new SkyBlockSlayerInfo("");
        }

        public enum Type
        {
            TEXT, MOB, LEVEL;
        }
    }

    class InfoStats extends GuiScrollingList
    {
        private final List<ItemStack> armorItems;
        private final ScaledResolution res;
        private final Minecraft mc;
        private EntityOtherFakePlayer player;

        public InfoStats(Minecraft mc, int width, int height, int top, int bottom, int left, int entryHeight, int parentWidth, int parentHeight, List<ItemStack> armorItems, GameProfile profile)
        {
            super(mc, width, height, top, bottom, left, entryHeight, parentWidth, parentHeight);
            this.armorItems = armorItems;
            this.mc = mc;
            this.res = new ScaledResolution(this.mc);
            this.mc.getNetHandler().playerInfoMap.put(profile.getId(), new NetworkPlayerInfo(profile)); // hack into map to show their skin :D
            this.player = new EntityOtherFakePlayer(this.mc.theWorld, profile);
            GuiSkyBlockData.renderSecondLayer = true;
        }

        @Override
        protected int getSize()
        {
            return 1;
        }

        @Override
        protected void drawSlot(int index, int right, int top, int height, Tessellator tess)
        {
            for (int i = 0; i < this.armorItems.size(); i++)
            {
                ItemStack armor = this.armorItems.get(i);

                if (armor == null)
                {
                    continue;
                }
                this.player.setCurrentItemOrArmor(EntityLiving.getArmorPosition(armor), armor);
            }
            GuiSkyBlockData.drawEntityOnScreen(this.left + 20 * this.res.getScaleFactor(), top + 40, 20 * this.res.getScaleFactor(), this.player);
        }

        @Override
        protected void drawBackground()
        {
            this.player.onUpdate();
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick) {}

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
        private final ScaledResolution res;

        public SlayerStats(GuiSkyBlockData parent, int width, int height, int top, int bottom, int left, int entryHeight, int parentWidth, int parentHeight, List<SkyBlockSlayerInfo> stats)
        {
            super(parent.mc, width, height, top, bottom, left, entryHeight, parentWidth, parentHeight);
            this.stats = stats;
            this.parent = parent;
            this.res = new ScaledResolution(this.parent.mc);

            if (this.stats.size() <= 3)
            {
                this.setHeaderInfo(false, 0);
            }
            else
            {
                this.setHeaderInfo(true, 24);
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
            case MOB:
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
                    GuiSkyBlockData.drawEntityOnScreen(this.left + 42 * this.res.getScaleFactor(), top + 80, 20 * this.res.getScaleFactor(), zombie);
                }
                else if (stat.getText().equals("Spider"))
                {
                    EntitySpider spider = new EntitySpider(this.parent.mc.theWorld);
                    EntityCaveSpider cave = new EntityCaveSpider(this.parent.mc.theWorld);
                    GuiSkyBlockData.drawEntityOnScreen(this.left + 42 * this.res.getScaleFactor(), top + 60, 20 * this.res.getScaleFactor(), cave);
                    GuiSkyBlockData.drawEntityOnScreen(this.left + 42 * this.res.getScaleFactor(), top + 80, 20 * this.res.getScaleFactor(), spider);
                    GlStateManager.blendFunc(770, 771);
                }
                else
                {
                    EntityWolf wolf = new EntityWolf(this.parent.mc.theWorld);
                    wolf.setAngry(true);
                    GuiSkyBlockData.drawEntityOnScreen(this.left + 42 * this.res.getScaleFactor(), top + 80, 20 * this.res.getScaleFactor(), wolf);
                }
                break;
            case LEVEL:
                this.parent.mc.getTextureManager().bindTexture(XP_BARS);
                GlStateManager.color(0.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableBlend();

                String[] xpSplit = stat.getText().split(",");
                int slayerLevel = Integer.valueOf(xpSplit[0]);
                int playerSlayerXp = Integer.valueOf(xpSplit[1]);
                int maxSlayerXp = Integer.valueOf(xpSplit[2]);
                String text = "Level: " + slayerLevel + "/EXP: " + FORMAT.format(playerSlayerXp);

                short barWidth = 91;
                int filled = Math.min((int)Math.floor(playerSlayerXp * (barWidth + 1) / maxSlayerXp / 4), 91);//TODO Correct way to calculate exp to lvl, lvl to exp
                Gui.drawModalRectWithCustomSizedTexture(this.right - 150, top, 0, 0, barWidth, 5, barWidth, 10);

                if (filled > 0)
                {
                    Gui.drawModalRectWithCustomSizedTexture(this.right - 150, top, 0, 5, filled, 5, barWidth, 10);
                }

                GlStateManager.enableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.parent.drawString(this.parent.mc.fontRendererObj, text, this.right - this.parent.mc.fontRendererObj.getStringWidth(text) - 60, top - 16, 16777215);
                break;
            default:
                if (this.getSize() <= 3)
                {
                    this.parent.drawString(this.parent.mc.fontRendererObj, stat.getText(), this.left + 8, top, 16777215);
                }
                else
                {
                    this.parent.drawString(this.parent.mc.fontRendererObj, stat.getText(), this.right - this.parent.mc.fontRendererObj.getStringWidth(stat.getText()) - 60, top, 16777215);
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
}
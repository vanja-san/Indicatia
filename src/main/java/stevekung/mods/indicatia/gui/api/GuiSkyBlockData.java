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

import com.google.gson.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
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
    private static final String[] REVENANT_HORROR_HEAD = new String[] {"0862e0b0-a14f-3f93-894f-013502936b59", "eyJ0aW1lc3RhbXAiOjE1Njg0NTc0MjAxMzcsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RiYWQ5OWVkM2M4MjBiNzk3ODE5MGFkMDhhOTM0YTY4ZGZhOTBkOTk4NjgyNWRhMWM5N2Y2ZjIxZjQ5YWQ2MjYifX19"};
    private static final ResourceLocation XP_BARS = new ResourceLocation("indicatia:textures/gui/skill_xp_bar.png");
    private boolean loadingApi = true;
    private static InventoryBasic TEMP_INVENTORY = new InventoryBasic("tmp", true, 512);
    private static final int COLUMNS = 64;
    private GuiButton doneButton;
    private GuiButton backButton;
    private final List<SkyBlockFallbackData> profiles;
    private final String sbProfileId;
    private final String sbProfileName;
    private final String uuid;
    private final String username;
    private final List<SkyBlockInfo> infoList = new CopyOnWriteArrayList<>();
    private final List<SkyBlockSlayerInfo> slayerInfo = new CopyOnWriteArrayList<>();
    private static final int MAX_FAIRY_SOULS = 190;
    private final StopWatch watch = new StopWatch();
    private GuiScrollingList currentSlot;
    private List<SkyBlockStats> sbKillStats = new ArrayList<>();
    private List<SkyBlockStats> sbDeathStats = new ArrayList<>();
    private List<SkyBlockStats> sbOtherStats = new ArrayList<>();
    private int selectedView = -1;
    private int selectedOtherStatsView = -1;
    private boolean resize;
    private int percent;

    public GuiSkyBlockData(List<SkyBlockFallbackData> profiles, String sbProfileId, String sbProfileName, String username, String uuid)
    {
        //        super(new GuiSkyBlockData.ContainerSkyBlock());
        this.profiles = profiles;
        this.sbProfileId = sbProfileId;
        this.sbProfileName = sbProfileName;
        this.username = username;
        this.uuid = uuid;
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
                //            this.watch.start();

                try
                {
                    this.getProfileData();
                }
                catch (IOException | JsonSyntaxException | JsonIOException e)
                {
                    e.printStackTrace();
                    this.loadingApi = false;
                }
                //            this.watch.stop();
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

        this.resize = false;
        this.currentSlot = null;
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
    }

    @Override
    public void onResize(Minecraft mc, int width, int height)
    {
        //        this.watch.reset();
        this.resize = true;

        if (this.selectedView != -1)
        {
            if (this.selectedView == ViewButton.OTHER_STATS.id)
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

    private void getProfileData() throws IOException
    {
        URL url = new URL(SkyBlockAPIUtils.SKYBLOCK_PROFILE + this.sbProfileId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), StandardCharsets.UTF_8));
        JsonElement element = new JsonParser().parse(reader);
        JsonObject obj = element.getAsJsonObject();

        LoggerIN.info("");
        LoggerIN.info("SkyBlockData: {}", element);

        JsonObject profiles = obj.get("profile").getAsJsonObject().get("members").getAsJsonObject();
        JsonElement banking = obj.get("profile").getAsJsonObject().get("banking");

        for (Map.Entry<String, JsonElement> entry : profiles.entrySet())
        {
            String userUUID = entry.getKey();

            if (userUUID.equals(this.uuid))
            {
                DecimalFormat format = new DecimalFormat("#,###,###,###,###");

                LoggerIN.info("Name: {}, UserInProfileUUID: {}", this.username, userUUID);

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
                    this.infoList.add(new SkyBlockInfo("Banking Account", format.format(balance)));
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



                //                ContainerSkyBlock container = (ContainerSkyBlock)this.inventorySlots;
                //                container.itemList.clear();
                //
                //                container.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentUserProfile, "inv_armor"));
                //                container.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentUserProfile, "inv_contents"));
                //                container.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentUserProfile, "ender_chest_contents"));
                //                container.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentUserProfile, "talisman_bag"));
                //                container.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentUserProfile, "potion_bag"));
                //                container.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentUserProfile, "fishing_bag"));
                //                container.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentUserProfile, "quiver"));
                //                container.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentUserProfile, "candy_inventory_contents"));
                //
                //                for (int k = 0; k < GuiSkyBlockData.COLUMNS; ++k)
                //                {
                //                    for (int l = 0; l < 9; ++l)
                //                    {
                //                        int i1 = l + k * 9;
                //
                //                        if (i1 >= 0 && i1 < container.itemList.size())
                //                        {
                //                            TEMP_INVENTORY.setInventorySlotContents(l + k * 9, container.itemList.get(i1));
                //                        }
                //                    }
                //                }
                //                LoggerIN.info("UserProfileData: {}", currentUserProfile);
                break;
            }
        }
        this.loadingApi = false;
    }

    private String replaceStatsString(String statName, String replace)
    {
        String original = statName.replace(replace + "_", "").replace("_", " ");
        return original.equals(replace) ? "Total " + replace : WordUtils.capitalize(original) + " " + replace;
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
            list.add(SkyBlockSlayerInfo.createXp(xp.getAsInt()));

            for (int i = 1; i <= 4; i++)
            {
                JsonElement kills = slayer.getAsJsonObject().get("boss_kills_tier_" + (i - 1));
                list.add(new SkyBlockSlayerInfo(name + " Tier " + i + ": " + (kills != null ? kills.getAsInt() : 0) + " kills"));
            }
            for (int i = 0; i < 2; i++)
            {
                list.add(SkyBlockSlayerInfo.empty());
            }
            return list;
        }
        else
        {
            return Collections.singletonList(new SkyBlockSlayerInfo("Slayer Info: This player doesn't start " + name.toLowerCase() + " slayer yet!"));
        }
    }

    @Override
    public void onGuiClosed()
    {
        TEMP_INVENTORY.clear();
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

            if (type.id == ViewButton.OTHER_STATS.id)
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
            else if (type.id == ViewButton.SLAYERS.id)
            {
                this.currentSlot = new SlayerStats(this, this.width - 119, this.height, 44, this.height - 30, 59, 16, this.width, this.height, this.slayerInfo);
                this.hideOtherStatsButton();
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
        super.mouseClicked(mouseX, mouseY, mouseButton);
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
            super.drawScreen(mouseX, mouseY, partialTicks);
        }
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

    static class ContainerSkyBlock extends Container
    {
        public List<ItemStack> itemList = new ArrayList<>();

        public ContainerSkyBlock()
        {
            for (int i = 0; i < GuiSkyBlockData.COLUMNS; ++i)
            {
                for (int j = 0; j < 9; ++j)
                {
                    this.addSlotToContainer(new Slot(GuiSkyBlockData.TEMP_INVENTORY, i * 9 + j, 120 + j * 18, 1 + i * 18));
                }
            }
        }

        @Override
        public boolean canInteractWith(EntityPlayer playerIn)
        {
            return false;
        }

        @Override
        protected void retrySlotClick(int slotId, int clickedButton, boolean mode, EntityPlayer playerIn) {}

        @Override
        public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
        {
            return null;
        }

        @Override
        public boolean canMergeSlot(ItemStack stack, Slot p_94530_2_)
        {
            return false;
        }

        @Override
        public boolean canDragIntoSlot(Slot p_94531_1_)
        {
            return false;
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
        private String text;
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

        public static SkyBlockSlayerInfo createXp(int xp)
        {
            return new SkyBlockSlayerInfo(String.valueOf(xp), Type.LEVEL);
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

    class SlayerStats extends GuiScrollingList
    {
        private final List<SkyBlockSlayerInfo> stats;
        private final GuiScreen parent;

        public SlayerStats(GuiScreen parent, int width, int height, int top, int bottom, int left, int entryHeight, int parentWidth, int parentHeight, List<SkyBlockSlayerInfo> stats)
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
                this.setHeaderInfo(true, 24);
            }
        }

        @Override
        protected int getSize()
        {
            return this.stats.size();
        }

        @Override
        protected int getContentHeight()
        {
            return this.getSize() * 15 + 30;
        }

        @Override
        protected void drawSlot(int index, int right, int top, int height, Tessellator tess)
        {
            SkyBlockSlayerInfo stat = this.stats.get(index);
            ScaledResolution res = new ScaledResolution(this.parent.mc);

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
                    GuiSkyBlockData.drawEntityOnScreen(this.left + 42 * res.getScaleFactor(), top + 80, 20 * res.getScaleFactor(), zombie);
                }
                else if (stat.getText().equals("Spider"))
                {
                    EntitySpider spider = new EntitySpider(this.parent.mc.theWorld);
                    EntityCaveSpider cave = new EntityCaveSpider(this.parent.mc.theWorld);
                    GuiSkyBlockData.drawEntityOnScreen(this.left + 42 * res.getScaleFactor(), top + 60, 20 * res.getScaleFactor(), cave);
                    GuiSkyBlockData.drawEntityOnScreen(this.left + 42 * res.getScaleFactor(), top + 80, 20 * res.getScaleFactor(), spider);
                }
                else
                {
                    EntityWolf wolf = new EntityWolf(this.parent.mc.theWorld);
                    wolf.setAngry(true);
                    GlStateManager.disableBlend();
                    GuiSkyBlockData.drawEntityOnScreen(this.left + 42 * res.getScaleFactor(), top + 80, 20 * res.getScaleFactor(), wolf);
                    GlStateManager.enableBlend();
                }
                break;
            case LEVEL:
                this.parent.mc.getTextureManager().bindTexture(XP_BARS);
                GlStateManager.color(0.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableBlend();

                int cap = this.parent.mc.thePlayer.xpBarCap();
                int xp = Integer.valueOf(stat.getText());

                if (cap > 0)
                {
                    short barWidth = 91;
                    int filled = (int)(xp * (float)(barWidth + 1));
                    Gui.drawModalRectWithCustomSizedTexture(this.right - 150, top, 0, 0, barWidth, 5, barWidth, 10);

                    if (filled > 0)
                    {
                        Gui.drawModalRectWithCustomSizedTexture(this.right - 150, top, 0, 5, filled, 5, barWidth, 10);
                    }
                }
                GlStateManager.enableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.parent.drawString(this.parent.mc.fontRendererObj, stat.getText(), this.right - this.parent.mc.fontRendererObj.getStringWidth(stat.getText()) - 60, top - 16, 16777215);
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
        protected void drawScreen(int mouseX, int mouseY)
        {

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

    class OtherStats extends GuiScrollingList
    {
        private final List<SkyBlockStats> stats;
        private final GuiScreen parent;

        public OtherStats(GuiScreen parent, int width, int height, int top, int bottom, int left, int entryHeight, int parentWidth, int parentHeight, List<SkyBlockStats> stats, SkyBlockStats.Type type)
        {
            super(parent.mc, width, height, top, bottom, left, entryHeight, parentWidth, parentHeight);
            stats.sort((stat1, stat2) -> type == SkyBlockStats.Type.OTHERS ? new CompareToBuilder().append(stat1.getName(), stat2.getName()).build() : new CompareToBuilder().append(stat2.getValue(), stat1.getValue()).build());
            this.stats = stats;
            this.parent = parent;
        }

        @Override
        protected int getSize()
        {
            return this.stats.size();
        }

        @Override
        protected int getContentHeight()
        {
            return this.getSize() * 12;
        }

        @Override
        protected void drawSlot(int index, int right, int top, int height, Tessellator tess)
        {
            SkyBlockStats stat = this.stats.get(index);
            this.parent.drawString(this.parent.mc.fontRendererObj, stat.getName(), this.left + 3, top, index % 2 == 0 ? 16777215 : 9474192);
            this.parent.drawString(this.parent.mc.fontRendererObj, stat.getValueByString(), this.right - this.parent.mc.fontRendererObj.getStringWidth(stat.getValueByString()) - 10, top, index % 2 == 0 ? 16777215 : 9474192);
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
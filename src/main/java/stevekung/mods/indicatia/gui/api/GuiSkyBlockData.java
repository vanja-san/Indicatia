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

import org.apache.commons.lang3.time.StopWatch;

import com.google.gson.*;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import stevekung.mods.indicatia.utils.*;

public class GuiSkyBlockData extends GuiContainer
{
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
    private static final int MAX_FAIRY_SOULS = 190;
    private final StopWatch watch = new StopWatch();
    private int percent;

    public GuiSkyBlockData(List<SkyBlockFallbackData> profiles, String sbProfileId, String sbProfileName, String username, String uuid)
    {
        super(new GuiSkyBlockData.ContainerSkyBlock());
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

        CommonUtils.POOL.execute(() ->
        {
            this.watch.start();

            try
            {
                this.getProfileData();
            }
            catch (IOException | JsonSyntaxException | JsonIOException e)
            {
                e.printStackTrace();
                this.loadingApi = false;
            }
            this.watch.stop();
        });
        this.buttonList.add(this.doneButton = new GuiButton(0, this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, LangUtils.translate("gui.done")));
        this.buttonList.add(this.backButton = new GuiButton(1, this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, LangUtils.translate("gui.back")));
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
                    this.infoList.addAll(this.getSlayer(slayerBosses, format, "Zombie"));
                    this.infoList.addAll(this.getSlayer(slayerBosses, format, "Spider"));
                    this.infoList.addAll(this.getSlayer(slayerBosses, format, "Wolf"));
                }
                else
                {
                    this.infoList.add(new SkyBlockInfo(EnumChatFormatting.RED, EnumChatFormatting.DARK_RED, "Slayer Info", "Slayer data not available!"));
                }

                ContainerSkyBlock container = (ContainerSkyBlock)this.inventorySlots;
                container.itemList.clear();

                container.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentUserProfile, "inv_armor"));
                container.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentUserProfile, "inv_contents"));
                container.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentUserProfile, "ender_chest_contents"));
                container.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentUserProfile, "talisman_bag"));
                container.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentUserProfile, "potion_bag"));
                container.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentUserProfile, "fishing_bag"));
                container.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentUserProfile, "quiver"));
                container.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentUserProfile, "candy_inventory_contents"));

                for (int k = 0; k < GuiSkyBlockData.COLUMNS; ++k)
                {
                    for (int l = 0; l < 9; ++l)
                    {
                        int i1 = l + k * 9;

                        if (i1 >= 0 && i1 < container.itemList.size())
                        {
                            TEMP_INVENTORY.setInventorySlotContents(l + k * 9, container.itemList.get(i1));
                        }
                    }
                }
                LoggerIN.info("UserProfileData: {}", currentUserProfile);
                break;
            }
        }
        this.loadingApi = false;
    }

    private List<SkyBlockInfo> getSlayer(JsonElement element, DecimalFormat format, String name)
    {
        List<SkyBlockInfo> list = new ArrayList<>();
        JsonElement slayer = element.getAsJsonObject().get(name.toLowerCase());
        JsonElement xp = slayer.getAsJsonObject().get("xp");

        if (xp != null)
        {
            list.add(new SkyBlockInfo(EnumChatFormatting.GOLD, EnumChatFormatting.RED, name + " Slayer", format.format(xp.getAsInt())));

            for (int i = 1; i <= 4; i++)
            {
                JsonElement kills = slayer.getAsJsonObject().get("boss_kills_tier_" + (i - 1));

                if (kills != null)
                {
                    list.add(new SkyBlockInfo(EnumChatFormatting.GOLD, EnumChatFormatting.RED, name + " Tier " + i, kills.getAsInt() + " kills"));
                }
                else
                {
                    list.add(new SkyBlockInfo(EnumChatFormatting.GOLD, EnumChatFormatting.DARK_RED, name + " Tier " + i, "Not started yet!"));
                }
            }
            return list;
        }
        else
        {
            return Collections.singletonList(new SkyBlockInfo(EnumChatFormatting.GOLD, EnumChatFormatting.DARK_RED, "Slayer Info", "This player doesn't start " + name.toLowerCase() + " slayer yet!"));
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
        if (mouseButton == 0)
        {
            for (int i = 0; i < this.buttonList.size(); ++i)
            {
                GuiButton guibutton = this.buttonList.get(i);

                if (guibutton.mousePressed(this.mc, mouseX, mouseY))
                {
                    net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre event = new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre(this, guibutton, this.buttonList);
                    if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
                    {
                        break;
                    }
                    guibutton = event.button;
                    //                    this.selectedButton = guibutton;
                    guibutton.playPressSound(this.mc.getSoundHandler());
                    this.actionPerformed(guibutton);
                    if (this.equals(this.mc.currentScreen))
                    {
                        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post(this, event.button, this.buttonList));
                    }
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        if (this.loadingApi)
        {
            this.drawDefaultBackground();
            this.drawCenteredString(this.fontRendererObj, LangUtils.translate("Downloading SkyBlock stats"), this.width / 2, this.height / 2 - 20, 16777215);

            int i = this.width / 2 - 150;
            int j = this.width / 2 + 150;
            int k = this.height / 4 + 100;
            int l = k + 10;
            int j1 = MathHelper.floor_float(this.percent / 100.0F * (j - i));
            Gui.drawRect(i - 1, k - 1, j + 1, l + 1, -16777216);
            Gui.drawRect(i, k, i + j1, l, ColorUtils.to32BitColor(128, 85, 255, 85));
            this.drawCenteredString(this.fontRendererObj, this.percent + "%", this.width / 2, k + (l - k) / 2 - 9 / 2, 10526880);
        }
        else
        {
            super.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        this.drawCenteredString(this.fontRendererObj, "SkyBlock API Viewer", this.width / 2, 20, 16777215);
        this.drawCenteredString(this.fontRendererObj, EnumChatFormatting.GOLD + this.username + "'s Profile: " + this.sbProfileName, this.width / 2, 30, 16777215);

        int i = 0;

        for (SkyBlockInfo info : this.infoList)
        {
            String textInfo = info.toString();
            int fontHeight = this.mc.fontRendererObj.FONT_HEIGHT + 1;
            int yOffset = 50 + fontHeight * i;
            this.drawCenteredString(this.fontRendererObj, textInfo, this.width / 2, yOffset, 16777215);
            ++i;
        }
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
}
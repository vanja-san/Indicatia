package stevekung.mods.indicatia.gui.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.IProgressMeter;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import stevekung.mods.indicatia.utils.CommonUtils;
import stevekung.mods.indicatia.utils.LangUtils;
import stevekung.mods.indicatia.utils.LoggerIN;
import stevekung.mods.indicatia.utils.SkyBlockAPIUtils;

public class GuiSkyBlockData extends GuiContainer
{
    private boolean loadingApi = true;
    private static InventoryBasic TEMP_INVENTORY = new InventoryBasic("tmp", true, 512);
    private static final int COLUMNS = 64;
    private GuiButton doneButton;
    private GuiButton backButton;
    private final String sbProfileId;
    private final String uuid;
    private String username;

    public GuiSkyBlockData(String sbProfileId, String uuid)
    {
        super(new GuiSkyBlockData.ContainerSkyBlock());
        this.sbProfileId = sbProfileId;
        this.uuid = uuid;
    }

    @Override
    public void initGui()
    {
        this.buttonList.clear();

        CommonUtils.POOL.execute(() ->
        {
            try
            {
                this.getProfileData();
            }
            catch (IOException | JsonSyntaxException | JsonIOException e)
            {
                e.printStackTrace();
                this.loadingApi = false;
            }
        });
        this.buttonList.add(this.doneButton = new GuiButton(0, this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, LangUtils.translate("gui.done")));
        this.buttonList.add(this.backButton = new GuiButton(1, this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, LangUtils.translate("gui.back")));
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

        for (Map.Entry<String, JsonElement> entry : profiles.entrySet())
        {
            String userUUID = entry.getKey();// user uuid not skyblock profile uuid

            if (userUUID.equals(this.uuid))
            {
                System.out.println("equalll");
                String rawName = "";

                try (InputStream input = new URL("https://api.mojang.com/user/profiles/" + userUUID + "/names").openStream();)
                {
                    BufferedReader reader2 = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
                    JsonArray json = new JsonParser().parse(reader2).getAsJsonArray();
                    rawName = json.get(json.size() - 1).getAsJsonObject().get("name").getAsString();
                    this.username = rawName;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                LoggerIN.info("Name: {}, UserInProfileUUID: {}", rawName, userUUID);

                JsonObject currentUserProfile = profiles.get(userUUID).getAsJsonObject();

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

    @Override
    public void updateScreen()
    {
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
                this.mc.displayGuiScreen(new GuiSkyBlockProfileSelection(this.username));
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
            this.drawCenteredString(this.fontRendererObj, IProgressMeter.lanSearchStates[(int)(Minecraft.getSystemTime() / 150L % IProgressMeter.lanSearchStates.length)], this.width / 2, this.height / 2 + this.fontRendererObj.FONT_HEIGHT * 2 - 20, 16777215);
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
}
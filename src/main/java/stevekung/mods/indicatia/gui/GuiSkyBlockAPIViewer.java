package stevekung.mods.indicatia.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;
import com.google.gson.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stevekung.mods.indicatia.utils.CommonUtils;
import stevekung.mods.indicatia.utils.SkyBlockAPIUtils;

@SideOnly(Side.CLIENT)
public class GuiSkyBlockAPIViewer extends GuiContainer
{
    private static InventoryBasic field_147060_v = new InventoryBasic("tmp", true, 512);
    private static final int COLUMNS = 64;
    private GuiTextField usernameTextField;
    private GuiButton checkButton;
    private GuiButton cancelBtn;

    public GuiSkyBlockAPIViewer()
    {
        super(new GuiSkyBlockAPIViewer.ContainerCreative(Minecraft.getMinecraft().thePlayer));
    }

    @Override
    public void updateScreen()
    {
        this.usernameTextField.updateCursorCounter();
    }

    @Override
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.buttonList.add(this.checkButton = new GuiButton(0, this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, "Check"));
        this.buttonList.add(this.cancelBtn = new GuiButton(1, this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, I18n.format("gui.cancel", new Object[0])));
        this.usernameTextField = new GuiTextField(2, this.fontRendererObj, this.width / 2 - 75, 70, 150, 20);
        this.usernameTextField.setMaxStringLength(32767);
        this.usernameTextField.setFocused(true);
        this.checkButton.enabled = this.usernameTextField.getText().trim().length() > 0;
        field_147060_v.clear();
    }

    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 0)
            {
                field_147060_v.clear();
                
                CommonUtils.POOL.execute(() ->
                {
                    try
                    {
                        this.checkAPI(this.usernameTextField.getText());
                    }
                    catch (IOException | JsonSyntaxException | JsonIOException e)
                    {
                        e.printStackTrace();
                    }
                });
            }
            else if (button.id == 1)
            {
                this.mc.displayGuiScreen(null);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        this.usernameTextField.textboxKeyTyped(typedChar, keyCode);
        this.checkButton.enabled = this.usernameTextField.getText().trim().length() > 0;

        if (keyCode != 28 && keyCode != 156)
        {
            if (keyCode == 1)
            {
                this.actionPerformed(this.cancelBtn);
            }
        }
        else
        {
            this.actionPerformed(this.checkButton);
            this.usernameTextField.setFocused(false);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
//        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0)
        {
            for (int i = 0; i < this.buttonList.size(); ++i)
            {
                GuiButton guibutton = (GuiButton)this.buttonList.get(i);

                if (guibutton.mousePressed(this.mc, mouseX, mouseY))
                {
                    net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre event = new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre(this, guibutton, this.buttonList);
                    if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
                        break;
                    guibutton = event.button;
//                    this.selectedButton = guibutton;
                    guibutton.playPressSound(this.mc.getSoundHandler());
                    this.actionPerformed(guibutton);
                    if (this.equals(this.mc.currentScreen))
                        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post(this, event.button, this.buttonList));
                }
            }
        }
        
        this.usernameTextField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        this.drawCenteredString(this.fontRendererObj, "SkyBlock API Viewer", this.width / 2, 20, 16777215);
        this.drawString(this.fontRendererObj, "Enter Username", this.width / 2 - 75, 55, 10526880);
        this.usernameTextField.drawTextBox();
    }

    private void checkAPI(String username) throws IOException, JsonIOException, JsonSyntaxException
    {
        URL url = new URL("https://api.hypixel.net/player?key=cf1a95f9-82e5-4770-9e66-f9b9f1a613c5&name=" + username);
        URLConnection connection = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

        JsonElement element = new JsonParser().parse(in);
        JsonObject obj = element.getAsJsonObject();

        System.out.println("rawApi: " + element);

        JsonObject profiles = obj.get("player").getAsJsonObject().get("stats").getAsJsonObject().get("SkyBlock").getAsJsonObject().get("profiles").getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : profiles.entrySet())
        {
            String profileId = profiles.get(entry.getKey()).getAsJsonObject().get("profile_id").getAsString();
            String profileName = profiles.get(entry.getKey()).getAsJsonObject().get("cute_name").getAsString();

            System.out.println("profileId: " + profileId);
            System.out.println("profileName: " + profileName);

            this.getItem(profileId);
        }
    }

    private void getItem(String profileId) throws IOException, JsonIOException, JsonSyntaxException
    {
        URL url = new URL("https://api.hypixel.net/skyblock/profile?key=cf1a95f9-82e5-4770-9e66-f9b9f1a613c5&profile=" + profileId);
        URLConnection connection = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

        JsonElement element = new JsonParser().parse(in);
        JsonObject obj = element.getAsJsonObject();

        System.out.println("rawProfileApi: " + element);

        JsonObject profiles = obj.get("profile").getAsJsonObject().get("members").getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : profiles.entrySet())
        {
            JsonObject currentProfile = profiles.get(entry.getKey()).getAsJsonObject();
            System.out.println("rawCurrentProfile: " + currentProfile);

            ContainerCreative guicontainercreative$containercreative = (ContainerCreative)this.inventorySlots;
            guicontainercreative$containercreative.itemList.clear();

            guicontainercreative$containercreative.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentProfile, "inv_armor"));
            guicontainercreative$containercreative.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentProfile, "inv_contents"));
            guicontainercreative$containercreative.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentProfile, "ender_chest_contents"));
            guicontainercreative$containercreative.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentProfile, "talisman_bag"));
            guicontainercreative$containercreative.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentProfile, "potion_bag"));
            guicontainercreative$containercreative.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentProfile, "fishing_bag"));
            guicontainercreative$containercreative.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentProfile, "quiver"));
            guicontainercreative$containercreative.itemList.addAll(SkyBlockAPIUtils.decodeItem(currentProfile, "candy_inventory_contents"));

            for (int k = 0; k < GuiSkyBlockAPIViewer.COLUMNS; ++k)
            {
                for (int l = 0; l < 9; ++l)
                {
                    int i1 = l + k * 9;

                    if (i1 >= 0 && i1 < guicontainercreative$containercreative.itemList.size())
                    {
                        field_147060_v.setInventorySlotContents(l + k * 9, (ItemStack)guicontainercreative$containercreative.itemList.get(i1));
                    }
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    static class ContainerCreative extends Container
    {
        public List<ItemStack> itemList = Lists.<ItemStack>newArrayList();

        public ContainerCreative(EntityPlayer p_i1086_1_)
        {
            for (int i = 0; i < GuiSkyBlockAPIViewer.COLUMNS; ++i)
            {
                for (int j = 0; j < 9; ++j)
                {
                    this.addSlotToContainer(new Slot(GuiSkyBlockAPIViewer.field_147060_v, i * 9 + j, 120 + j * 18, 1 + i * 18));
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
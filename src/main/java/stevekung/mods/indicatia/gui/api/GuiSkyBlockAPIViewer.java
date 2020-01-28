package stevekung.mods.indicatia.gui.api;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.time.StopWatch;
import org.lwjgl.input.Keyboard;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StringUtils;
import stevekung.mods.indicatia.gui.GuiButtonSearch;
import stevekung.mods.indicatia.gui.GuiRightClickTextField;
import stevekung.mods.indicatia.gui.GuiSBProfileButton;
import stevekung.mods.indicatia.utils.*;

public class GuiSkyBlockAPIViewer extends GuiScreen implements GuiYesNoCallback
{
    private GuiRightClickTextField usernameTextField;
    private GuiButtonSearch checkButton;
    private GuiButton closeButton;
    private String username = "";
    private boolean openFromPlayer;
    private boolean loadingApi;
    private boolean error;
    private boolean showWeb;
    private String errorMessage;
    private String statusMessage;
    private List<ProfileDataCallback> profiles = new ArrayList<>();
    private final StopWatch watch = new StopWatch();
    private int percent;
    private List<GuiSBProfileButton> profileButtonList = new ArrayList<>();
    private final String skyblockStats = "https://sky.lea.moe/";
    private boolean fromError;

    public GuiSkyBlockAPIViewer(GuiState state)
    {
        this(state, "");
    }

    public GuiSkyBlockAPIViewer(GuiState state, String username)
    {
        this(state, username, null);
    }

    public GuiSkyBlockAPIViewer(GuiState state, String username, List<ProfileDataCallback> profiles)
    {
        if (state == GuiState.SEARCH)
        {
            this.profiles = profiles;
        }
        this.loadingApi = state == GuiState.PLAYER;
        this.openFromPlayer = state == GuiState.PLAYER;
        this.fromError = state == GuiState.ERROR;
        this.username = username;
    }

    @Override
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.buttonList.add(this.checkButton = new GuiButtonSearch(0, this.width / 2 + 78, 46));
        this.buttonList.add(this.closeButton = new GuiButton(1, this.width / 2 - 75, this.height / 4 + 152, 150, 20, LangUtils.translate("gui.close")));
        this.usernameTextField = new GuiRightClickTextField(2, this.fontRendererObj, this.width / 2 - 75, 45, 150, 20);
        this.usernameTextField.setMaxStringLength(32767);
        this.usernameTextField.setFocused(true);
        this.usernameTextField.setText(this.username);
        this.checkButton.enabled = this.usernameTextField.getText().trim().length() > 0;
        this.checkButton.visible = !this.error;

        if (this.error)
        {
            this.closeButton.displayString = LangUtils.translate("gui.back");
        }
        if (this.fromError)
        {
            this.usernameTextField.setText(this.username);
        }

        if (this.openFromPlayer)
        {
            CommonUtils.runAsync(() ->
            {
                try
                {
                    this.watch.start();
                    this.checkAPI();
                    this.watch.stop();
                    this.openFromPlayer = false;

                    if (this.watch.getTime() > 0)
                    {
                        LoggerIN.info("API Download finished in: {}ms", this.watch.getTime());
                    }
                }
                catch (Throwable e)
                {
                    this.setErrorMessage(e.getMessage());
                    e.printStackTrace();
                }
            });
        }
        if (!this.profiles.isEmpty())
        {
            int i = 0;
            List<GuiSBProfileButton> buttons = new ArrayList<>();

            for (ProfileDataCallback data : this.profiles)
            {
                GuiSBProfileButton button = new GuiSBProfileButton(i + 1000, this.width / 2 - 75, 75, 150, 20, data);
                buttons.add(button);
                ++i;
            }

            buttons.sort((button1, button2) -> new CompareToBuilder().append(button2.getLastSave(), button1.getLastSave()).build());

            int i2 = 0;

            for (GuiSBProfileButton button : buttons)
            {
                if (i2 == 0)
                {
                    button.displayString = EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + button.displayString;
                }
                button.yPosition += i2 * 22;
                button.setProfileList(this.profiles);
                this.profileButtonList.add(button);
                ++i2;
            }
        }
    }

    @Override
    public void updateScreen()
    {
        this.usernameTextField.updateCursorCounter();
        this.checkButton.enabled = this.usernameTextField.getText().trim().length() > 0;

        if (!this.watch.isStopped() && this.percent < 100)
        {
            this.percent = (int)(this.watch.getTime() * 100 / 10000L);
        }
        if (this.percent > 100)
        {
            this.percent = 100;
        }
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
                this.watch.reset();
                this.percent = 0;
                this.username = this.usernameTextField.getText();
                this.profiles.clear();
                this.profileButtonList.clear();
                this.loadingApi = true;

                CommonUtils.runAsync(() ->
                {
                    try
                    {
                        this.watch.start();
                        this.checkAPI();
                        this.watch.stop();

                        if (this.watch.getTime() > 0)
                        {
                            LoggerIN.info("API Download finished in: {}ms", this.watch.getTime());
                        }
                    }
                    catch (Throwable e)
                    {
                        this.setErrorMessage(e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
            else if (button.id == 1)
            {
                this.mc.displayGuiScreen(this.error ? new GuiSkyBlockAPIViewer(GuiState.ERROR, this.username) : null);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        this.usernameTextField.textboxKeyTyped(typedChar, keyCode);

        if (keyCode != 28 && keyCode != 156)
        {
            if (keyCode == 1)
            {
                this.actionPerformed(this.closeButton);
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
        if (!this.loadingApi)
        {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            this.usernameTextField.mouseClicked(mouseX, mouseY, mouseButton);

            if (mouseButton == 0)
            {
                for (GuiSBProfileButton button : this.profileButtonList)
                {
                    if (button.mousePressed(this.mc, mouseX, mouseY))
                    {
                        this.selectedButton = button;
                        button.playPressSound(this.mc.getSoundHandler());
                    }
                }

                if (this.showWeb)
                {
                    String url = "Click here to open SkyBlock Stats: " + this.skyblockStats;
                    int minX = this.width / 2 - this.fontRendererObj.getStringWidth(url) / 2 - 2;
                    int minY = 119;
                    int maxX = minX + this.fontRendererObj.getStringWidth(url) + 2;
                    int maxY = minY + this.fontRendererObj.FONT_HEIGHT + 1;

                    if (mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY)
                    {
                        this.mc.displayGuiScreen(new GuiConfirmOpenLink(this, this.skyblockStats, 500, false));
                    }
                }
            }
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
            this.drawCenteredString(this.fontRendererObj, "SkyBlock API Viewer", this.width / 2, 20, 16777215);

            if (this.error)
            {
                this.drawCenteredString(this.fontRendererObj, EnumChatFormatting.RED + this.errorMessage, this.width / 2, 100, 16777215);

                if (this.showWeb)
                {
                    String url = "Click here to open SkyBlock Stats: " + this.skyblockStats;
                    boolean hover = false;
                    int minX = this.width / 2 - this.fontRendererObj.getStringWidth(url) / 2 - 2;
                    int minY = 119;
                    int maxX = minX + this.fontRendererObj.getStringWidth(url) + 2;
                    int maxY = minY + this.fontRendererObj.FONT_HEIGHT + 1;

                    if (mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY)
                    {
                        hover = true;
                    }
                    Gui.drawRect(minX, minY, maxX, maxY, ColorUtils.to32BitColor(hover ? 128 : 60, 255, 255, 255));
                    this.drawCenteredString(this.fontRendererObj, EnumChatFormatting.YELLOW + url, this.width / 2, 120, 16777215);
                }
                super.drawScreen(mouseX, mouseY, partialTicks);
            }
            else
            {
                if (!this.profiles.isEmpty())
                {
                    this.drawCenteredString(this.fontRendererObj, EnumChatFormatting.GOLD + this.username + "'s Profile(s)", this.width / 2, 30, 16777215);
                }

                this.usernameTextField.drawTextBox();

                if (StringUtils.isNullOrEmpty(this.usernameTextField.getText()))
                {
                    this.drawString(this.fontRendererObj, "Enter username", this.width / 2 - 71, 51, 10526880);
                }

                for (GuiSBProfileButton button : this.profileButtonList)
                {
                    button.drawButton(this.mc, mouseX, mouseY);
                }

                super.drawScreen(mouseX, mouseY, partialTicks);

                for (GuiSBProfileButton button : this.profileButtonList)
                {
                    button.drawRegion(this.mc.displayWidth, mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height)
    {
        this.profileButtonList.clear();
        super.setWorldAndResolution(mc, width, height);
    }

    @Override
    public void confirmClicked(boolean result, int id)
    {
        if (id == 500)
        {
            if (result)
            {
                CommonUtils.openLink(this.skyblockStats);
            }
            this.mc.displayGuiScreen(this);
        }
    }

    private void checkAPI() throws IOException
    {
        if (!this.username.matches("\\w+"))
        {
            this.setErrorMessage("Invalid Username Pattern!");
            return;
        }

        this.statusMessage = "Getting Hypixel API";

        URL url = new URL(SkyBlockAPIUtils.PLAYER_NAME + this.username);
        JsonObject obj = new JsonParser().parse(IOUtils.toString(url.openConnection().getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();

        if (!obj.get("success").getAsBoolean())
        {
            this.setErrorMessage(obj.get("cause").getAsString());
            return;
        }

        JsonElement jsonPlayer = obj.get("player");

        if (jsonPlayer.isJsonNull())
        {
            this.setErrorMessage("Player not found!");
            return;
        }

        this.username = jsonPlayer.getAsJsonObject().get("displayname").getAsString();
        JsonElement jsonSkyBlock = jsonPlayer.getAsJsonObject().get("stats").getAsJsonObject().get("SkyBlock");

        if (jsonSkyBlock == null)
        {
            this.setErrorMessage("Player has not played SkyBlock yet!");
            return;
        }

        JsonObject profiles = jsonSkyBlock.getAsJsonObject().get("profiles").getAsJsonObject();
        int i = 0;

        if (profiles.entrySet().isEmpty())
        {
            this.setErrorMessage("Empty profile data! Please check on website instead", true);
            return;
        }

        this.statusMessage = "Getting SkyBlock profiles";
        List<GuiSBProfileButton> buttons = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : profiles.entrySet())
        {
            String sbProfileId = profiles.get(entry.getKey()).getAsJsonObject().get("profile_id").getAsString();
            String profileName = profiles.get(entry.getKey()).getAsJsonObject().get("cute_name").getAsString();
            String uuid = jsonPlayer.getAsJsonObject().get("uuid").getAsString();
            this.statusMessage = "Found " + EnumChatFormatting.GOLD + profileName + EnumChatFormatting.RESET + " profile";
            GameProfile profile = TileEntitySkull.updateGameprofile(new GameProfile(UUID.fromString(uuid.replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5")), this.username));
            ProfileDataCallback callback = new ProfileDataCallback(sbProfileId, profileName, this.username, uuid, profile, this.getLastSaveProfile(sbProfileId, uuid));
            GuiSBProfileButton button = new GuiSBProfileButton(i + 1000, this.width / 2 - 75, 75, 150, 20, callback);
            buttons.add(button);
            this.profiles.add(callback);
            ++i;
        }

        buttons.sort((button1, button2) -> new CompareToBuilder().append(button2.getLastSave(), button1.getLastSave()).build());

        int i2 = 0;

        for (GuiSBProfileButton button : buttons)
        {
            if (i2 == 0)
            {
                button.displayString = EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + button.displayString;
            }
            button.yPosition += i2 * 22;
            button.setProfileList(this.profiles);
            this.profileButtonList.add(button);
            ++i2;
        }
        this.usernameTextField.setText(this.username);
        this.loadingApi = false;
    }

    private long getLastSaveProfile(String currentProfileId, String uuid) throws IOException
    {
        long lastSave = -1;
        URL url = new URL(SkyBlockAPIUtils.SKYBLOCK_PROFILE + currentProfileId);
        JsonObject obj = new JsonParser().parse(IOUtils.toString(url.openConnection().getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
        JsonElement profile = obj.get("profile");
        JsonObject profiles = profile.getAsJsonObject().get("members").getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : profiles.entrySet().stream().filter(entry -> entry.getKey().equals(uuid)).collect(Collectors.toList()))
        {
            JsonObject currentUserProfile = profiles.get(entry.getKey()).getAsJsonObject();
            JsonElement lastSaveJson = currentUserProfile.get("last_save");

            if (lastSaveJson != null)
            {
                lastSave = lastSaveJson.getAsLong();
            }
        }
        return lastSave;
    }

    private void setErrorMessage(String message)
    {
        this.setErrorMessage(message, false);
    }

    private void setErrorMessage(String message, boolean showWeb)
    {
        this.error = true;
        this.loadingApi = false;
        this.showWeb = showWeb;
        this.errorMessage = message;
        this.checkButton.visible = !this.error;
        this.closeButton.displayString = LangUtils.translate("gui.back");
    }

    public enum GuiState
    {
        EMPTY, ERROR, PLAYER, SEARCH;
    }
}
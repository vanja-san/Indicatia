package stevekung.mods.indicatia.gui.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.time.StopWatch;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import stevekung.mods.indicatia.gui.GuiSBProfileButton;
import stevekung.mods.indicatia.utils.ColorUtils;
import stevekung.mods.indicatia.utils.CommonUtils;
import stevekung.mods.indicatia.utils.LangUtils;
import stevekung.mods.indicatia.utils.SkyBlockAPIUtils;

public class GuiSkyBlockProfileSelection extends GuiScreen
{
    private boolean loadingApi = true;
    private boolean error = false;
    private String errorMessage;
    private GuiButton doneButton;
    private GuiButton backButton;
    private String username;
    private List<SkyBlockFallbackData> profiles = new ArrayList<>();
    private final StopWatch watch = new StopWatch();
    private int percent;

    public GuiSkyBlockProfileSelection(String username)
    {
        this.username = username;
    }

    public GuiSkyBlockProfileSelection(String username, List<SkyBlockFallbackData> profiles)
    {
        this.username = username;
        this.profiles = profiles;
        this.loadingApi = false;
    }

    @Override
    public void initGui()
    {
        this.buttonList.clear();
        this.buttonList.add(this.doneButton = new GuiButton(0, this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, LangUtils.translate("gui.done")));
        this.buttonList.add(this.backButton = new GuiButton(1, this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, LangUtils.translate("gui.back")));

        if (!this.profiles.isEmpty())
        {
            int i = 0;

            for (SkyBlockFallbackData data : this.profiles)
            {
                String sbProfileId = data.getProfileId();
                String profileName = data.getProfileName();
                String uuid = data.getUUID();
                GameProfile gameProfile = data.getGameProfile();
                GuiSBProfileButton button = new GuiSBProfileButton(i + 1000, this.width / 2 - 75, 50, 150, 20, profileName, sbProfileId, this.username, uuid, gameProfile);
                button.yPosition += i * 22;
                this.buttonList.add(button);
                ++i;
            }
            for (GuiButton button : this.buttonList)
            {
                if (button instanceof GuiSBProfileButton)
                {
                    ((GuiSBProfileButton)button).setProfileList(this.profiles);
                }
            }
        }
        else
        {
            CommonUtils.POOL.execute(() ->
            {
                this.watch.start();

                try
                {
                    this.checkAPI();
                }
                catch (IOException | JsonSyntaxException | JsonIOException e)
                {
                    e.printStackTrace();
                    this.loadingApi = false;
                }
                this.watch.stop();
            });
        }
    }

    @Override
    public void updateScreen()
    {
        if (!this.watch.isStopped() && this.percent < 100)
        {
            this.percent = (int)(this.watch.getTime() * 100 / 1500);
        }
        if (this.percent > 100)
        {
            this.percent = 100;
        }
    }

    @Override
    public void onResize(Minecraft mc, int width, int height)
    {
        this.watch.reset();
        super.onResize(mc, width, height);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (!this.loadingApi)
        {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 0)
            {
                this.mc.displayGuiScreen(this.error ? new GuiSkyBlockAPIViewer(this.username) : null);
            }
            else if (button.id == 1)
            {
                this.mc.displayGuiScreen(new GuiSkyBlockAPIViewer(this.username));
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
        }
        else
        {
            this.drawCenteredString(this.fontRendererObj, "SkyBlock API Viewer", this.width / 2, 20, 16777215);
            this.drawCenteredString(this.fontRendererObj, EnumChatFormatting.GOLD + this.username + "'s Profile(s)", this.width / 2, 30, 16777215);

            if (this.error)
            {
                this.drawCenteredString(this.fontRendererObj, EnumChatFormatting.RED + this.errorMessage, this.width / 2, 100, 16777215);
            }
            super.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    private void checkAPI() throws IOException
    {
        if (!this.username.matches("\\w+"))
        {
            this.setErrorMessage("Invalid Username Pattern!");
            return;
        }

        URL url = new URL(SkyBlockAPIUtils.PLAYER_NAME + this.username);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), StandardCharsets.UTF_8));
        JsonElement element = new JsonParser().parse(reader);
        JsonObject obj = element.getAsJsonObject();
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
            this.setErrorMessage("Empty profile data! Please check to this website instead\nhttps://sky.lea.moe/");//TODO Split string + click handler
            return;
        }

        for (Map.Entry<String, JsonElement> entry : profiles.entrySet())
        {
            String sbProfileId = profiles.get(entry.getKey()).getAsJsonObject().get("profile_id").getAsString();
            String profileName = profiles.get(entry.getKey()).getAsJsonObject().get("cute_name").getAsString();
            String uuid = jsonPlayer.getAsJsonObject().get("uuid").getAsString();
            GameProfile profile = TileEntitySkull.updateGameprofile(new GameProfile(UUID.fromString(uuid.replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5")), this.username));
            GuiSBProfileButton button = new GuiSBProfileButton(i + 1000, this.width / 2 - 75, 50, 150, 20, profileName, sbProfileId, this.username, uuid, profile);
            button.yPosition += i * 22;
            this.buttonList.add(button);
            this.profiles.add(new SkyBlockFallbackData(sbProfileId, profileName, uuid, profile));
            ++i;
        }
        for (GuiButton button : this.buttonList)
        {
            if (button instanceof GuiSBProfileButton)
            {
                ((GuiSBProfileButton)button).setProfileList(this.profiles);
            }
        }
        this.loadingApi = false;
    }

    private void setErrorMessage(String message)
    {
        this.error = true;
        this.loadingApi = false;
        this.errorMessage = message;
        this.backButton.visible = false;
        this.doneButton.xPosition = this.width / 2 - 75;
        this.doneButton.yPosition = this.height / 4 + 132;
    }
}
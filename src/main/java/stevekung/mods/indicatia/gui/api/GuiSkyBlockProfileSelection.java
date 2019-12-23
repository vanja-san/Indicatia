package stevekung.mods.indicatia.gui.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.gson.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.IProgressMeter;
import net.minecraft.util.EnumChatFormatting;
import stevekung.mods.indicatia.gui.GuiSBProfileButton;
import stevekung.mods.indicatia.utils.CommonUtils;
import stevekung.mods.indicatia.utils.LangUtils;
import stevekung.mods.indicatia.utils.LoggerIN;
import stevekung.mods.indicatia.utils.SkyBlockAPIUtils;

public class GuiSkyBlockProfileSelection extends GuiScreen
{
    private boolean loadingApi = true;
    private boolean error = false;
    private String errorMessage;
    private GuiButton doneButton;
    private GuiButton backButton;
    private final String username;

    public GuiSkyBlockProfileSelection(String username)
    {
        this.username = username;
    }

    @Override
    public void initGui()
    {
        this.buttonList.clear();
        this.buttonList.add(this.doneButton = new GuiButton(0, this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, LangUtils.translate("gui.done")));
        this.buttonList.add(this.backButton = new GuiButton(1, this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, LangUtils.translate("gui.back")));

        CommonUtils.POOL.execute(() ->
        {
            try
            {
                this.checkAPI();
            }
            catch (IOException | JsonSyntaxException | JsonIOException e)
            {
                e.printStackTrace();
                this.loadingApi = false;
            }
        });
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
                this.mc.displayGuiScreen(this.error ? new GuiSkyBlockAPIViewer() : null);
            }
            else if (button.id == 1)
            {
                this.mc.displayGuiScreen(new GuiSkyBlockAPIViewer());
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
            this.drawCenteredString(this.fontRendererObj, IProgressMeter.lanSearchStates[(int)(Minecraft.getSystemTime() / 150L % IProgressMeter.lanSearchStates.length)], this.width / 2, this.height / 2 + this.fontRendererObj.FONT_HEIGHT * 2 - 20, 16777215);
        }
        else
        {
            this.drawCenteredString(this.fontRendererObj, "SkyBlock API Viewer", this.width / 2, 20, 16777215);

            if (this.error)
            {
                this.drawCenteredString(this.fontRendererObj, EnumChatFormatting.RED + this.errorMessage, this.width / 2, 100, 16777215);
            }
            super.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    private void checkAPI() throws IOException
    {
        URL url = new URL(SkyBlockAPIUtils.PLAYER_NAME + this.username);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), StandardCharsets.UTF_8));
        JsonElement element = new JsonParser().parse(reader);
        JsonObject obj = element.getAsJsonObject();
        JsonElement jsonPlayer = obj.get("player");

        LoggerIN.info("API: {}", element);

        if (jsonPlayer.isJsonNull())
        {
            this.setErrorMessage("Player not found!");
            return;
        }

        JsonElement jsonSkyBlock = jsonPlayer.getAsJsonObject().get("stats").getAsJsonObject().get("SkyBlock");

        if (jsonSkyBlock == null)
        {
            this.setErrorMessage("Player has not played SkyBlock yet!");
            return;
        }

        JsonObject profiles = jsonSkyBlock.getAsJsonObject().get("profiles").getAsJsonObject();
        int i = 0;

        for (Map.Entry<String, JsonElement> entry : profiles.entrySet())
        {
            String sbProfileId = profiles.get(entry.getKey()).getAsJsonObject().get("profile_id").getAsString();
            String profileName = profiles.get(entry.getKey()).getAsJsonObject().get("cute_name").getAsString();
            GuiSBProfileButton button = new GuiSBProfileButton(i + 1000, this.width / 2 - 75, 50, 150, 20, profileName, sbProfileId, jsonPlayer.getAsJsonObject().get("uuid").getAsString());
            button.yPosition += i * 22;
            this.buttonList.add(button);
            ++i;
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
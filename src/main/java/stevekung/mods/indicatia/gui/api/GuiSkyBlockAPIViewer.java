package stevekung.mods.indicatia.gui.api;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import stevekung.mods.indicatia.gui.GuiRightClickTextField;
import stevekung.mods.indicatia.utils.LangUtils;

public class GuiSkyBlockAPIViewer extends GuiScreen
{
    private GuiRightClickTextField usernameTextField;
    private GuiButton checkButton;
    private GuiButton cancelButton;
    private String username = "";

    public GuiSkyBlockAPIViewer() {}

    public GuiSkyBlockAPIViewer(String username)
    {
        this.username = username;
    }

    @Override
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.buttonList.add(this.checkButton = new GuiButton(0, this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, "Check"));
        this.buttonList.add(this.cancelButton = new GuiButton(1, this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, LangUtils.translate("gui.cancel")));
        this.usernameTextField = new GuiRightClickTextField(2, this.fontRendererObj, this.width / 2 - 75, 70, 150, 20);
        this.usernameTextField.setMaxStringLength(32767);
        this.usernameTextField.setFocused(true);
        this.usernameTextField.setText(this.username);
        this.checkButton.enabled = this.usernameTextField.getText().trim().length() > 0;
    }

    @Override
    public void updateScreen()
    {
        this.usernameTextField.updateCursorCounter();
        this.checkButton.enabled = this.usernameTextField.getText().trim().length() > 0;
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
                this.mc.displayGuiScreen(new GuiSkyBlockProfileSelection(this.usernameTextField.getText()));
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

        if (keyCode != 28 && keyCode != 156)
        {
            if (keyCode == 1)
            {
                this.actionPerformed(this.cancelButton);
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
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.usernameTextField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "SkyBlock API Viewer", this.width / 2, 20, 16777215);
        this.drawString(this.fontRendererObj, "Enter Username", this.width / 2 - 75, 55, 10526880);
        this.usernameTextField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
package stevekung.mods.indicatia.gui.screen;

import java.util.Collections;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.gui.widget.CustomizedTextureButton;
import stevekung.mods.stevekungslib.utils.GameProfileUtils;
import stevekung.mods.stevekungslib.utils.JsonUtils;
import stevekung.mods.stevekungslib.utils.LangUtils;

@OnlyIn(Dist.CLIENT)
public class AutoLoginFunctionScreen extends Screen
{
    private TextFieldWidget inputField;
    private CustomizedTextureButton helpBtn;
    private ServerData data;

    public AutoLoginFunctionScreen()
    {
        super(JsonUtils.create("Auto Login Function"));
        this.data = Minecraft.getInstance().getCurrentServerData();
    }

    @Override
    public void init()
    {
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.inputField = new TextFieldWidget(this.font, this.width / 2 - 150, this.height / 4 + 65, 300, 20, "Auto Login Input");
        this.inputField.setMaxStringLength(32767);
        this.inputField.setFocused(true);
        this.inputField.setCanLoseFocus(true);
        this.addButton(new Button(this.width / 2 - 152, this.height / 4 + 100, 150, 20, LangUtils.translate("gui.done"), button ->
        {
            if (this.data != null)
            {
                this.minecraft.player.sendMessage(JsonUtils.create(LangUtils.translate("commands.auto_login.function_set")));
                ExtendedConfig.loginData.removeAutoLogin(GameProfileUtils.getUUID() + this.data.serverIP);
                ExtendedConfig.loginData.addAutoLogin(this.data.serverIP, "", "", GameProfileUtils.getUUID(), this.inputField.getText());
                ExtendedConfig.instance.save();
            }
            this.minecraft.displayGuiScreen(null);
        }));
        this.addButton(new Button(this.width / 2 + 2, this.height / 4 + 100, 150, 20, LangUtils.translate("gui.cancel"), button ->
        {
            this.minecraft.displayGuiScreen(null);
        }));
        this.addButton(this.helpBtn = new CustomizedTextureButton(this.width / 2 + 130, this.height / 4 + 35, this, Collections.singletonList(LangUtils.translate("menu.help")), "help", button ->
        {
            this.minecraft.displayGuiScreen(new AutoLoginFunctionHelpScreen(true));
        }));

        if (this.data != null)
        {
            ExtendedConfig.loginData.getAutoLoginList().forEach(login ->
            {
                if (this.data.serverIP.equalsIgnoreCase(login.getServerIP()) && GameProfileUtils.getUUID().equals(login.getUUID()) && !login.getFunction().isEmpty())
                {
                    this.inputField.setText(login.getFunction());
                }
            });
        }
        this.children.add(this.inputField);
    }

    @Override
    public void tick()
    {
        this.inputField.tick();
    }

    @Override
    public void onClose()
    {
        this.minecraft.keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        this.inputField.mouseClicked(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.font, "Auto Login Function", this.width / 2, this.height / 4, 16777215);
        this.drawCenteredString(this.font, "Put your own bot function to make it run automatically", this.width / 2, this.height / 4 + 20, 10526880);
        this.inputField.render(mouseX, mouseY, partialTicks);
        this.helpBtn.drawRegion(mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}
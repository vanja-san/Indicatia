package stevekung.mods.indicatia.gui.optifine;

import java.math.BigInteger;
import java.util.Random;

import com.mojang.authlib.exceptions.InvalidCredentialsException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import stevekung.mods.indicatia.utils.CommonUtils;
import stevekung.mods.indicatia.utils.LangUtils;
import stevekung.mods.indicatia.utils.LoggerIN;

public class GuiScreenCapeOF extends GuiScreenOF
{
    public final GuiScreen parentScreen;
    public String title;
    public String message;
    public long messageHideTimeMs;

    public GuiScreenCapeOF(GuiScreen parentScreenIn)
    {
        this.parentScreen = parentScreenIn;
    }

    @Override
    public void initGui()
    {
        int i = 0;
        this.title = I18n.format("of.options.capeOF.title", new Object[0]);

        i += 2;

        this.buttonList.add(new GuiButtonOF(210, this.width / 2 - 155, this.height / 6 + 24 * (i >> 1), 150, 20, I18n.format("of.options.capeOF.openEditor", new Object[0])));
        this.buttonList.add(new GuiButtonOF(220, this.width / 2 - 155 + 160, this.height / 6 + 24 * (i >> 1), 150, 20, I18n.format("of.options.capeOF.reloadCape", new Object[0])));

        i += 8;

        this.buttonList.add(new GuiButtonOF(200, this.width / 2 - 100, this.height / 6 + 24 * (i >> 1), I18n.format("gui.done", new Object[0])));
    }

    @Override
    public void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
            if (button.id == 200)
            {
                this.mc.displayGuiScreen(this.parentScreen);
            }
            if (button.id == 210)
            {
                try
                {
                    String userName = this.mc.getSession().getProfile().getName();
                    String userId = this.mc.getSession().getProfile().getId().toString().replace("-", "");

                    String accessToken = this.mc.getSession().getToken();

                    Random r1 = new Random();
                    Random r2 = new Random(System.identityHashCode(new Object()));
                    BigInteger random1Bi = new BigInteger(128, r1);
                    BigInteger random2Bi = new BigInteger(128, r2);

                    BigInteger serverBi = random1Bi.xor(random2Bi);
                    String serverId = serverBi.toString(16);

                    this.mc.getSessionService().joinServer(this.mc.getSession().getProfile(), accessToken, serverId);

                    String urlStr = "https://optifine.net/capeChange?u=" + userId + "&n=" + userName + "&s=" + serverId;
                    CommonUtils.openLink(urlStr);

                    this.showMessage(LangUtils.translate("of.message.capeOF.openEditor"), 10000L);
                }
                catch (InvalidCredentialsException e)
                {
                    this.showGuiMessage(I18n.format("of.message.capeOF.error1", new Object[0]), I18n.format("of.message.capeOF.error2", new Object[] { e.getMessage() }));
                    LoggerIN.warning("Mojang authentication failed");
                    LoggerIN.warning(e.getClass().getName() + ": " + e.getMessage());
                }
                catch (Exception e)
                {
                    LoggerIN.warning("Error opening OptiFine cape link");
                    LoggerIN.warning(e.getClass().getName() + ": " + e.getMessage());
                }
            }
            if (button.id == 220)
            {
                this.showMessage(LangUtils.translate("of.message.capeOF.reloadCape"), 15000L);

                if (this.mc.thePlayer != null)
                {
                    long delayMs = 15000L;
                    long reloadTimeMs = System.currentTimeMillis() + delayMs;
                    ((ICapeReload)this.mc.thePlayer).setReloadCapeTimeMs(reloadTimeMs);
                }
            }
        }
    }

    public void showMessage(String msg, long timeMs)
    {
        this.message = msg;
        this.messageHideTimeMs = System.currentTimeMillis() + timeMs;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 20, 16777215);

        if (this.message != null)
        {
            this.drawCenteredString(this.fontRendererObj, this.message, this.width / 2, this.height / 6 + 60, 16777215);

            if (System.currentTimeMillis() > this.messageHideTimeMs)
            {
                this.message = null;
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void showGuiMessage(String line1, String line2)
    {
        GuiMessage gui = new GuiMessage(this.mc.currentScreen, line1, line2);
        this.mc.displayGuiScreen(gui);
    }
}
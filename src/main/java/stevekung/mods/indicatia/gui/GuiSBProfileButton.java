package stevekung.mods.indicatia.gui;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import stevekung.mods.indicatia.gui.api.GuiSkyBlockData;
import stevekung.mods.indicatia.gui.api.ProfileDataCallback;
import stevekung.mods.indicatia.utils.CommonUtils;

public class GuiSBProfileButton extends GuiButton
{
    private List<ProfileDataCallback> profiles;
    private final ProfileDataCallback callback;
    private final Minecraft mc;

    public GuiSBProfileButton(int id, int x, int y, int width, int height, ProfileDataCallback callback)
    {
        super(id, x, y, width, height, callback.getProfileName());
        this.mc = Minecraft.getMinecraft();
        this.callback = callback;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height)
        {
            this.mc.displayGuiScreen(new GuiSkyBlockData(this.profiles, this.callback));
            return true;
        }
        return false;
    }

    public long getLastSave()
    {
        return this.callback.getLastSave();
    }

    public void drawRegion(int parentWidth, int mouseX, int mouseY)
    {
        String text = this.getLastActive();

        if (this.visible)
        {
            boolean isHover = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            GlStateManager.disableDepth();

            if (isHover)
            {
                int k = 0;
                int l = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
                k = l;
                int i1 = mouseX + 12;
                int j1 = mouseY - 12;
                int k1 = 8;
                int l1 = -267386864;
                int i2 = 1347420415;
                int i3 = i2 & 16711422;
                int i4 = i2 & -16777216;
                int j2 = i3 >> 1 | i4;

            if (i1 + k > parentWidth)
            {
                i1 -= 28 + k;
            }

            this.zLevel = 300.0F;
            this.drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
            this.drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
            this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
            this.drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
            this.drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
            this.drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
            this.drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
            this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
            this.drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text, i1, j1, -1);
            this.zLevel = 0.0F;
            GlStateManager.enableDepth();
            }
        }
        GlStateManager.enableDepth();
    }

    public void setProfileList(List<ProfileDataCallback> profiles)
    {
        this.profiles = profiles;
    }

    private String getLastActive()
    {
        return "Last active: " + CommonUtils.getRelativeTime(this.callback.getLastSave());
    }
}
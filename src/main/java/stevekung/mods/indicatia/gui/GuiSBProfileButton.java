package stevekung.mods.indicatia.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import stevekung.mods.indicatia.gui.api.GuiSkyBlockData;

public class GuiSBProfileButton extends GuiButton
{
    private final String sbProfileId;
    private final String sbProfileName;
    private final String uuid;
    private final Minecraft mc;

    public GuiSBProfileButton(int id, int x, int y, int width, int height, String text, String sbProfileId, String uuid)
    {
        super(id, x, y, width, height, text);
        this.mc = Minecraft.getMinecraft();
        this.sbProfileId = sbProfileId;
        this.sbProfileName = text;
        this.uuid = uuid;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height)
        {
            this.mc.displayGuiScreen(new GuiSkyBlockData(this.sbProfileId, this.sbProfileName, this.uuid));
            return true;
        }
        return false;
    }
}
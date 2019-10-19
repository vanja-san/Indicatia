package stevekung.mods.indicatia.gui.optifine;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiScreenOF
extends GuiScreen
{
    public void actionPerformedRightClick(GuiButton button)
            throws IOException
    {}

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
            throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 1)
        {
            GuiButton btn = getSelectedButton(mouseX, mouseY, this.buttonList);
            if (btn != null) {
                if (btn.enabled)
                {
                    btn.playPressSound(this.mc.getSoundHandler());
                    this.actionPerformedRightClick(btn);
                }
            }
        }
    }

    public static GuiButton getSelectedButton(int x, int y, List<GuiButton> listButtons)
    {
        for (int i = 0; i < listButtons.size(); i++)
        {
            GuiButton btn = listButtons.get(i);
            if (btn.visible)
            {
                int btnWidth = btn.width;
                int btnHeight = btn.height;
                if (x >= btn.xPosition && y >= btn.yPosition && x < btn.xPosition + btnWidth && y < btn.yPosition + btnHeight) {
                    return btn;
                }
            }
        }
        return null;
    }
}
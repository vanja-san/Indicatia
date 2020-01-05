package stevekung.mods.indicatia.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stevekung.mods.indicatia.utils.NumberUtils;

@SideOnly(Side.CLIENT)
public class GuiNumberField extends GuiRightClickTextField
{
    public GuiNumberField(int id, FontRenderer font, int x, int y, int width, int height)
    {
        super(id, font, x, y, width, height);
    }

    @Override
    public void writeText(String textToWrite)
    {
        for (int i = 0; i < textToWrite.length(); i++)
        {
            if (NumberUtils.isNumber(textToWrite.charAt(i)))
            {
                super.writeText(textToWrite);
            }
        }
    }
}
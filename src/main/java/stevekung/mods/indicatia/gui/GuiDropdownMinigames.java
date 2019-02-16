package stevekung.mods.indicatia.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.stevekungslib.utils.ColorUtils;

import java.util.List;

public class GuiDropdownMinigames extends GuiButton
{
    private static final ResourceLocation texture = new ResourceLocation("indicatia:textures/gui/dropdown.png");
    public boolean dropdownClicked;
    private int selectedMinigame = -1;
    private final List<String> minigameLists;
    private final IDropboxCallback parentClass;
    private int displayLength;

    public GuiDropdownMinigames(IDropboxCallback parentClass, int x, int y, List<String> minigameLists)
    {
        super(0, x, y, 15, 15, "");
        this.parentClass = parentClass;
        this.minigameLists = minigameLists;

        if (this.minigameLists.size() == 1)
        {
            this.displayLength = 1;
        }
        else
        {
            this.displayLength = 6;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        Minecraft mc = Minecraft.getInstance();
        int hoverColor = 150;

        if (!this.dropdownClicked && this.enabled && this.visible && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height)
        {
            hoverColor = 180;
        }
        if (this.selectedMinigame == -1)
        {
            this.selectedMinigame = this.parentClass.getInitialSelection(this);

            if (this.selectedMinigame > this.minigameLists.size())
            {
                this.selectedMinigame = 0;
            }
        }
        if (this.visible)
        {
            GlStateManager.pushMatrix();
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            Gui.drawRect(this.x, this.y, this.x + this.width - 15, this.y + (this.dropdownClicked ? this.height * this.displayLength : this.height), ColorUtils.to32BitColor(255, 0, 0, 0));
            Gui.drawRect(this.x + 1, this.y + 1, this.x + this.width - 16, this.y + (this.dropdownClicked ? this.height * this.displayLength : this.height) - 1, ColorUtils.to32BitColor(255, hoverColor, hoverColor, hoverColor));
            Gui.drawRect(this.x + this.width - 15, this.y, this.x + this.width - 1, this.y + this.height, ColorUtils.to32BitColor(255, 0, 0, 0));
            Gui.drawRect(this.x + this.width - 15, this.y + 1, this.x + this.width - 2, this.y + this.height - 1, ColorUtils.to32BitColor(255, 150, 150, 150));

            if (this.displayLength > 1 && this.dropdownClicked && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width - 16 && mouseY < this.y + this.height * this.displayLength)
            {
                int hoverPos = (mouseY - this.y) / this.height;
                Gui.drawRect(this.x + 1, this.y + this.height * hoverPos + 1, this.x + this.width - 16, this.y + this.height * (hoverPos + 1) - 1, ColorUtils.to32BitColor(255, 180, 180, 180));
            }

            for (int i = 0; i + ExtendedConfig.hypixelMinigameScrollPos < this.minigameLists.size() && i < this.displayLength; ++i)
            {
                String minigames = this.minigameLists.get(i + ExtendedConfig.hypixelMinigameScrollPos);

                if (minigames != null)
                {
                    if (this.dropdownClicked)
                    {
                        mc.fontRenderer.drawStringWithShadow(minigames, this.x + this.width / 2 - 7 - mc.fontRenderer.getStringWidth(minigames) / 2, this.y + (this.height - 6) / 2 + this.height * i, ColorUtils.to32BitColor(255, 255, 255, 255));
                    }
                    else
                    {
                        mc.fontRenderer.drawStringWithShadow(this.minigameLists.get(this.selectedMinigame), this.x + this.width / 2 - 7 - mc.fontRenderer.getStringWidth(this.minigameLists.get(this.selectedMinigame)) / 2, this.y + (this.height - 6) / 2, ColorUtils.to32BitColor(255, 255, 255, 255));
                    }
                }
            }
            mc.getTextureManager().bindTexture(GuiDropdownMinigames.texture);
            Gui.drawModalRectWithCustomSizedTexture(this.x + this.width - 12, this.y + 5, 0, 0, 7, 4, 7, 4);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseEvent)
    {
        if (this.displayLength == 1)
        {
            return false;
        }
        if (!this.dropdownClicked)
        {
            if (this.enabled && this.visible && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height)
            {
                this.dropdownClicked = true;
                return true;
            }
        }
        else
        {
            if (this.enabled && this.visible && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width - 16 && mouseY < this.y + this.height * this.displayLength)
            {
                double optionClicked = (mouseY - this.y) / this.height + ExtendedConfig.hypixelMinigameScrollPos;
                this.selectedMinigame = (int)optionClicked % this.minigameLists.size();
                this.dropdownClicked = false;
                this.parentClass.onSelectionChanged(this, this.selectedMinigame);
                return true;
            }
            else
            {
                this.dropdownClicked = false;
                return false;
            }
        }
        return false;
    }

    public void scroll(double amount)
    {
        ExtendedConfig.hypixelMinigameScrollPos += amount;
        int i = this.minigameLists.size();

        if (ExtendedConfig.hypixelMinigameScrollPos > i - this.displayLength)
        {
            ExtendedConfig.hypixelMinigameScrollPos = i - this.displayLength;
        }
        if (ExtendedConfig.hypixelMinigameScrollPos <= 0)
        {
            ExtendedConfig.hypixelMinigameScrollPos = 0;
        }
    }

    public boolean isHoverDropdown(double mouseX, double mouseY)
    {
        return this.enabled && this.visible && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width - 16 && mouseY < this.y + this.height * this.displayLength;
    }

    public interface IDropboxCallback
    {
        void onSelectionChanged(GuiDropdownMinigames dropdown, int selection);

        int getInitialSelection(GuiDropdownMinigames dropdown);
    }
}
package stevekung.mods.indicatia.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.gui.GuiDropdownMinigames.IDropboxCallback;
import stevekung.mods.indicatia.utils.*;

public class GuiIndicatiaChat implements IGuiChat, IDropboxCallback
{
    private GuiDropdownMinigames dropdown;
    private int prevSelect = -1;

    @Override
    public void initGui(List<GuiButton> buttonList, int width, int height)
    {
        this.updateButton(buttonList, width, height);
    }

    @Override
    public void drawScreen(List<GuiButton> buttonList, int mouseX, int mouseY, float partialTicks)
    {
        buttonList.forEach(button ->
        {
            if (button instanceof GuiButtonCustomize)
            {
                GuiButtonCustomize customButton = (GuiButtonCustomize) button;
                customButton.drawRegion(mouseX, mouseY);
            }
        });
    }

    @Override
    public void updateScreen(List<GuiButton> buttonList, int width, int height)
    {
        if (InfoUtils.INSTANCE.isHypixel())
        {
            if (this.prevSelect != ExtendedConfig.instance.selectedHypixelMinigame)
            {
                this.updateButton(buttonList, width, height);
                this.prevSelect = ExtendedConfig.instance.selectedHypixelMinigame;
            }

            boolean clicked = !this.dropdown.dropdownClicked;

            buttonList.forEach(button ->
            {
                if (button instanceof GuiButtonCustomize)
                {
                    GuiButtonCustomize buttonCustom = (GuiButtonCustomize) button;
                    buttonCustom.visible = clicked;
                }
            });
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {}

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {}

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}

    @Override
    public void actionPerformed(GuiButton button)
    {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;

        if (player == null || !(mc.currentScreen instanceof GuiChat))
        {
            return;
        }

        if (button instanceof GuiButtonCustomize)
        {
            GuiButtonCustomize buttomCustom = (GuiButtonCustomize) button;

            if (button.id == buttomCustom.id)
            {
                player.sendChatMessage(buttomCustom.command);
            }
        }

        switch (button.id)
        {
        case 200:
            player.sendChatMessage("/chat a");
            player.addChatMessage(JsonUtils.create("Reset Hypixel Chat"));
            break;
        case 201:
            player.sendChatMessage("/chat p");
            player.addChatMessage(JsonUtils.create("Set chat mode to Hypixel Party Chat"));
            break;
        case 202:
            player.sendChatMessage("/chat g");
            player.addChatMessage(JsonUtils.create("Set chat mode to Hypixel Guild Chat"));
            break;
        }
    }

    @Override
    public void onGuiClosed()
    {
        ExtendedConfig.instance.save();
    }

    @Override
    public void handleMouseInput(int width, int height)
    {
        Minecraft mc = Minecraft.getMinecraft();
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;

        if (this.dropdown != null && this.dropdown.dropdownClicked && this.dropdown.isHoverDropdown(mouseX, mouseY))
        {
            int i = Mouse.getEventDWheel();

            if (i != 0)
            {
                if (i > 1)
                {
                    i = -1;
                }
                if (i < -1)
                {
                    i = 1;
                }
                if (GuiScreen.isCtrlKeyDown())
                {
                    i *= 7;
                }
                this.dropdown.scroll(i);
            }
        }
    }

    @Override
    public void pageUp() {}

    @Override
    public void pageDown() {}

    @Override
    public void getSentHistory(int msgPos) {}

    @Override
    public void onSelectionChanged(GuiDropdownMinigames dropdown, int selection)
    {
        ExtendedConfig.instance.selectedHypixelMinigame = selection;
        ExtendedConfig.instance.save();
    }

    @Override
    public int getInitialSelection(GuiDropdownMinigames dropdown)
    {
        return ExtendedConfig.instance.selectedHypixelMinigame;
    }

    private void updateButton(List<GuiButton> buttonList, int width, int height)
    {
        Minecraft mc = Minecraft.getMinecraft();
        buttonList.clear();

        if (InfoUtils.INSTANCE.isHypixel())
        {
            List<String> list = new ArrayList<>();

            for (MinigameData data : MinigameData.getMinigameData())
            {
                list.add(data.getName());
            }

            String max = Collections.max(list, Comparator.comparing(text -> text.length()));
            int length = mc.fontRendererObj.getStringWidth(max) + 32;

            // hypixel chat
            buttonList.add(new GuiButton(200, width - 63, height - 35, 60, 20, "Reset Chat"));
            buttonList.add(new GuiButton(201, width - 63, height - 56, 60, 20, "Party Chat"));
            buttonList.add(new GuiButton(202, width - 63, height - 77, 60, 20, "Guild Chat"));
            buttonList.add(this.dropdown = new GuiDropdownMinigames(this, width - length, 2, list));
            this.dropdown.width = length;
            this.prevSelect = ExtendedConfig.instance.selectedHypixelMinigame;

            List<GuiButtonCustomize> gameBtn = new ArrayList<>();
            int xPos2 = width - 99;

            if (this.prevSelect > list.size())
            {
                this.prevSelect = 0;
                ExtendedConfig.instance.selectedHypixelMinigame = 0;
            }

            for (MinigameData data : MinigameData.getMinigameData())
            {
                for (MinigameCommand command : data.getCommands())
                {
                    if (data.getName().equals(list.get(this.prevSelect)))
                    {
                        gameBtn.add(new GuiButtonCustomize(width, command.getName(), command.getCommand(), command.isMinigame()));
                    }
                }
            }

            for (int i = 0; i < gameBtn.size(); i++)
            {
                GuiButtonCustomize button = gameBtn.get(i);

                if (i >= 6 && i <= 10)
                {
                    button.xPosition = xPos2 - 136;
                    button.yPosition = 41;
                }
                else if (i >= 11 && i <= 15)
                {
                    button.xPosition = xPos2 - 241;
                    button.yPosition = 62;
                }
                else if (i >= 16 && i <= 20)
                {
                    button.xPosition = xPos2 - 346;
                    button.yPosition = 83;
                }
                else if (i >= 21)
                {
                    button.xPosition = xPos2 - 451;
                    button.yPosition = 104;
                }
                button.xPosition += 21 * i;
                buttonList.add(button);
            }
        }

        for (GuiButton button : buttonList)
        {
            if (!button.getClass().equals(GuiDropdownMinigames.class) && !(button.id >= 0 && button.id <= 202))
            {
                button.visible = false;
            }
        }
    }
}
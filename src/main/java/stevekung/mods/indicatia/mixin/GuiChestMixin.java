package stevekung.mods.indicatia.mixin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.google.common.collect.ObjectArrays;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.util.*;
import net.minecraftforge.client.ClientCommandHandler;
import stevekung.mods.indicatia.core.IndicatiaMod;
import stevekung.mods.indicatia.integration.SkyBlockAddonsBackpack;
import stevekung.mods.indicatia.integration.SkyblockAddonsGuiChest;
import stevekung.mods.indicatia.utils.ITradeGUI;

@Mixin(GuiChest.class)
public abstract class GuiChestMixin extends GuiContainer implements ITradeGUI
{
    private GuiTextField inputField;
    private int sentHistoryCursor = -1;
    private boolean playerNamesFound;
    private boolean waitingOnAutocomplete;
    private int autocompleteIndex;
    private List<String> foundPlayerNames = new ArrayList<>();
    private String historyBuffer = "";

    private final SkyblockAddonsGuiChest chest = new SkyblockAddonsGuiChest();
    private GuiTextField textFieldMatch = null;
    private GuiTextField textFieldExclusions = null;

    @Shadow
    private IInventory lowerChestInventory;

    public GuiChestMixin(Container container)
    {
        super(container);
    }

    @Override
    public void initGui()
    {
        super.initGui();

        if (this.isTradeGUI())
        {
            Keyboard.enableRepeatEvents(true);
            this.sentHistoryCursor = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
            this.inputField = new GuiTextField(0, this.fontRendererObj, 4, this.height - 12, this.width - 4, 12);
            this.inputField.setEnableBackgroundDrawing(false);
            this.inputField.setMaxStringLength(256);
            this.inputField.setFocused(false);
            this.inputField.setCanLoseFocus(true);
        }
        if (IndicatiaMod.isSkyblockAddonsLoaded)
        {
            this.textFieldMatch = this.chest.initGui(this.lowerChestInventory, this.fontRendererObj, this.guiTop, this.guiLeft)[0];
            this.textFieldExclusions = this.chest.initGui(this.lowerChestInventory, this.fontRendererObj, this.guiTop, this.guiLeft)[1];
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        if (this.isTradeGUI())
        {
            Gui.drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);
            this.inputField.drawTextBox();
        }

        if (this.textFieldMatch != null && IndicatiaMod.isSkyblockAddonsLoaded)
        {
            this.chest.drawScreen(this.mc, this.guiLeft, this.guiTop, this.textFieldMatch, this.textFieldExclusions);
        }

        for (int ii = 0; ii < this.buttonList.size(); ++ii)
        {
            this.buttonList.get(ii).drawButton(this.mc, mouseX, mouseY);
        }
        for (int jj = 0; jj < this.labelList.size(); ++jj)
        {
            this.labelList.get(jj).drawLabel(this.mc, mouseX, mouseY);
        }

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(i, j, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        this.theSlot = null;
        int k = 240;
        int l = 240;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, k / 1.0F, l / 1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        for (int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); ++i1)
        {
            Slot slot = this.inventorySlots.inventorySlots.get(i1);
            this.drawSlot(slot);

            if (this.isMouseOverSlot(slot, mouseX, mouseY) && slot.canBeHovered())
            {
                this.theSlot = slot;
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                int j1 = slot.xDisplayPosition;
                int k1 = slot.yDisplayPosition;
                GlStateManager.colorMask(true, true, true, false);
                this.drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
                GlStateManager.colorMask(true, true, true, true);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }
        }

        RenderHelper.disableStandardItemLighting();
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        RenderHelper.enableGUIStandardItemLighting();
        InventoryPlayer inventoryplayer = this.mc.thePlayer.inventory;
        ItemStack itemstack = this.draggedStack == null ? inventoryplayer.getItemStack() : this.draggedStack;

        if (itemstack != null)
        {
            int j2 = 8;
            int k2 = this.draggedStack == null ? 8 : 16;
            String s = null;

            if (this.draggedStack != null && this.isRightMouseClick)
            {
                itemstack = itemstack.copy();
                itemstack.stackSize = MathHelper.ceiling_float_int(itemstack.stackSize / 2.0F);
            }
            else if (this.dragSplitting && this.dragSplittingSlots.size() > 1)
            {
                itemstack = itemstack.copy();
                itemstack.stackSize = this.dragSplittingRemnant;

                if (itemstack.stackSize == 0)
                {
                    s = "" + EnumChatFormatting.YELLOW + "0";
                }
            }
            this.drawItemStack(itemstack, mouseX - i - j2, mouseY - j - k2, s);
        }

        if (this.returningStack != null)
        {
            float f = (Minecraft.getSystemTime() - this.returningStackTime) / 100.0F;

            if (f >= 1.0F)
            {
                f = 1.0F;
                this.returningStack = null;
            }
            int l2 = this.returningStackDestSlot.xDisplayPosition - this.touchUpX;
            int i3 = this.returningStackDestSlot.yDisplayPosition - this.touchUpY;
            int l1 = this.touchUpX + (int)(l2 * f);
            int i2 = this.touchUpY + (int)(i3 * f);
            this.drawItemStack(this.returningStack, l1, i2, (String)null);
        }

        GlStateManager.popMatrix();

        if (inventoryplayer.getItemStack() == null && this.theSlot != null && this.theSlot.getHasStack())
        {
            ItemStack itemstack1 = this.theSlot.getStack();
            this.renderToolTip(itemstack1, mouseX, mouseY);
        }
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();

        if (IndicatiaMod.isSkyblockAddonsLoaded)
        {
            SkyBlockAddonsBackpack backpack = new SkyBlockAddonsBackpack();
            backpack.drawBackpacks(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (this.isTradeGUI())
        {
            this.inputField.mouseClicked(mouseX, mouseY, mouseButton);
        }
        if (IndicatiaMod.isSkyblockAddonsLoaded && this.textFieldMatch != null)
        {
            this.textFieldMatch.mouseClicked(mouseX, mouseY, mouseButton);
            this.textFieldExclusions.mouseClicked(mouseX, mouseY, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onGuiClosed()
    {
        if (this.isTradeGUI())
        {
            Keyboard.enableRepeatEvents(false);
        }
        if (IndicatiaMod.isSkyblockAddonsLoaded && this.textFieldMatch != null && this.textFieldExclusions != null)
        {
            Keyboard.enableRepeatEvents(false);
        }
        super.onGuiClosed();
    }

    @Override
    public void updateScreen()
    {
        if (this.isTradeGUI())
        {
            this.inputField.updateCursorCounter();
        }
        if (IndicatiaMod.isSkyblockAddonsLoaded && this.textFieldMatch != null && this.textFieldExclusions != null)
        {
            this.textFieldMatch.updateCursorCounter();
            this.textFieldExclusions.updateCursorCounter();
        }
        super.updateScreen();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (this.isTradeGUI())
        {
            if ((keyCode == 1 || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode()) && !this.inputField.isFocused())
            {
                this.mc.thePlayer.closeScreen();
            }

            if (keyCode != 28 && keyCode != 156)
            {
                if (keyCode == 200)
                {
                    this.getSentHistory(-1);
                }
                else if (keyCode == 208)
                {
                    this.getSentHistory(1);
                }
                else
                {
                    this.inputField.textboxKeyTyped(typedChar, keyCode);
                }
            }
            else
            {
                String text = this.inputField.getText().trim();

                if (!text.isEmpty())
                {
                    this.sendChatMessage(text);
                }
                this.inputField.setText("");
                this.mc.ingameGUI.getChatGUI().resetScroll();
            }

            this.waitingOnAutocomplete = false;

            if (keyCode == 15)
            {
                this.autocompletePlayerNames();
            }
            else
            {
                this.playerNamesFound = false;
            }
        }
        else
        {
            if (this.chest.getInventoryType() != null && IndicatiaMod.isSkyblockAddonsLoaded)
            {
                if (keyCode != this.mc.gameSettings.keyBindInventory.getKeyCode() || !this.textFieldMatch.isFocused() && !this.textFieldExclusions.isFocused())
                {
                    super.keyTyped(typedChar, keyCode);
                }
                this.chest.keyTyped(typedChar, keyCode, this.textFieldMatch, this.textFieldExclusions);
            }
            else
            {
                super.keyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    protected void setText(String newChatText, boolean shouldOverwrite)
    {
        if (this.isTradeGUI())
        {
            if (shouldOverwrite)
            {
                this.inputField.setText(newChatText);
            }
            else
            {
                this.inputField.writeText(newChatText);
            }
        }
    }

    @Override
    public void onAutocompleteResponse(String[] list)
    {
        if (this.waitingOnAutocomplete)
        {
            this.playerNamesFound = false;
            this.foundPlayerNames.clear();

            String[] complete = ClientCommandHandler.instance.latestAutoComplete;

            if (complete != null)
            {
                list = ObjectArrays.concat(complete, list, String.class);
            }

            for (String s : list)
            {
                if (s.length() > 0)
                {
                    this.foundPlayerNames.add(s);
                }
            }

            String s1 = this.inputField.getText().substring(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false));
            String s2 = StringUtils.getCommonPrefix(list);
            s2 = EnumChatFormatting.getTextWithoutFormattingCodes(s2);

            if (s2.length() > 0 && !s1.equalsIgnoreCase(s2))
            {
                this.inputField.deleteFromCursor(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false) - this.inputField.getCursorPosition());
                this.inputField.writeText(s2);
            }
            else if (this.foundPlayerNames.size() > 0)
            {
                this.playerNamesFound = true;
                this.autocompletePlayerNames();
            }
        }
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotId, int clickedButton, int clickType)
    {
        if (IndicatiaMod.isSkyblockAddonsLoaded)
        {
            if (this.chest.handleMouseClick(slot, this.mc, this.inventorySlots, this.lowerChestInventory))
            {
                super.handleMouseClick(slot, slotId, clickedButton, clickType);
            }
        }
        else
        {
            super.handleMouseClick(slot, slotId, clickedButton, clickType);
        }
    }

    private void getSentHistory(int msgPos)
    {
        int i = this.sentHistoryCursor + msgPos;
        int j = this.mc.ingameGUI.getChatGUI().getSentMessages().size();
        i = MathHelper.clamp_int(i, 0, j);

        if (i != this.sentHistoryCursor)
        {
            if (i == j)
            {
                this.sentHistoryCursor = j;
                this.inputField.setText(this.historyBuffer);
            }
            else
            {
                if (this.sentHistoryCursor == j)
                {
                    this.historyBuffer = this.inputField.getText();
                }
                this.inputField.setText(this.mc.ingameGUI.getChatGUI().getSentMessages().get(i));
                this.sentHistoryCursor = i;
            }
        }
    }

    private void sendAutocompleteRequest(String leftOfCursor, String full)
    {
        if (leftOfCursor.length() >= 1)
        {
            ClientCommandHandler.instance.autoComplete(leftOfCursor, full);
            BlockPos blockpos = null;

            if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            {
                blockpos = this.mc.objectMouseOver.getBlockPos();
            }
            this.mc.thePlayer.sendQueue.addToSendQueue(new C14PacketTabComplete(leftOfCursor, blockpos));
            this.waitingOnAutocomplete = true;
        }
    }

    private void autocompletePlayerNames()
    {
        if (this.playerNamesFound)
        {
            this.inputField.deleteFromCursor(this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false) - this.inputField.getCursorPosition());

            if (this.autocompleteIndex >= this.foundPlayerNames.size())
            {
                this.autocompleteIndex = 0;
            }
        }
        else
        {
            int i = this.inputField.func_146197_a(-1, this.inputField.getCursorPosition(), false);
            this.foundPlayerNames.clear();
            this.autocompleteIndex = 0;
            String s = this.inputField.getText().substring(i).toLowerCase();
            String s1 = this.inputField.getText().substring(0, this.inputField.getCursorPosition());
            this.sendAutocompleteRequest(s1, s);

            if (this.foundPlayerNames.isEmpty())
            {
                return;
            }
            this.playerNamesFound = true;
            this.inputField.deleteFromCursor(i - this.inputField.getCursorPosition());
        }

        if (this.foundPlayerNames.size() > 1)
        {
            StringBuilder stringbuilder = new StringBuilder();

            for (String s2 : this.foundPlayerNames)
            {
                if (stringbuilder.length() > 0)
                {
                    stringbuilder.append(", ");
                }
                stringbuilder.append(s2);
            }
            this.mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(stringbuilder.toString()), 1);
        }
        this.inputField.writeText(EnumChatFormatting.getTextWithoutFormattingCodes(this.foundPlayerNames.get(this.autocompleteIndex++)));
    }

    private boolean isTradeGUI()
    {
        return this.lowerChestInventory.getDisplayName().getUnformattedText().equals("You                  Other");
    }
}
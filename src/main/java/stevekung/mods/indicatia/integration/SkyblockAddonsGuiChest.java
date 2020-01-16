package stevekung.mods.indicatia.integration;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.lwjgl.input.Keyboard;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.elements.CraftingPatternSelection;
import codes.biscuit.skyblockaddons.listeners.RenderListener;
import codes.biscuit.skyblockaddons.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import stevekung.mods.indicatia.utils.ColorUtils;

public class SkyblockAddonsGuiChest
{
    private static final int OVERLAY_RED = ColorUtils.to32BitColor(128, 255, 85, 85);
    private static final int OVERLAY_GREEN = ColorUtils.to32BitColor(128, 85, 255, 85);
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private EnumUtils.InventoryType inventoryType = null;
    private CraftingPatternSelection craftingPatternSelection = null;

    public void initGui(IInventory lowerChestInventory)
    {
        String guiName = lowerChestInventory.getDisplayName().getUnformattedText();
        this.inventoryType = EnumUtils.InventoryType.getCurrentInventoryType(guiName);
    }

    public void initGuiCraftingPattern(IInventory lowerChestInventory, int guiTop, int guiLeft)
    {
        if (this.inventoryType != null)
        {
            if (this.isCraftingPattern())
            {
                if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.CRAFTING_PATTERNS))
                {
                    this.craftingPatternSelection = new CraftingPatternSelection(Minecraft.getMinecraft(), Math.max(guiLeft - CraftingPatternSelection.ICON_SIZE - 2, 10), guiTop + 1);
                }
            }
        }
    }

    public GuiTextField[] initGuiTextField(IInventory lowerChestInventory, FontRenderer fontRendererObj, int guiTop, int guiLeft)
    {
        GuiTextField[] textFields = new GuiTextField[2];

        if (this.inventoryType != null)
        {
            int xPos = guiLeft - 160;

            if (xPos < 0)
            {
                xPos = 20;
            }

            int yPos = guiTop + 80;
            textFields[0] = new GuiTextField(2, fontRendererObj, xPos, yPos, 120, 20);
            textFields[0].setMaxStringLength(500);
            List<String> lockedEnchantments = SkyblockAddons.getInstance().getUtils().getEnchantmentMatch();
            StringBuilder enchantmentBuilder = new StringBuilder();
            int i = 1;

            for (String enchantment : lockedEnchantments)
            {
                enchantmentBuilder.append(enchantment);

                if (i < lockedEnchantments.size())
                {
                    enchantmentBuilder.append(",");
                }
                i++;
            }

            String text = enchantmentBuilder.toString();

            if (text.length() > 0)
            {
                textFields[0].setText(text);
            }

            yPos += 40;
            textFields[1] = new GuiTextField(2, fontRendererObj, xPos, yPos, 120, 20);
            textFields[1].setMaxStringLength(500);
            lockedEnchantments = SkyblockAddons.getInstance().getUtils().getEnchantmentExclusion();
            enchantmentBuilder = new StringBuilder();
            i = 1;

            for (String enchantment : lockedEnchantments)
            {
                enchantmentBuilder.append(enchantment);

                if (i < lockedEnchantments.size())
                {
                    enchantmentBuilder.append(",");
                }
                i++;
            }

            text = enchantmentBuilder.toString();

            if (text.length() > 0)
            {
                textFields[1].setText(text);
            }
            Keyboard.enableRepeatEvents(true);
        }
        return textFields;
    }

    public void drawScreen(Minecraft mc, int guiLeft, int guiTop, GuiTextField textFieldMatch, GuiTextField textFieldExclusions)
    {
        GlStateManager.color(1F, 1F, 1F);
        SkyblockAddons main = SkyblockAddons.getInstance();
        String inventoryMessage = this.inventoryType.getMessage();
        int defaultBlue = main.getUtils().getDefaultBlue(255);
        GlStateManager.pushMatrix();
        float scale = 0.75F;
        GlStateManager.scale(scale, scale, 1);
        int x = guiLeft - 160;

        if (x < 0)
        {
            x = 20;
        }

        mc.ingameGUI.drawString(mc.fontRendererObj, Message.MESSAGE_TYPE_ENCHANTMENTS.getMessage(inventoryMessage), Math.round(x/scale), Math.round((guiTop+40)/scale), defaultBlue);
        mc.ingameGUI.drawString(mc.fontRendererObj, Message.MESSAGE_SEPARATE_ENCHANTMENTS.getMessage(), Math.round(x/scale), Math.round((guiTop + 50)/scale), defaultBlue);
        mc.ingameGUI.drawString(mc.fontRendererObj, Message.MESSAGE_ENCHANTS_TO_MATCH.getMessage(inventoryMessage), Math.round(x/scale), Math.round((guiTop + 70)/scale), defaultBlue);
        mc.ingameGUI.drawString(mc.fontRendererObj,Message.MESSAGE_ENCHANTS_TO_EXCLUDE.getMessage(inventoryMessage), Math.round(x/scale), Math.round((guiTop + 110)/scale), defaultBlue);
        GlStateManager.popMatrix();

        textFieldMatch.drawTextBox();

        if (textFieldMatch.getText().equals(""))
        {
            mc.ingameGUI.drawString(mc.fontRendererObj, "ex. \"prot, feather\"", x+4, guiTop + 86, ConfigColor.DARK_GRAY.getColor());
        }

        textFieldExclusions.drawTextBox();

        if (textFieldExclusions.getText().equals(""))
        {
            mc.ingameGUI.drawString(mc.fontRendererObj, "ex. \"proj, blast\"", x+4, guiTop + 126, ConfigColor.DARK_GRAY.getColor());
        }
    }

    public void drawScreenCraftingPattern()
    {
        if (this.isCraftingPattern() && this.craftingPatternSelection != null)
        {
            this.craftingPatternSelection.draw();
        }
    }

    public void keyTyped(char typedChar, int keyCode, GuiTextField textFieldMatch, GuiTextField textFieldExclusions)
    {
        if (textFieldMatch != null)
        {
            textFieldMatch.textboxKeyTyped(typedChar, keyCode);
            textFieldExclusions.textboxKeyTyped(typedChar, keyCode);
            List<String> enchantments = new LinkedList<>(Arrays.asList(textFieldMatch.getText().split(",")));
            SkyblockAddons.getInstance().getUtils().setEnchantmentMatch(enchantments);
            enchantments = new LinkedList<>(Arrays.asList(textFieldExclusions.getText().split(",")));
            SkyblockAddons.getInstance().getUtils().setEnchantmentExclusion(enchantments);
        }
    }

    public boolean handleMouseClick(Slot slot, Minecraft mc, Container inventorySlots, IInventory lowerChestInventory, int clickType)
    {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Container slots = inventorySlots;

        if (main.getUtils().getEnchantmentMatch().size() > 0)
        {
            if (slot != null && !slot.inventory.equals(mc.thePlayer.inventory) && slot.getHasStack())
            {
                if (slot.getSlotIndex() == 13 && this.inventoryType == EnumUtils.InventoryType.ENCHANTMENT_TABLE)
                {
                    ItemStack[] enchantBottles = {slots.getSlot(29).getStack(), slots.getSlot(31).getStack(), slots.getSlot(33).getStack()};

                    for (ItemStack bottle : enchantBottles)
                    {
                        if (bottle != null && bottle.hasDisplayName())
                        {
                            if (bottle.getDisplayName().startsWith(EnumChatFormatting.GREEN + "Enchant Item"))
                            {
                                List<String> toolip = bottle.getTooltip(mc.thePlayer, false);

                                if (toolip.size() > 2)
                                {
                                    String[] lines = toolip.get(2).split(Pattern.quote("* "));

                                    if (lines.length > 1)
                                    {
                                        String enchantLine = lines[1];

                                        if (main.getUtils().enchantReforgeMatches(enchantLine))
                                        {
                                            main.getUtils().playLoudSound("random.orb", 0.1);
                                            return false;
                                        }
                                    }
                                }
                            }
                            else if (bottle.getDisplayName().startsWith(EnumChatFormatting.RED + "Enchant Item"))
                            {
                                // Stop player from removing item before the enchants have even loaded.
                                return false;
                            }
                        }
                    }
                }
                else if (slot.getSlotIndex() == 22 && this.inventoryType == EnumUtils.InventoryType.REFORGE_ANVIL)
                {
                    Slot itemSlot = slots.getSlot(13);

                    if (itemSlot != null && itemSlot.getHasStack())
                    {
                        ItemStack item = itemSlot.getStack();

                        if (item.hasDisplayName())
                        {
                            String reforge = main.getUtils().getReforgeFromItem(item);

                            if (reforge != null)
                            {
                                if (main.getUtils().enchantReforgeMatches(reforge))
                                {
                                    main.getUtils().playLoudSound("random.orb", 0.1);
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) && lowerChestInventory.hasCustomName() && EnumUtils.SkyblockNPC.isMerchant(lowerChestInventory.getDisplayName().getUnformattedText()) && slot != null && slot.inventory instanceof InventoryPlayer)
        {
            if (main.getInventoryUtils().shouldCancelDrop(slot))
            {
                return false;
            }
        }
        return true;
    }

    public void drawSBASlot(Minecraft mc, Gui gui, Slot slot)
    {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Container container = mc.thePlayer.openContainer;

        if (slot != null)
        {
            // Draw crafting pattern overlays inside the crafting grid
            if (main.getConfigValues().isEnabled(Feature.CRAFTING_PATTERNS) && main.getUtils().isOnSkyblock() && slot.inventory.getDisplayName().getUnformattedText().equals(CraftingPattern.CRAFTING_TABLE_DISPLAYNAME) && CraftingPatternSelection.selectedPattern != CraftingPattern.FREE)
            {
                int craftingGridIndex = CraftingPattern.slotToCraftingGridIndex(slot.getSlotIndex());

                if (craftingGridIndex >= 0)
                {
                    int slotLeft = slot.xDisplayPosition;
                    int slotTop = slot.yDisplayPosition;
                    int slotRight = slotLeft + 16;
                    int slotBottom = slotTop + 16;

                    if (CraftingPatternSelection.selectedPattern.isSlotInPattern(craftingGridIndex))
                    {
                        if (!slot.getHasStack())
                        {
                            this.drawGradientRect(gui, slotLeft, slotTop, slotRight, slotBottom, OVERLAY_GREEN, OVERLAY_GREEN);
                        }
                    }
                    else
                    {
                        if (slot.getHasStack())
                        {
                            this.drawGradientRect(gui, slotLeft, slotTop, slotRight, slotBottom, OVERLAY_RED, OVERLAY_RED);
                        }
                    }
                }
            }

            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && main.getUtils().isOnSkyblock())
            {
                int slotNum = slot.slotNumber + main.getInventoryUtils().getSlotDifference(container);

                if (main.getConfigValues().getLockedSlots().contains(slotNum) && (slotNum >= 9 || container instanceof ContainerPlayer && slotNum >= 5))
                {
                    GlStateManager.disableLighting();
                    GlStateManager.disableDepth();
                    GlStateManager.color(1,1,1,0.4F);
                    GlStateManager.enableBlend();
                    Minecraft.getMinecraft().getTextureManager().bindTexture(RenderListener.LOCK);
                    mc.ingameGUI.drawTexturedModalRect(slot.xDisplayPosition, slot.yDisplayPosition, 0, 0, 16, 16);
                    GlStateManager.enableLighting();
                    GlStateManager.enableDepth();
                }
            }
        }
    }

    public void drawBackpacks(GuiScreen gui, int mouseX, int mouseY, float partialTicks)
    {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Backpack backpack = main.getUtils().getBackpackToRender();
        Minecraft mc = Minecraft.getMinecraft();

        if (backpack != null)
        {
            int x = backpack.getX();
            int y = backpack.getY();
            ItemStack[] items = backpack.getItems();
            int length = items.length;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (main.getConfigValues().getBackpackStyle() == EnumUtils.BackpackStyle.GUI)
            {
                mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                int rows = length/9;
                GlStateManager.disableLighting();
                GlStateManager.pushMatrix();
                GlStateManager.translate(0,0,300);
                int textColor = 4210752;

                if (main.getConfigValues().isEnabled(Feature.MAKE_BACKPACK_INVENTORIES_COLORED))
                {
                    BackpackColor color = backpack.getBackpackColor();
                    GlStateManager.color(color.getR(), color.getG(), color.getB(), 1);
                    textColor = color.getTextColor();
                }

                gui.drawTexturedModalRect(x, y, 0, 0, 176, rows * 18 + 17);
                gui.drawTexturedModalRect(x, y + rows * 18 + 17, 0, 215, 176, 7);
                mc.fontRendererObj.drawString(backpack.getBackpackName(), x+8, y+6, textColor);
                GlStateManager.popMatrix();
                GlStateManager.enableLighting();

                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();

                for (int i = 0; i < length; i++)
                {
                    ItemStack item = items[i];

                    if (item != null)
                    {
                        int itemX = x+8 + i % 9 * 18;
                        int itemY = y+18 + i / 9 * 18;
                        RenderItem renderItem = mc.getRenderItem();
                        gui.zLevel = 200;
                        renderItem.zLevel = 200;
                        renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY);
                        renderItem.renderItemOverlayIntoGUI(mc.fontRendererObj, item, itemX, itemY, null);
                        gui.zLevel = 0;
                        renderItem.zLevel = 0;
                    }
                }
            }
            else
            {
                GlStateManager.disableLighting();
                GlStateManager.pushMatrix();
                GlStateManager.translate(0,0, 300);
                Gui.drawRect(x, y, x + 16 * 9 + 3, y + 16 * (length / 9) + 3, ConfigColor.DARK_GRAY.getColor(250));
                GlStateManager.popMatrix();
                GlStateManager.enableLighting();

                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();

                for (int i = 0; i < length; i++)
                {
                    ItemStack item = items[i];

                    if (item != null)
                    {
                        int itemX = x + i % 9 * 16;
                        int itemY = y + i / 9 * 16;
                        RenderItem renderItem = mc.getRenderItem();
                        gui.zLevel = 200;
                        renderItem.zLevel = 200;
                        renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY);
                        renderItem.renderItemOverlayIntoGUI(mc.fontRendererObj, item, itemX, itemY, null);
                        gui.zLevel = 0;
                        renderItem.zLevel = 0;
                    }
                }
            }
            main.getUtils().setBackpackToRender(null);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
        }
    }

    public void keyTypedLockedSlot(Minecraft mc, Slot theSlot, int keyCode)
    {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getUtils().isOnSkyblock())
        {
            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && keyCode != 1 && keyCode != mc.gameSettings.keyBindInventory.getKeyCode())
            {
                int slot = main.getUtils().getLastHoveredSlot();

                if (mc.thePlayer.inventory.getItemStack() == null && theSlot != null)
                {
                    for (int i = 0; i < 9; ++i)
                    {
                        if (keyCode == mc.gameSettings.keyBindsHotbar[i].getKeyCode())
                        {
                            slot = i + 36; // They are hotkeying, the actual slot is the targeted one, +36 because
                        }
                    }
                }

                if (slot >= 9 || mc.thePlayer.openContainer instanceof ContainerPlayer && slot >= 5)
                {
                    if (main.getConfigValues().getLockedSlots().contains(slot))
                    {
                        if (main.getLockSlot().getKeyCode() == keyCode)
                        {
                            main.getUtils().playLoudSound("random.orb", 1);
                            main.getConfigValues().getLockedSlots().remove(slot);
                            main.getConfigValues().saveConfig();
                        }
                        else
                        {
                            main.getUtils().playLoudSound("note.bass", 0.5);
                            return;
                        }
                    }
                    else
                    {
                        if (main.getLockSlot().getKeyCode() == keyCode)
                        {
                            main.getUtils().playLoudSound("random.orb", 0.1);
                            main.getConfigValues().getLockedSlots().add(slot);
                            main.getConfigValues().saveConfig();
                        }
                    }
                }
            }
            if (mc.gameSettings.keyBindDrop.getKeyCode() == keyCode && main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS))
            {
                if (main.getInventoryUtils().shouldCancelDrop(theSlot))
                {
                    return;
                }
            }
        }
    }

    private void drawGradientRect(Gui gui, int left, int top, int right, int bottom, int startColor, int endColor)
    {
        float f = (startColor >> 24 & 255) / 255.0F;
        float f1 = (startColor >> 16 & 255) / 255.0F;
        float f2 = (startColor >> 8 & 255) / 255.0F;
        float f3 = (startColor & 255) / 255.0F;
        float f4 = (endColor >> 24 & 255) / 255.0F;
        float f5 = (endColor >> 16 & 255) / 255.0F;
        float f6 = (endColor >> 8 & 255) / 255.0F;
        float f7 = (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(right, top, gui.zLevel).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(left, top, gui.zLevel).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(left, bottom, gui.zLevel).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos(right, bottom, gui.zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public void drawGradientRectMod(Gui gui, Slot slot, Minecraft mc, int left, int top, int right, int bottom, int startColor, int endColor)
    {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Container container = mc.thePlayer.openContainer;
        int slotNum = slot.slotNumber + main.getInventoryUtils().getSlotDifference(container);
        main.getUtils().setLastHoveredSlot(slotNum);

        if (slot != null && main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && main.getUtils().isOnSkyblock() && main.getConfigValues().getLockedSlots().contains(slotNum) && (slotNum >= 9 || container instanceof ContainerPlayer && slotNum >= 5))
        {
            this.drawGradientRect(gui, left, top, right, bottom, OVERLAY_RED, OVERLAY_RED);
        }
        else
        {
            this.drawGradientRect(gui, left, top, right, bottom, startColor, endColor);
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        this.craftingPatternSelection.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void onGuiClosed()
    {
        EnumUtils.InventoryType.resetCurrentInventoryType();

        if (this.craftingPatternSelection != null)
        {
            this.craftingPatternSelection.onGuiClosed();
        }
    }

    public Object getInventoryType()
    {
        return this.inventoryType;
    }

    public Object getCraftingPatternSelection()
    {
        return this.craftingPatternSelection;
    }

    public boolean isCraftingPattern()
    {
        return this.inventoryType == EnumUtils.InventoryType.CRAFTING_TABLE;
    }

    public boolean isNonCraftingPattern()
    {
        return this.inventoryType == EnumUtils.InventoryType.ENCHANTMENT_TABLE || this.inventoryType == EnumUtils.InventoryType.REFORGE_ANVIL;
    }
}
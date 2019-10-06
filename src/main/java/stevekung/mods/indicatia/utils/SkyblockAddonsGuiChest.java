package stevekung.mods.indicatia.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ConfigColor;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.IInventory;

public class SkyblockAddonsGuiChest
{
    private EnumUtils.InventoryType inventoryType = null;

    public GuiTextField[] initGui(IInventory lowerChestInventory, FontRenderer fontRendererObj, int guiTop, int guiLeft)
    {
        GuiTextField[] textFields = new GuiTextField[2];
        String guiName = lowerChestInventory.getDisplayName().getUnformattedText();

        if (guiName.equals("Enchant Item"))
        {
            this.inventoryType = EnumUtils.InventoryType.ENCHANTMENT_TABLE;
        }
        if (guiName.equals("Reforge Item"))
        {
            this.inventoryType = EnumUtils.InventoryType.REFORGE_ANVIL;
        }

        if (this.inventoryType != null)
        {
            int xPos = guiLeft - 160;

            if (xPos<0)
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
            mc.ingameGUI.drawString(mc.fontRendererObj, "ex. \"prot, feather\"", x+4, guiTop + 86, ConfigColor.DARK_GRAY.getColor(255));
        }

        textFieldExclusions.drawTextBox();

        if (textFieldExclusions.getText().equals(""))
        {
            mc.ingameGUI.drawString(mc.fontRendererObj, "ex. \"proj, blast\"", x+4, guiTop + 126, ConfigColor.DARK_GRAY.getColor(255));
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

    public Object getInventoryType()
    {
        return this.inventoryType;
    }
}
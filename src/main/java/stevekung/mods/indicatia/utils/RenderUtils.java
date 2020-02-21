package stevekung.mods.indicatia.utils;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.utils.ColorUtils.RGB;

public class RenderUtils
{
    private static final ResourceLocation RARITY = new ResourceLocation("indicatia:textures/gui/rarity.png");

    public static void bindTexture(ResourceLocation resource)
    {
        Minecraft.getMinecraft().getTextureManager().bindTexture(resource);
    }

    public static void bindTexture(String resource)
    {
        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(resource));
    }

    public static void disableLighting()
    {
        GlStateManager.disableLighting();
        GlStateManager.disableLight(0);
        GlStateManager.disableLight(1);
        GlStateManager.disableColorMaterial();
    }

    public static void enableLighting()
    {
        GlStateManager.enableLighting();
        GlStateManager.enableLight(0);
        GlStateManager.enableLight(1);
        GlStateManager.enableColorMaterial();
    }

    public static ItemStack getSkullItemStack(String skullId, String skullValue)
    {
        ItemStack itemStack = new ItemStack(Items.skull, 1, 3);
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagCompound properties = new NBTTagCompound();
        properties.setString("Id", skullId);
        NBTTagCompound texture = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        NBTTagCompound value = new NBTTagCompound();
        value.setString("Value", skullValue);
        list.appendTag(value);
        texture.setTag("textures", list);
        properties.setTag("Properties", texture);
        compound.setTag("SkullOwner", properties);
        itemStack.setTagCompound(compound);
        return itemStack;
    }

    public static void drawRarity(Slot slot, IInventory lowerChestInventory)
    {
        if (lowerChestInventory != null)
        {
            String name = lowerChestInventory.getDisplayName().getUnformattedText();

            if (name.equals("Auctions Browser") && slot.slotNumber >= 0 && slot.slotNumber <= 53)
            {
                return;
            }
            else if ((name.equals("Manage Auctions") || name.equals("Your Bids")) && slot.slotNumber >= 0 && slot.slotNumber <= 26)
            {
                return;
            }
            else if (name.endsWith("'s Auctions") && slot.slotNumber >= 0 && slot.slotNumber <= 35)
            {
                return;
            }
        }
        RenderUtils.drawRarity(slot.getStack(), slot.xDisplayPosition, slot.yDisplayPosition);
    }

    public static void drawRarity(ItemStack itemStack, int xPos, int yPos)
    {
        if (itemStack != null && itemStack.hasTagCompound())
        {
            NBTTagCompound compound = itemStack.getTagCompound().getCompoundTag("display");

            if (compound.getTagId("Lore") == 9)
            {
                NBTTagList list = compound.getTagList("Lore", 8);

                if (list.tagCount() > 0)
                {
                    for (int j1 = 0; j1 < list.tagCount(); ++j1)
                    {
                        String lore = list.getStringTagAt(j1);
                        RGB common = ColorUtils.stringToRGB("255,255,255");
                        RGB uncommon = ColorUtils.stringToRGB("85,255,85");
                        RGB rare = ColorUtils.stringToRGB("85,85,255");
                        RGB epic = ColorUtils.stringToRGB("170,0,170");
                        RGB legendary = ColorUtils.stringToRGB("255,170,0");
                        RGB special = ColorUtils.stringToRGB("255,85,255");

                        if (lore.startsWith(EnumChatFormatting.WHITE + "" + EnumChatFormatting.BOLD + "COMMON"))
                        {
                            RenderUtils.renderRarity(xPos, yPos, common);
                        }
                        else if (lore.startsWith(EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "UNCOMMON"))
                        {
                            RenderUtils.renderRarity(xPos, yPos, uncommon);
                        }
                        else if (lore.startsWith(EnumChatFormatting.BLUE + "" + EnumChatFormatting.BOLD + "RARE"))
                        {
                            RenderUtils.renderRarity(xPos, yPos, rare);
                        }
                        else if (lore.startsWith(EnumChatFormatting.DARK_PURPLE + "" + EnumChatFormatting.BOLD + "EPIC"))
                        {
                            RenderUtils.renderRarity(xPos, yPos, epic);
                        }
                        else if (lore.startsWith(EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "LEGENDARY"))
                        {
                            RenderUtils.renderRarity(xPos, yPos, legendary);
                        }
                        else if (lore.startsWith(EnumChatFormatting.LIGHT_PURPLE + "" + EnumChatFormatting.BOLD + "SPECIAL"))
                        {
                            RenderUtils.renderRarity(xPos, yPos, special);
                        }
                    }
                }
            }
        }
    }

    private static void renderRarity(int xPos, int yPos, RGB color)
    {
        float alpha = ExtendedConfig.instance.itemRarityOpacity / 100.0F;
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        RenderUtils.bindTexture(RARITY);
        GlStateManager.color(color.floatRed(), color.floatGreen(), color.floatBlue(), alpha);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
        Gui.drawModalRectWithCustomSizedTexture(xPos, yPos, 0, 0, 16, 16, 16, 16);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableAlpha();
    }
}
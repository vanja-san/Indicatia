package stevekung.mods.indicatia.mixin;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.event.HUDRenderEventHandler;
import stevekung.mods.indicatia.utils.ColorUtils;
import stevekung.mods.indicatia.utils.ColorUtils.RGB;

@Mixin(GuiIngame.class)
public abstract class GuiIngameMixin
{
    private static final ResourceLocation RARITY = new ResourceLocation("indicatia:textures/gui/rarity.png");

    @Shadow
    @Final
    @Mutable
    protected Minecraft mc;

    @Inject(method = "func_181029_i()V", at = @At("RETURN"))
    private void resetToast(CallbackInfo info)
    {
        HUDRenderEventHandler.INSTANCE.getToastGui().clear();
    }

    @Inject(method = "renderHotbarItem(IIIFLnet/minecraft/entity/player/EntityPlayer;)V", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/entity/RenderItem.renderItemAndEffectIntoGUI(Lnet/minecraft/item/ItemStack;II)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void renderRarity(int index, int xPos, int yPos, float partialTicks, EntityPlayer player, CallbackInfo info, ItemStack itemstack, float f)
    {
        if (ExtendedConfig.instance.showItemRarity)
        {
            this.drawRarity(itemstack, xPos, yPos);
        }
    }

    private void drawRarity(ItemStack itemStack, int xPos, int yPos)
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
                            GuiIngameMixin.renderRarity(xPos, yPos, common);
                        }
                        else if (lore.startsWith(EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "UNCOMMON"))
                        {
                            GuiIngameMixin.renderRarity(xPos, yPos, uncommon);
                        }
                        else if (lore.startsWith(EnumChatFormatting.BLUE + "" + EnumChatFormatting.BOLD + "RARE"))
                        {
                            GuiIngameMixin.renderRarity(xPos, yPos, rare);
                        }
                        else if (lore.startsWith(EnumChatFormatting.DARK_PURPLE + "" + EnumChatFormatting.BOLD + "EPIC"))
                        {
                            GuiIngameMixin.renderRarity(xPos, yPos, epic);
                        }
                        else if (lore.startsWith(EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "LEGENDARY"))
                        {
                            GuiIngameMixin.renderRarity(xPos, yPos, legendary);
                        }
                        else if (lore.startsWith(EnumChatFormatting.LIGHT_PURPLE + "" + EnumChatFormatting.BOLD + "SPECIAL"))
                        {
                            GuiIngameMixin.renderRarity(xPos, yPos, special);
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
        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiIngameMixin.RARITY);
        GlStateManager.color(color.floatRed(), color.floatGreen(), color.floatBlue(), alpha);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
        Gui.drawModalRectWithCustomSizedTexture(xPos, yPos, 0, 0, 16, 16, 16, 16);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }
}
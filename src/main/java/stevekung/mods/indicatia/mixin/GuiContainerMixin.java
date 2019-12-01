package stevekung.mods.indicatia.mixin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.event.IndicatiaEventHandler;
import stevekung.mods.indicatia.utils.ColorUtils;
import stevekung.mods.indicatia.utils.ColorUtils.RGB;
import stevekung.mods.indicatia.utils.ITradeGUI;
import stevekung.mods.indicatia.utils.SearchMode;

@Mixin(GuiContainer.class)
public abstract class GuiContainerMixin extends GuiScreen
{
    private final GuiContainer that = (GuiContainer) (Object) this;
    private static final List<String> IGNORE_ITEMS = new ArrayList<>(Arrays.asList(" ", "Recipe Required", "Item To Upgrade", "Rune to Sacrifice", "Runic Pedestal", "Final confirmation"));
    private static final List<String> IGNORE_TOOLTIPS = new ArrayList<>(Arrays.asList(" "));
    private static final ResourceLocation RARITY = new ResourceLocation("indicatia:textures/gui/rarity.png");
    private SearchMode mode = SearchMode.SIMPLE;

    @Inject(method = "handleMouseClick(Lnet/minecraft/inventory/Slot;III)V", cancellable = true, at = @At("HEAD"))
    private void handleMouseClick(Slot slot, int slotId, int clickedButton, int clickType, CallbackInfo info)
    {
        if (slotId != -999 && slotId != -1)
        {
            ItemStack itemStack = this.that.inventorySlots.getSlot(slotId).getStack();

            if (itemStack != null)
            {
                if (this.ignoreNullItem(itemStack, IGNORE_ITEMS))
                {
                    info.cancel();
                }
            }
        }
    }

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/entity/RenderItem.renderItemAndEffectIntoGUI(Lnet/minecraft/item/ItemStack;II)V"))
    private void renderRarity(Slot slot, CallbackInfo info)
    {
        if (ExtendedConfig.instance.showItemRarity)
        {
            this.drawRarity(slot);
        }
    }

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItemOverlayIntoGUI(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V"))
    private void renderBids(Slot slot, CallbackInfo info)
    {
        if (this.isAuctionBrowser())
        {
            this.drawBids(slot);
        }
    }

    @Override
    protected void renderToolTip(ItemStack itemStack, int x, int y)
    {
        if (itemStack != null)
        {
            if (this.ignoreNullItem(itemStack, IGNORE_TOOLTIPS))
            {
                return;
            }
        }
        super.renderToolTip(itemStack, x, y);
    }

    private boolean isAuctionBrowser()
    {
        GuiScreen gui = this.mc.currentScreen;

        if (gui != null && gui instanceof GuiChest)
        {
            GuiChest chest = (GuiChest)gui;
            return chest.lowerChestInventory.getDisplayName().getUnformattedText().equals("Auctions Browser");
        }
        else
        {
            return false;
        }
    }

    private boolean ignoreNullItem(ItemStack itemStack, List<String> ignores)
    {
        String displayName = EnumChatFormatting.getTextWithoutFormattingCodes(itemStack.getDisplayName());
        return ignores.stream().anyMatch(name -> displayName.equals(name));
    }

    private void drawRarity(Slot slot)
    {
        if (slot.getStack() != null && slot.getStack().hasTagCompound())
        {
            NBTTagCompound compound = slot.getStack().getTagCompound().getCompoundTag("display");

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

                        if (this.isAuctionBrowser() && slot.slotNumber >= 0 && slot.slotNumber <= 53)
                        {
                            return;
                        }

                        if (lore.startsWith(EnumChatFormatting.WHITE + "" + EnumChatFormatting.BOLD + "COMMON"))
                        {
                            this.renderRarity(slot, common);
                        }
                        else if (lore.startsWith(EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "UNCOMMON"))
                        {
                            this.renderRarity(slot, uncommon);
                        }
                        else if (lore.startsWith(EnumChatFormatting.BLUE + "" + EnumChatFormatting.BOLD + "RARE"))
                        {
                            this.renderRarity(slot, rare);
                        }
                        else if (lore.startsWith(EnumChatFormatting.DARK_PURPLE + "" + EnumChatFormatting.BOLD + "EPIC"))
                        {
                            this.renderRarity(slot, epic);
                        }
                        else if (lore.startsWith(EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "LEGENDARY"))
                        {
                            this.renderRarity(slot, legendary);
                        }
                        else if (lore.startsWith(EnumChatFormatting.LIGHT_PURPLE + "" + EnumChatFormatting.BOLD + "SPECIAL"))
                        {
                            this.renderRarity(slot, special);
                        }
                    }
                }
            }
        }
    }

    private void drawBids(Slot slot)
    {
        if (slot.getStack() != null && slot.getStack().hasTagCompound())
        {
            NBTTagCompound compound = slot.getStack().getTagCompound().getCompoundTag("display");

            if (compound.getTagId("Lore") == 9)
            {
                NBTTagList list = compound.getTagList("Lore", 8);

                if (list.tagCount() > 0)
                {
                    for (int j1 = 0; j1 < list.tagCount(); ++j1)
                    {
                        int slotLeft = slot.xDisplayPosition;
                        int slotTop = slot.yDisplayPosition;
                        int slotRight = slotLeft + 16;
                        int slotBottom = slotTop + 16;
                        String lore = EnumChatFormatting.getTextWithoutFormattingCodes(list.getStringTagAt(j1));
                        Matcher matcher = Pattern.compile("(?:Top|Starting) bid: (?<coin>[0-9,]+) coins").matcher(lore);
                        int red = ColorUtils.to32BitColor(128, 255, 85, 85);
                        int green = ColorUtils.to32BitColor(128, 85, 255, 85);
                        int yellow = ColorUtils.to32BitColor(128, 255, 255, 85);

                        if (((ITradeGUI)(GuiChest)this.mc.currentScreen).getNumberField().getText().isEmpty())
                        {
                            if (lore.startsWith("Starting bid:"))
                            {
                                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, green, green);
                            }
                            else if (lore.startsWith("Bidder:"))
                            {
                                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, red, red);
                            }
                        }
                        else
                        {
                            try
                            {
                                int priceMin = 0;
                                int priceMax = 0;
                                int moneyFromText = 0;

                                if (matcher.matches())
                                {
                                    String[] priceSplit = IndicatiaEventHandler.auctionPrice.split("\\.\\.");
                                    int moneyFromAh = Integer.parseInt(matcher.group("coin").replaceAll("[^\\d.]+", ""));

                                    if (IndicatiaEventHandler.auctionPrice.matches("[\\d]+\\.\\.[\\d]+"))
                                    {
                                        priceMin = Integer.parseInt(priceSplit[0]);
                                        priceMax = Integer.parseInt(priceSplit[1]);
                                        this.mode = SearchMode.RANGED;
                                    }
                                    else if (IndicatiaEventHandler.auctionPrice.matches("[\\d]+\\.\\."))
                                    {
                                        priceMin = Integer.parseInt(IndicatiaEventHandler.auctionPrice.replaceAll("\\.\\.", ""));
                                        this.mode = SearchMode.MIN;
                                    }
                                    else if (IndicatiaEventHandler.auctionPrice.matches("\\.\\.[\\d]+"))
                                    {
                                        priceMax = Integer.parseInt(IndicatiaEventHandler.auctionPrice.replaceAll("\\.\\.", ""));
                                        this.mode = SearchMode.MAX;
                                    }
                                    else
                                    {
                                        moneyFromText = Integer.parseInt(IndicatiaEventHandler.auctionPrice);
                                        this.mode = SearchMode.SIMPLE;
                                    }

                                    if (lore.startsWith("Top bid:"))
                                    {
                                        this.checkCondition(moneyFromText, moneyFromAh, priceMin, priceMax, slotLeft, slotTop, slotRight, slotBottom, yellow, red);
                                    }
                                    else if (lore.startsWith("Starting bid:"))
                                    {
                                        this.checkCondition(moneyFromText, moneyFromAh, priceMin, priceMax, slotLeft, slotTop, slotRight, slotBottom, green, red);
                                    }
                                }
                            }
                            catch (Exception e) {}
                        }
                    }
                }
            }
        }
    }

    private void checkCondition(int moneyFromText, int moneyFromAh, int priceMin, int priceMax, int slotLeft, int slotTop, int slotRight, int slotBottom, int color1, int color2)
    {
        switch (this.mode)
        {
        default:
        case SIMPLE:
            if (moneyFromText == moneyFromAh)
            {
                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, color1, color1);
            }
            else
            {
                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, color2, color2);
            }
            break;
        case MIN:
            if (moneyFromAh >= priceMin)
            {
                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, color1, color1);
            }
            else
            {
                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, color2, color2);
            }
            break;
        case MAX:
            if (moneyFromAh <= priceMax)
            {
                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, color1, color1);
            }
            else
            {
                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, color2, color2);
            }
            break;
        case RANGED:
            if (moneyFromAh >= priceMin && moneyFromAh <= priceMax)
            {
                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, color1, color1);
            }
            else
            {
                this.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, color2, color2);
            }
            break;
        }
    }

    private void renderRarity(Slot slot, RGB color)
    {
        float alpha = ExtendedConfig.instance.itemRarityOpacity / 100.0F;
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        this.mc.getTextureManager().bindTexture(GuiContainerMixin.RARITY);
        GlStateManager.color(color.floatRed(), color.floatGreen(), color.floatBlue(), alpha);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);
        Gui.drawModalRectWithCustomSizedTexture(slot.xDisplayPosition, slot.yDisplayPosition, 0, 0, 16, 16, 16, 16);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }
}
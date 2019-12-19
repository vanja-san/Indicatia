package stevekung.mods.indicatia.gui.toasts;

import java.util.Random;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stevekung.mods.indicatia.gui.toasts.ItemDropsToast.ItemDrop;
import stevekung.mods.indicatia.renderer.HUDInfo;
import stevekung.mods.indicatia.utils.ColorUtils;
import stevekung.mods.indicatia.utils.JsonUtils;

@SideOnly(Side.CLIENT)
public class GiftToast implements IToast
{
    private final Random rand = new Random();
    private final ResourceLocation texture;
    private final ItemDrop rareDropOutput;
    private long firstDrawTime;
    private boolean hasNewStacks;

    public GiftToast(ItemStack itemStack, ItemDropsToast.Type rarity)
    {
        this.rareDropOutput = new ItemDrop(itemStack, rarity);
        this.texture = new ResourceLocation("indicatia:textures/gui/gift_toasts_" + Integer.valueOf(1 + this.rand.nextInt(2)) + ".png");
    }

    @Override
    public IToast.Visibility draw(GuiToast toastGui, long delta)
    {
        if (this.hasNewStacks)
        {
            this.firstDrawTime = delta;
            this.hasNewStacks = false;
        }

        if (this.rareDropOutput == null)
        {
            return IToast.Visibility.HIDE;
        }
        else
        {
            ItemDrop drop = this.rareDropOutput;
            ItemStack itemStack = drop.getItemStack();
            toastGui.mc.getTextureManager().bindTexture(this.texture);
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 160, 32, 160, 32);
            toastGui.mc.fontRendererObj.drawString(drop.getType().getColor() + JsonUtils.create(drop.getType().getName()).setChatStyle(JsonUtils.style().setBold(true)).getFormattedText(), 30, 7, 16777215);
            toastGui.mc.fontRendererObj.drawString(itemStack.getDisplayName(), 30, 18, ColorUtils.rgbToDecimal(255, 255, 255));
            HUDInfo.renderItem(itemStack, 8, 8);
            return delta - this.firstDrawTime >= 15000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
        }
    }
}
package stevekung.mods.indicatia.gui.toasts;

import java.nio.FloatBuffer;
import java.util.Random;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import stevekung.mods.indicatia.renderer.HUDInfo;
import stevekung.mods.indicatia.utils.JsonUtils;

public class GiftToast implements IToast
{
    private final Random rand = new Random();
    private final ResourceLocation texture;
    private final ToastUtils.ItemDrop rareDropOutput;
    private long firstDrawTime;
    private boolean hasNewStacks;
    private final FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);

    public GiftToast(ItemStack itemStack, ToastUtils.DropType rarity, boolean santaGift)
    {
        this.rareDropOutput = new ToastUtils.ItemDrop(itemStack, rarity);
        this.texture = new ResourceLocation("indicatia:textures/gui/gift_toasts_" + (santaGift ? 1 : Integer.valueOf(1 + this.rand.nextInt(2))) + ".png");
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
            ToastUtils.ItemDrop drop = this.rareDropOutput;
            ItemStack itemStack = drop.getItemStack();
            String itemName = itemStack.getDisplayName();
            toastGui.mc.getTextureManager().bindTexture(this.texture);
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 160, 32, 160, 32);
            toastGui.mc.fontRendererObj.drawString(drop.getType().getColor() + JsonUtils.create(drop.getType().getName()).setChatStyle(JsonUtils.style().setBold(true)).getFormattedText(), 30, 7, 16777215);
            GuiToast.drawLongItemName(toastGui, delta, this.firstDrawTime, this.buffer, itemName, 10000L, 5000L);
            HUDInfo.renderItem(itemStack, 8, 8);
            return delta - this.firstDrawTime >= 10000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
        }
    }
}
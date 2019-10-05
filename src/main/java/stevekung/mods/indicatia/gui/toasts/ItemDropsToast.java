package stevekung.mods.indicatia.gui.toasts;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stevekung.mods.indicatia.renderer.HUDInfo;
import stevekung.mods.indicatia.utils.ColorUtils;
import stevekung.mods.indicatia.utils.JsonUtils;
import stevekung.mods.indicatia.utils.LangUtils;

@SideOnly(Side.CLIENT)
public class ItemDropsToast implements IToast
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("indicatia:textures/gui/toasts.png");
    private final List<ItemStack> rareDropOutput = new ArrayList<>();
    private long firstDrawTime;
    private boolean hasNewStacks;

    public ItemDropsToast(ItemStack itemStack)
    {
        this.rareDropOutput.add(itemStack);
    }

    @Override
    public IToast.Visibility draw(GuiToast toastGui, long delta)
    {
        if (this.hasNewStacks)
        {
            this.firstDrawTime = delta;
            this.hasNewStacks = false;
        }

        if (this.rareDropOutput.isEmpty())
        {
            return IToast.Visibility.HIDE;
        }
        else
        {
            ItemStack itemStack = this.rareDropOutput.get((int)(delta * this.rareDropOutput.size() / 5000L % this.rareDropOutput.size()));
            toastGui.mc.getTextureManager().bindTexture(TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 160, 32, 160, 32);
            toastGui.mc.fontRendererObj.drawString(JsonUtils.create(LangUtils.translate("RARE DROP!")).setChatStyle(JsonUtils.gold().setBold(true)).getFormattedText(), 30, 7, 16777215);
            toastGui.mc.fontRendererObj.drawString(itemStack.getDisplayName(), 30, 18, ColorUtils.rgbToDecimal(255, 255, 255));
            HUDInfo.renderItem(itemStack, 8, 8);
            return delta - this.firstDrawTime >= 10000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
        }
    }
}
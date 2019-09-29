package stevekung.mods.indicatia.gui.toasts;

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
    private final ItemStack itemStack;
    private long firstDrawTime;
    private boolean hasNewStacks;

    public ItemDropsToast(ItemStack itemStack)
    {
        this.itemStack = itemStack;
    }

    @Override
    public IToast.Visibility draw(GuiToast toastGui, long delta)
    {
        if (this.hasNewStacks)
        {
            this.firstDrawTime = delta;
            this.hasNewStacks = false;
        }

        if (this.itemStack == null)
        {
            return IToast.Visibility.HIDE;
        }
        else
        {
            toastGui.mc.getTextureManager().bindTexture(TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 160, 32, 160, 32);
            toastGui.mc.fontRendererObj.drawString(JsonUtils.create(LangUtils.translate("RARE DROP!")).setChatStyle(JsonUtils.gold().setBold(true)).getFormattedText(), 30, 7, 16777215);
            toastGui.mc.fontRendererObj.drawString(this.itemStack.getDisplayName(), 30, 18, ColorUtils.rgbToDecimal(255, 255, 255));
            HUDInfo.renderItem(this.itemStack, 8, 8);
            return delta - this.firstDrawTime >= 5000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
        }
    }

    public void addItemStack(ItemStack itemStack)
    {
        if (this.itemStack != null)
        {
            this.hasNewStacks = true;
        }
    }

    public static void addOrUpdate(GuiToast guiToast, ItemStack itemStack)
    {
        ItemDropsToast toast = guiToast.getToast(ItemDropsToast.class, NO_TOKEN);

        if (toast == null)
        {
            guiToast.add(new ItemDropsToast(itemStack));
        }
        else
        {
            toast.addItemStack(itemStack);
        }
    }
}
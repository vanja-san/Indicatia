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

@SideOnly(Side.CLIENT)
public class ItemDropsToast implements IToast
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("indicatia:textures/gui/toasts.png");
    private final List<ItemDrop> rareDropOutput = new ArrayList<>();
    private long firstDrawTime;
    private boolean hasNewStacks;

    public ItemDropsToast(ItemStack itemStack, Type type)
    {
        this.rareDropOutput.add(new ItemDrop(itemStack, type));
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
            ItemDrop drop = this.rareDropOutput.get((int)(delta * this.rareDropOutput.size() / 5000L % this.rareDropOutput.size()));
            ItemStack itemStack = drop.getItemStack();
            toastGui.mc.getTextureManager().bindTexture(TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 160, 32, 160, 32);
            toastGui.mc.fontRendererObj.drawString(drop.getType().getColor() + JsonUtils.create(drop.getType().getName()).setChatStyle(JsonUtils.style().setBold(true)).getFormattedText(), 30, 7, 16777215);
            toastGui.mc.fontRendererObj.drawString(itemStack.getDisplayName(), 30, 18, ColorUtils.rgbToDecimal(255, 255, 255));
            HUDInfo.renderItem(itemStack, 8, 8);
            return delta - this.firstDrawTime >= 10000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
        }
    }

    public void addItemStack(ItemStack itemStack, Type type)
    {
        if (this.rareDropOutput.add(new ItemDrop(itemStack, type)))
        {
            this.hasNewStacks = true;
        }
    }

    public static void addOrUpdate(GuiToast guiToast, ItemStack itemStack, Type type)
    {
        ItemDropsToast toast = guiToast.getToast(ItemDropsToast.class, NO_TOKEN);

        if (toast == null)
        {
            guiToast.add(new ItemDropsToast(itemStack, type));
        }
        else
        {
            toast.addItemStack(itemStack, type);
        }
    }

    public static class ItemDrop
    {
        private final ItemStack itemStack;
        private final Type type;

        private ItemDrop(ItemStack itemStack, Type type)
        {
            this.itemStack = itemStack;
            this.type = type;
        }

        public ItemStack getItemStack()
        {
            return this.itemStack;
        }

        public Type getType()
        {
            return this.type;
        }
    }

    public enum Type
    {
        RARE_DROP("RARE DROP!", "255,170,0"),
        DRAGON_CRYSTAL_FRAGMENT("RARE DROP!", "170,0,170"),
        DRAGON_DROP("DRAGON DROP!", "127,255,212"),
        GOOD_CATCH("GOOD CATCH!", "255,170,0"),
        GREAT_CATCH("GREAT CATCH!", "170,0,170"),
        GOOD_CATCH_COINS("GOOD CATCH!", "255,170,0"),
        GREAT_CATCH_COINS("GREAT CATCH!", "170,0,170");

        private final String name;
        private final String color;

        private Type(String name, String color)
        {
            this.name = name;
            this.color = color;
        }

        public String getName()
        {
            return this.name;
        }

        public String getColor()
        {
            return ColorUtils.stringToRGB(this.color).toColoredFont();
        }
    }
}
package stevekung.mods.indicatia.gui.toasts;

import java.nio.FloatBuffer;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import stevekung.mods.indicatia.event.ClientEventHandler;
import stevekung.mods.indicatia.renderer.HUDInfo;
import stevekung.mods.indicatia.utils.ColorUtils;
import stevekung.mods.indicatia.utils.JsonUtils;

public class ItemDropsToast implements IToast
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("indicatia:textures/gui/drop_toasts.png");
    private static final ResourceLocation MAGIC_FIND_GLINT = new ResourceLocation("indicatia:textures/gui/magic_find_glint.png");
    private final ToastUtils.ItemDrop rareDropOutput;
    private long firstDrawTime;
    private boolean hasNewStacks;
    private final FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);
    private String magicFind;

    public ItemDropsToast(ItemStack itemStack, ToastUtils.DropType type)
    {
        this(itemStack, type, null);
    }

    public ItemDropsToast(ItemStack itemStack, ToastUtils.DropType type, String magicFind)
    {
        this.rareDropOutput = new ToastUtils.ItemDrop(itemStack, type);
        this.magicFind = magicFind != null ? EnumChatFormatting.AQUA + " (" + magicFind + "% Magic Find!)" : "";
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
            String itemName = itemStack.getDisplayName() + this.magicFind;
            float partialTicks = ClientEventHandler.renderPartialTicks;

            if (itemStack.getItem() == Items.enchanted_book)
            {
                itemName = itemStack.getTooltip(null, false).get(1) + this.magicFind;
            }

            if (!StringUtils.isNullOrEmpty(this.magicFind))
            {
                toastGui.mc.getTextureManager().bindTexture(TEXTURE);
                GlStateManager.color(1.0F, 1.0F, 1.0F);
                Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 160, 32, 160, 32);

                GlStateManager.enableBlend();
                GlStateManager.depthFunc(514);

                for (int i = 0; i < 2; ++i)
                {
                    GlStateManager.disableLighting();
                    GlStateManager.blendFunc(768, 1);
                    ColorUtils.RGB rgb = ColorUtils.stringToRGB("85,255,255");
                    GlStateManager.color(rgb.floatRed(), rgb.floatGreen(), rgb.floatBlue(), 0.25F);
                    GlStateManager.matrixMode(5890);
                    GlStateManager.loadIdentity();
                    GlStateManager.scale(0.2F, 0.2F, 0.2F);
                    GlStateManager.rotate(30.0F - i * 60.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.translate(0.0F, partialTicks * (0.001F + i * 0.003F) * 20.0F, 0.0F);
                    GlStateManager.matrixMode(5888);

                    toastGui.mc.getTextureManager().bindTexture(MAGIC_FIND_GLINT);
                    GlStateManager.blendFunc(770, 771);
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 160, 32, 160, 32);
                }

                GlStateManager.matrixMode(5890);
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(5888);
                GlStateManager.enableLighting();
                GlStateManager.depthFunc(515);
                GlStateManager.disableBlend();
            }
            else
            {
                toastGui.mc.getTextureManager().bindTexture(TEXTURE);
                GlStateManager.color(1.0F, 1.0F, 1.0F);
                Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 160, 32, 160, 32);
            }

            RenderHelper.disableStandardItemLighting();
            toastGui.mc.fontRendererObj.drawString(drop.getType().getColor() + JsonUtils.create(drop.getType().getName()).setChatStyle(JsonUtils.style().setBold(true)).getFormattedText(), 30, 7, 16777215);
            GuiToast.drawLongItemName(toastGui, delta, this.firstDrawTime, this.buffer, itemName);
            RenderHelper.enableGUIStandardItemLighting();

            HUDInfo.renderItem(itemStack, 8, 8);
            return delta - this.firstDrawTime >= 15000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
        }
    }
}
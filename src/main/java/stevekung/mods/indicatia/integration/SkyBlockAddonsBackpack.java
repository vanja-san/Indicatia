package stevekung.mods.indicatia.integration;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.*;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class SkyBlockAddonsBackpack extends GuiScreen
{
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    public void drawBackpacks(int mouseX, int mouseY, float partialTicks)
    {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Backpack backpack = main.getUtils().getBackpackToRender();

        if (backpack != null)
        {
            int x = backpack.getX();
            int y = backpack.getY();
            ItemStack[] items = backpack.getItems();
            int length = items.length;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (main.getConfigValues().getBackpackStyle() == EnumUtils.BackpackStyle.GUI)
            {
                this.mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                int rows = length/9;
                GlStateManager.disableLighting();
                GlStateManager.pushMatrix();
                GlStateManager.translate(0,0,300);
                int textColor = 4210752;

                if (main.getConfigValues().isEnabled(Feature.MAKE_BACKPACK_INVENTORIES_COLORED))
                {
                    BackpackColor color = backpack.getBackpackColor();
                    GlStateManager.color(color.getR(), color.getG(), color.getB(), 1);
                    textColor = color.getTextColor();
                }

                this.drawTexturedModalRect(x, y, 0, 0, 176, rows * 18 + 17);
                this.drawTexturedModalRect(x, y + rows * 18 + 17, 0, 215, 176, 7);
                this.mc.fontRendererObj.drawString(backpack.getBackpackName(), x+8, y+6, textColor);
                GlStateManager.popMatrix();
                GlStateManager.enableLighting();

                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();

                for (int i = 0; i < length; i++)
                {
                    ItemStack item = items[i];

                    if (item != null)
                    {
                        int itemX = x+8 + i % 9 * 18;
                        int itemY = y+18 + i / 9 * 18;
                        RenderItem renderItem = this.mc.getRenderItem();
                        this.zLevel = 200;
                        renderItem.zLevel = 200;
                        renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY);
                        renderItem.renderItemOverlayIntoGUI(this.mc.fontRendererObj, item, itemX, itemY, null);
                        this.zLevel = 0;
                        renderItem.zLevel = 0;
                    }
                }
            }
            else
            {
                GlStateManager.disableLighting();
                GlStateManager.pushMatrix();
                GlStateManager.translate(0,0, 300);
                Gui.drawRect(x, y, x + 16 * 9 + 3, y + 16 * (length / 9) + 3, ConfigColor.DARK_GRAY.getColor(250));
                GlStateManager.popMatrix();
                GlStateManager.enableLighting();

                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();

                for (int i = 0; i < length; i++)
                {
                    ItemStack item = items[i];

                    if (item != null)
                    {
                        int itemX = x + i % 9 * 16;
                        int itemY = y + i / 9 * 16;
                        RenderItem renderItem = this.mc.getRenderItem();
                        this.zLevel = 200;
                        renderItem.zLevel = 200;
                        renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY);
                        renderItem.renderItemOverlayIntoGUI(this.mc.fontRendererObj, item, itemX, itemY, null);
                        this.zLevel = 0;
                        renderItem.zLevel = 0;
                    }
                }
            }
            main.getUtils().setBackpackToRender(null);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
        }
    }
}
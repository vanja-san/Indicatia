package stevekung.mods.indicatia.integration;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class SkyBlockAddonsBackpack
{
    public static final SkyBlockAddonsBackpack INSTANCE = new SkyBlockAddonsBackpack();
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private float zLevel;

    public void drawBackpacks(int mouseX, int mouseY, float partialTicks)
    {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Backpack backpack = main.getUtils().getBackpackToRender();
        Minecraft mc = Minecraft.getMinecraft();

        if (backpack != null)
        {
            int x = backpack.getX();
            int y = backpack.getY();
            ItemStack[] items = backpack.getItems();
            int length = items.length;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (main.getConfigValues().getBackpackStyle() == EnumUtils.BackpackStyle.GUI)
            {
                mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
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
                mc.fontRendererObj.drawString(backpack.getBackpackName(), x+8, y+6, textColor);
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
                        RenderItem renderItem = mc.getRenderItem();
                        this.zLevel = 200;
                        renderItem.zLevel = 200;
                        renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY);
                        renderItem.renderItemOverlayIntoGUI(mc.fontRendererObj, item, itemX, itemY, null);
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
                        RenderItem renderItem = mc.getRenderItem();
                        this.zLevel = 200;
                        renderItem.zLevel = 200;
                        renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY);
                        renderItem.renderItemOverlayIntoGUI(mc.fontRendererObj, item, itemX, itemY, null);
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

    private void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
    {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x + 0, y + height, this.zLevel).tex((textureX + 0) * f, (textureY + height) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, this.zLevel).tex((textureX + width) * f, (textureY + height) * f1).endVertex();
        worldrenderer.pos(x + width, y + 0, this.zLevel).tex((textureX + width) * f, (textureY + 0) * f1).endVertex();
        worldrenderer.pos(x + 0, y + 0, this.zLevel).tex((textureX + 0) * f, (textureY + 0) * f1).endVertex();
        tessellator.draw();
    }
}
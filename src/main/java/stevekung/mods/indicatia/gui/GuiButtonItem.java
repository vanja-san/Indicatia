package stevekung.mods.indicatia.gui;

import java.util.Collections;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButtonItem extends GuiButton
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("indicatia:textures/gui/blank.png");
    private final int originalX;
    private final int potionX;
    private final Item item;

    public GuiButtonItem(int buttonID, int xPos, int yPos, int potionX, Item item)
    {
        super(buttonID, xPos, yPos, 18, 18, "");
        this.originalX = xPos;
        this.potionX = potionX;
        this.item = item;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        boolean hasVisibleEffect = false;

        for (PotionEffect potioneffect : mc.thePlayer.getActivePotionEffects())
        {
            Potion potion = Potion.potionTypes[potioneffect.getPotionID()];

            if (potion.shouldRender(potioneffect))
            {
                hasVisibleEffect = true;
                break;
            }
        }

        if (!mc.thePlayer.getActivePotionEffects().isEmpty() && hasVisibleEffect)
        {
            this.xPosition = this.potionX;
        }
        else
        {
            this.xPosition = this.originalX;
        }

        if (this.visible)
        {
            ItemStack itemStack = new ItemStack(this.item);
            boolean flag = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

            mc.getTextureManager().bindTexture(TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            Gui.drawModalRectWithCustomSizedTexture(this.xPosition, this.yPosition, flag ? 18 : 0, 0, this.width, this.height, 36, 18);

            GlStateManager.enableDepth();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, this.xPosition + 1, this.yPosition + 1);

            if (flag)
            {
                GuiUtils.drawHoveringText(Collections.singletonList(this.item.getItemStackDisplayName(itemStack)), mouseX, mouseY, mc.currentScreen.width, mc.currentScreen.height, mc.fontRendererObj.getStringWidth(this.item.getItemStackDisplayName(itemStack)), mc.fontRendererObj);
            }
        }
    }
}
package stevekung.mods.indicatia.mixin;

import java.awt.Color;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.GuiIngameForge;

@Mixin(value = GuiIngameForge.class, remap = false)
public abstract class GuiIngameForgeMixin extends GuiIngame
{
    @Shadow(remap = false)
    private FontRenderer fontrenderer;

    @Shadow(remap = false)
    @Final
    private static int WHITE;

    public GuiIngameForgeMixin(Minecraft mc)
    {
        super(mc);
    }

    @Overwrite
    protected void renderToolHightlight(ScaledResolution res)
    {
        if (this.mc.gameSettings.heldItemTooltips && !this.mc.playerController.isSpectator())
        {
            this.mc.mcProfiler.startSection("toolHighlight");

            if (this.remainingHighlightTicks > 0 && this.highlightingItemStack != null)
            {
                String name = this.highlightingItemStack.getDisplayName();

                if (this.highlightingItemStack.hasDisplayName())
                {
                    name = EnumChatFormatting.ITALIC + name;
                }

                name = this.highlightingItemStack.getItem().getHighlightTip(this.highlightingItemStack, name);
                int opacity = (int)(this.remainingHighlightTicks * 256.0F / 10.0F);

                if (opacity > 255)
                {
                    opacity = 255;
                }
                if (opacity > 0)
                {
                    int y = res.getScaledHeight() - 59;

                    if (!this.mc.playerController.shouldDrawHUD())
                    {
                        y += 14;
                    }

                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    FontRenderer font = this.highlightingItemStack.getItem().getFontRenderer(this.highlightingItemStack);

                    if (font != null)
                    {
                        int x = (res.getScaledWidth() - font.getStringWidth(name)) / 2;
                        font.drawStringWithShadow(name, x, y, WHITE | opacity << 24);
                    }
                    else
                    {
                        int x = (res.getScaledWidth() - this.fontrenderer.getStringWidth(name)) / 2;
                        this.fontrenderer.drawStringWithShadow(name, x, y, WHITE | opacity << 24);
                    }
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                }
            }
            this.mc.mcProfiler.endSection();
        }
        else if (this.mc.thePlayer.isSpectator())
        {
            this.spectatorGui.func_175263_a(res);
        }
    }

    @Overwrite
    protected void renderRecordOverlay(int width, int height, float partialTicks)
    {
        if (this.recordPlayingUpFor > 0)
        {
            this.mc.mcProfiler.startSection("overlayMessage");
            float hue = this.recordPlayingUpFor - partialTicks;
            int opacity = (int)(hue * 256.0F / 20.0F);

            if (opacity > 255)
            {
                opacity = 255;
            }

            if (opacity > 0)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(width / 2, height - 68, 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                int color = this.recordIsPlaying ? Color.HSBtoRGB(hue / 50.0F, 0.7F, 0.6F) & WHITE : WHITE;
                this.fontrenderer.drawString(this.recordPlaying, -this.fontrenderer.getStringWidth(this.recordPlaying) / 2, -4, color | opacity << 24);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
            this.mc.mcProfiler.endSection();
        }
    }
}
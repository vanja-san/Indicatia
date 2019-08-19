package stevekung.mods.indicatia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import stevekung.mods.indicatia.config.ConfigManagerIN;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin
{
    @Inject(method = "renderHand(FI)V", cancellable = true, at = @At("HEAD"))
    private void renderPre(float partialTicks, int xOffset, CallbackInfo ci)
    {
        if (ConfigManagerIN.enableAlternatePlayerModel)
        {
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
        }
    }

    @Inject(method = "renderHand(FI)V", cancellable = true, at = @At("RETURN"))
    private void renderPost(float partialTicks, int xOffset, CallbackInfo ci)
    {
        if (ConfigManagerIN.enableAlternatePlayerModel)
        {
            GlStateManager.disableBlend();
        }
    }
}
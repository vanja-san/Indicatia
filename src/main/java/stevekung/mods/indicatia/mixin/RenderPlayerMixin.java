package stevekung.mods.indicatia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import stevekung.mods.indicatia.config.ConfigManagerIN;

@Mixin(RenderPlayer.class)
public abstract class RenderPlayerMixin
{
    @Inject(method = "doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V", cancellable = true, at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/entity/RenderPlayer.setModelVisibilities(Lnet/minecraft/client/entity/AbstractClientPlayer;)V", shift = At.Shift.AFTER))
    private void renderPre(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci)
    {
        if (ConfigManagerIN.enableAlternatePlayerModel)
        {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        }
    }

    @Inject(method = "doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V", cancellable = true, at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/entity/RendererLivingEntity.doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", shift = At.Shift.AFTER))
    private void renderPost(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci)
    {
        if (ConfigManagerIN.enableAlternatePlayerModel)
        {
            GlStateManager.disableBlend();
        }
    }
}
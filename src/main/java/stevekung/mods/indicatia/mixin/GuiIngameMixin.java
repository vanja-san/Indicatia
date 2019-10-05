package stevekung.mods.indicatia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiIngame;
import stevekung.mods.indicatia.event.HUDRenderEventHandler;

@Mixin(GuiIngame.class)
public abstract class GuiIngameMixin
{
    @Inject(method = "func_181029_i()V", at = @At("RETURN"))
    private void resetToast(CallbackInfo info)
    {
        HUDRenderEventHandler.INSTANCE.getToastGui().clear();
    }
}
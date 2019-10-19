package stevekung.mods.indicatia.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCustomizeSkin;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraftforge.fml.client.FMLClientHandler;
import stevekung.mods.indicatia.gui.optifine.GuiButtonOF;
import stevekung.mods.indicatia.gui.optifine.GuiScreenCapeOF;

@Mixin(GuiCustomizeSkin.class)
public abstract class GuiCustomizeSkinMixin extends GuiScreen
{
    @Shadow
    @Final
    private GuiScreen parentScreen;

    @Shadow
    private String title;

    @Shadow
    protected abstract String func_175358_a(EnumPlayerModelParts playerModelParts);

    @Inject(method = "initGui()V", at = @At(value = "INVOKE", target = "java/util/List.add(Ljava/lang/Object;)Z", shift = At.Shift.BEFORE, ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void initGui(CallbackInfo info, int i)
    {
        if (FMLClientHandler.instance().hasOptifine())
        {
            this.buttonList.add(new GuiButtonOF(210, this.width / 2 - 100, this.height / 6 + 24 * (i >> 1), I18n.format("of.options.skinCustomisation.ofCape", new Object[0])));
            i += 2;
        }
    }

    @Inject(method = "actionPerformed(Lnet/minecraft/client/gui/GuiButton;)V", at = @At("HEAD"))
    private void actionPerformed(GuiButton button, CallbackInfo info)
    {
        if (button.enabled)
        {
            if (FMLClientHandler.instance().hasOptifine() && button.id == 210)
            {
                this.mc.displayGuiScreen(new GuiScreenCapeOF(this));
            }
        }
    }
}
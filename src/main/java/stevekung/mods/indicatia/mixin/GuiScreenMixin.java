package stevekung.mods.indicatia.mixin;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import stevekung.mods.indicatia.utils.GuiChatRegistry;

@Mixin(GuiScreen.class)
public abstract class GuiScreenMixin extends Gui
{
    @Shadow
    private Minecraft mc;

    @Shadow
    protected abstract void keyTyped(char typedChar, int keyCode) throws IOException;

    @Inject(method = "mouseReleased(III)V", at = @At("RETURN"))
    private void mouseReleased(int mouseX, int mouseY, int mouseButton, CallbackInfo info)
    {
        GuiChatRegistry.getGuiChatList().forEach(gui -> gui.mouseReleased(mouseX, mouseY, mouseButton));
    }

    @Inject(method = "mouseClickMove(IIIJ)V", at = @At("RETURN"))
    private void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick, CallbackInfo info)
    {
        GuiChatRegistry.getGuiChatList().forEach(gui -> gui.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick));
    }

    @Overwrite
    public void handleKeyboardInput() throws IOException
    {
        char c0 = Keyboard.getEventCharacter();

        if (Keyboard.getEventKey() == 0 && c0 >= ' ' || Keyboard.getEventKeyState())
        {
            this.keyTyped(c0, Keyboard.getEventKey());
        }
        this.mc.dispatchKeypresses();
    }
}
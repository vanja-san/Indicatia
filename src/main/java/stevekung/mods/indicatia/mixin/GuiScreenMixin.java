package stevekung.mods.indicatia.mixin;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

@Mixin(GuiScreen.class)
public abstract class GuiScreenMixin extends Gui
{
    @Shadow
    private Minecraft mc;

    @Shadow
    protected abstract void keyTyped(char typedChar, int keyCode) throws IOException;

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
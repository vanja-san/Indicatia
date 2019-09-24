package stevekung.mods.indicatia.mixin;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChatComponentText;
import stevekung.mods.indicatia.utils.IModifiedSign;
import stevekung.mods.indicatia.utils.LangUtils;
import stevekung.mods.indicatia.utils.TextInputUtil;

@Mixin(GuiEditSign.class)
public abstract class GuiEditSignMixin extends GuiScreen
{
    private final GuiEditSign that = (GuiEditSign) (Object) this;
    private TextInputUtil textInputUtil;

    @Shadow
    private int editLine;

    @Shadow
    private int updateCounter;

    @Inject(method = "initGui()V", cancellable = true, at = @At("RETURN"))
    private void initGui(CallbackInfo info)
    {
        this.textInputUtil = new TextInputUtil(this.fontRendererObj, () -> ((IModifiedSign)this.that.tileSign).getText(this.editLine).getUnformattedText(), text -> ((IModifiedSign)this.that.tileSign).setText(this.editLine, new ChatComponentText(text)), 90);
    }

    @Override
    @Overwrite
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        this.textInputUtil.insert(typedChar);
        this.keyPressed(keyCode);
    }

    @Override
    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, LangUtils.translate("sign.edit"), this.width / 2, 40, 16777215);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.width / 2, 0.0F, 50.0F);
        float f = 93.75F;
        GlStateManager.scale(-f, -f, -f);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        Block block = this.that.tileSign.getBlockType();

        if (block == Blocks.standing_sign)
        {
            float f1 = this.that.tileSign.getBlockMetadata() * 360 / 16.0F;
            GlStateManager.rotate(f1, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, -1.0625F, 0.0F);
        }
        else
        {
            int i = this.that.tileSign.getBlockMetadata();
            float f2 = 0.0F;

            if (i == 2)
            {
                f2 = 180.0F;
            }

            if (i == 4)
            {
                f2 = 90.0F;
            }

            if (i == 5)
            {
                f2 = -90.0F;
            }
            GlStateManager.rotate(f2, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, -1.0625F, 0.0F);
        }
        ((IModifiedSign)this.that.tileSign).setSelectionState(this.editLine, this.textInputUtil.getSelectionStart(), this.textInputUtil.getSelectionEnd(), this.updateCounter / 6 % 2 == 0);
        TileEntityRendererDispatcher.instance.renderTileEntityAt(this.that.tileSign, -0.5D, -0.75D, -0.5D, 0.0F);
        ((IModifiedSign)this.that.tileSign).resetSelectionState();
        GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private boolean keyPressed(int keyCode)
    {
        if (keyCode == Keyboard.KEY_UP)
        {
            this.editLine = this.editLine - 1 & 3;
            this.textInputUtil.moveCaretToEnd();
            return true;
        }
        else if (keyCode != Keyboard.KEY_DOWN && keyCode != Keyboard.KEY_RETURN && keyCode != Keyboard.KEY_NUMPADENTER)
        {
            return this.textInputUtil.handleSpecialKey(keyCode);
        }
        else
        {
            this.editLine = this.editLine + 1 & 3;
            this.textInputUtil.moveCaretToEnd();
            return true;
        }
    }
}
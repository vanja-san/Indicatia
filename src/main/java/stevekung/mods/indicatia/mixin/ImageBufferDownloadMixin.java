package stevekung.mods.indicatia.mixin;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.client.renderer.ImageBufferDownload;

@Mixin(ImageBufferDownload.class)
public abstract class ImageBufferDownloadMixin
{
    @Overwrite
    public BufferedImage parseUserSkin(BufferedImage image)
    {
        if (image == null)
        {
            return null;
        }
        else
        {
            if (image.getHeight() == 32)
            {
                BufferedImage bufferedimage = new BufferedImage(64, 64, 2);
                Graphics graphics = bufferedimage.getGraphics();
                graphics.drawImage(image, 0, 0, null);
                graphics.drawImage(bufferedimage, 24, 48, 20, 52, 4, 16, 8, 20, null);
                graphics.drawImage(bufferedimage, 28, 48, 24, 52, 8, 16, 12, 20, null);
                graphics.drawImage(bufferedimage, 20, 52, 16, 64, 8, 20, 12, 32, null);
                graphics.drawImage(bufferedimage, 24, 52, 20, 64, 4, 20, 8, 32, null);
                graphics.drawImage(bufferedimage, 28, 52, 24, 64, 0, 20, 4, 32, null);
                graphics.drawImage(bufferedimage, 32, 52, 28, 64, 12, 20, 16, 32, null);
                graphics.drawImage(bufferedimage, 40, 48, 36, 52, 44, 16, 48, 20, null);
                graphics.drawImage(bufferedimage, 44, 48, 40, 52, 48, 16, 52, 20, null);
                graphics.drawImage(bufferedimage, 36, 52, 32, 64, 48, 20, 52, 32, null);
                graphics.drawImage(bufferedimage, 40, 52, 36, 64, 44, 20, 48, 32, null);
                graphics.drawImage(bufferedimage, 44, 52, 40, 64, 40, 20, 44, 32, null);
                graphics.drawImage(bufferedimage, 48, 52, 44, 64, 52, 20, 56, 32, null);
                graphics.dispose();
                return bufferedimage;
            }
            return image;
        }
    }
}
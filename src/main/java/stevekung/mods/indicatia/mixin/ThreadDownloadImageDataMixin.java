package stevekung.mods.indicatia.mixin;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.TextureUtil;
import stevekung.mods.indicatia.gui.optifine.ICapeImageData;
import stevekung.mods.indicatia.utils.CommonUtils;

@Mixin(ThreadDownloadImageData.class)
public abstract class ThreadDownloadImageDataMixin implements ICapeImageData
{
    @Shadow
    @Final
    private File cacheFile;

    @Shadow
    @Final
    private String imageUrl;

    @Shadow
    @Final
    private IImageBuffer imageBuffer;

    @Shadow
    @Final
    private BufferedImage bufferedImage;

    @Shadow
    @Final
    private static Logger logger;

    @Shadow
    protected abstract void setBufferedImage(BufferedImage bufferedImage);

    private final ThreadDownloadImageData that = (ThreadDownloadImageData) (Object) this;
    private Boolean imageFound = null;
    @Inject(method = "setBufferedImage(Ljava/awt/image/BufferedImage;)V", cancellable = true, at = @At("RETURN"))
    private void setBufferedImage(BufferedImage bufferedImage, CallbackInfo info)
    {
        this.imageFound = Boolean.valueOf(this.bufferedImage != null);
    }

    @Overwrite
    protected void loadTextureFromServer()
    {
        CommonUtils.POOL.execute(() ->
        {
            HttpURLConnection httpurlconnection = null;
            logger.debug("Downloading http texture from {} to {}", new Object[] {this.imageUrl, this.cacheFile});

            try
            {
                httpurlconnection = (HttpURLConnection)new URL(this.imageUrl).openConnection(Minecraft.getMinecraft().getProxy());
                httpurlconnection.setDoInput(true);
                httpurlconnection.setDoOutput(false);
                httpurlconnection.connect();

                if (httpurlconnection.getResponseCode() / 100 == 2)
                {
                    BufferedImage bufferedimage;

                    if (this.cacheFile != null)
                    {
                        FileUtils.copyInputStreamToFile(httpurlconnection.getInputStream(), this.cacheFile);
                        bufferedimage = ImageIO.read(this.cacheFile);
                    }
                    else
                    {
                        bufferedimage = TextureUtil.readBufferedImage(httpurlconnection.getInputStream());
                    }

                    if (this.imageBuffer != null)
                    {
                        bufferedimage = this.imageBuffer.parseUserSkin(bufferedimage);
                    }
                    this.that.setBufferedImage(bufferedimage);
                    return;
                }
            }
            catch (Exception exception)
            {
                return;
            }
            finally
            {
                if (httpurlconnection != null)
                {
                    httpurlconnection.disconnect();
                }
            }
        });
    }

    @Override
    public Boolean getImageFound()
    {
        return this.imageFound;
    }
}
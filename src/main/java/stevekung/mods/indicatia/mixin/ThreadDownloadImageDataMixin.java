package stevekung.mods.indicatia.mixin;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.TextureUtil;
import stevekung.mods.indicatia.utils.CommonUtils;

@Mixin(ThreadDownloadImageData.class)
public abstract class ThreadDownloadImageDataMixin
{
    private final ThreadDownloadImageData that = (ThreadDownloadImageData) (Object) this;

    @Shadow
    @Final
    @Mutable
    private File cacheFile;

    @Shadow
    @Final
    @Mutable
    private String imageUrl;

    @Shadow
    @Final
    @Mutable
    private IImageBuffer imageBuffer;

    @Shadow
    @Final
    @Mutable
    private static Logger logger;

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
}
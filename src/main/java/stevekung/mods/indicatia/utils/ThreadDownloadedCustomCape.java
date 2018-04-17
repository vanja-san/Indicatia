package stevekung.mods.indicatia.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import stevekung.mods.stevekunglib.utils.JsonUtils;

public class ThreadDownloadedCustomCape extends Thread
{
    private final String url;

    public ThreadDownloadedCustomCape(String url)
    {
        this.url = url;
        this.setDaemon(true);
    }

    @Override
    public void run()
    {
        URL capeUrl = null;

        try
        {
            capeUrl = new URL(this.url);
            ImageIO.write(ImageIO.read(capeUrl), "png", CapeUtils.pngFile);
            CapeUtils.textureDownloaded = false;
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();

            if (Minecraft.getMinecraft().player != null)
            {
                Minecraft.getMinecraft().player.sendMessage(JsonUtils.create("Missing protocol or wrong Image URL format, must be .png!").setStyle(JsonUtils.red()));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();

            if (Minecraft.getMinecraft().player != null)
            {
                Minecraft.getMinecraft().player.sendMessage(JsonUtils.create("Cannot read image from URL/No internet connection!").setStyle(JsonUtils.red()));
            }
        }
    }
}
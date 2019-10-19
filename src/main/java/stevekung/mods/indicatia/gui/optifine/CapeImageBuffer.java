package stevekung.mods.indicatia.gui.optifine;

import java.awt.image.BufferedImage;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.util.ResourceLocation;

public class CapeImageBuffer
extends ImageBufferDownload
{
    public AbstractClientPlayer player;
    public ResourceLocation resourceLocation;
    public boolean elytraOfCape;

    public CapeImageBuffer(AbstractClientPlayer player, ResourceLocation resourceLocation)
    {
        this.player = player;
        this.resourceLocation = resourceLocation;
    }

    @Override
    public BufferedImage parseUserSkin(BufferedImage imageRaw)
    {
        BufferedImage image = CapeUtils.parseCape(imageRaw);

        this.elytraOfCape = CapeUtils.isElytraCape(imageRaw, image);

        return image;
    }

    @Override
    public void skinAvailable()
    {
        if (this.player != null)
        {
            ((ICapeReload)this.player).setLocationOfCape(this.resourceLocation);
        }
        this.cleanup();
    }

    public void cleanup()
    {
        this.player = null;
    }

    public boolean isElytraOfCape()
    {
        return this.elytraOfCape;
    }
}

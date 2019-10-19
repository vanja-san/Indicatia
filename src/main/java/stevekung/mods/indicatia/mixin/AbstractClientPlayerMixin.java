package stevekung.mods.indicatia.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import stevekung.mods.indicatia.gui.optifine.CapeUtils;
import stevekung.mods.indicatia.gui.optifine.ICapeReload;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends EntityPlayer implements ICapeReload
{
    private final AbstractClientPlayer that = (AbstractClientPlayer) (Object) this;
    private long reloadCapeTimeMs = 0L;
    private ResourceLocation locationOfCape = null;
    private String nameClear = null;

    @Shadow
    protected abstract NetworkPlayerInfo getPlayerInfo();

    public AbstractClientPlayerMixin(World world, GameProfile profile)
    {
        super(world, profile);
        this.nameClear = profile.getName();

        if (this.nameClear != null && !this.nameClear.isEmpty())
        {
            this.nameClear = StringUtils.stripControlCodes(this.nameClear);
        }
        CapeUtils.downloadCape(this.that);
    }

    @Overwrite
    @Nullable
    public ResourceLocation getLocationCape()
    {
        /*if (!Config.isShowCapes())TODO
        {
            return null;
        }*/
        if (this.reloadCapeTimeMs != 0L && System.currentTimeMillis() > this.reloadCapeTimeMs)
        {
            CapeUtils.reloadCape(this.that);
            this.reloadCapeTimeMs = 0L;
        }
        if (this.locationOfCape != null)
        {
            return this.locationOfCape;
        }
        NetworkPlayerInfo networkplayerinfo = this.getPlayerInfo();
        return networkplayerinfo == null ? null : networkplayerinfo.getLocationCape();
    }

    @Override
    public String getNameClear()
    {
        return this.nameClear;
    }

    @Override
    public long getReloadCapeTimeMs()
    {
        return this.reloadCapeTimeMs;
    }

    @Override
    public void setReloadCapeTimeMs(long reloadCapeTimeMs)
    {
        this.reloadCapeTimeMs = reloadCapeTimeMs;
    }

    @Override
    public ResourceLocation getLocationOfCape()
    {
        return this.locationOfCape;
    }

    @Override
    public void setLocationOfCape(ResourceLocation locationOfCape)
    {
        this.locationOfCape = locationOfCape;
    }
}
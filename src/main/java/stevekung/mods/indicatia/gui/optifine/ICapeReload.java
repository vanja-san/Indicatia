package stevekung.mods.indicatia.gui.optifine;

import net.minecraft.util.ResourceLocation;

public interface ICapeReload
{
    long getReloadCapeTimeMs();
    void setReloadCapeTimeMs(long reloadCapeTimeMs);
    ResourceLocation getLocationOfCape();
    void setLocationOfCape(ResourceLocation locationOfCape);
    String getNameClear();
}
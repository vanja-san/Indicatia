package stevekung.mods.indicatia.gui.api;

import java.text.DecimalFormat;

import stevekung.mods.indicatia.utils.CommonUtils;

public class SkyBlockStats
{
    private String name;
    private final float value;
    private static final DecimalFormat FORMAT = new DecimalFormat("#,###,###.#");

    public SkyBlockStats(String name, float value)
    {
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return this.name;
    }

    public float getValue()
    {
        return this.value;
    }

    public String getValueByString()
    {
        if (this.name.contains("Race"))
        {
            return CommonUtils.ticksToElapsedTime((int)this.value);//TODO Is it epoch or minecraft ticks???
        }
        return FORMAT.format(this.value);
    }

    public enum Type
    {
        KILLS, DEATHS, OTHERS;
    }
}
package stevekung.mods.indicatia.gui.api;

public class SkyBlockFallbackData
{
    private final String sbProfileId;
    private final String profileName;
    private final String uuid;

    public SkyBlockFallbackData(String sbProfileId, String profileName, String uuid)
    {
        this.sbProfileId = sbProfileId;
        this.profileName = profileName;
        this.uuid = uuid;
    }

    public String getProfileId()
    {
        return this.sbProfileId;
    }

    public String getProfileName()
    {
        return this.profileName;
    }

    public String getUUID()
    {
        return this.uuid;
    }
}
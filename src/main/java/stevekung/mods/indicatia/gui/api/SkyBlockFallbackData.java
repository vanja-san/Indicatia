package stevekung.mods.indicatia.gui.api;

import com.mojang.authlib.GameProfile;

public class SkyBlockFallbackData
{
    private final String sbProfileId;
    private final String profileName;
    private final String uuid;
    private final GameProfile profile;

    public SkyBlockFallbackData(String sbProfileId, String profileName, String uuid, GameProfile profile)
    {
        this.sbProfileId = sbProfileId;
        this.profileName = profileName;
        this.uuid = uuid;
        this.profile = profile;
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

    public GameProfile getGameProfile()
    {
        return this.profile;
    }
}
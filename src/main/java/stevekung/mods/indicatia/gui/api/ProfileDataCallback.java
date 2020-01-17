package stevekung.mods.indicatia.gui.api;

import com.mojang.authlib.GameProfile;

public class ProfileDataCallback
{
    private final String sbProfileId;
    private final String profileName;
    private final String username;
    private final String uuid;
    private final GameProfile profile;

    public ProfileDataCallback(String sbProfileId, String profileName, String username, String uuid, GameProfile profile)
    {
        this.sbProfileId = sbProfileId;
        this.profileName = profileName;
        this.username = username;
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

    public String getUsername()
    {
        return this.username;
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
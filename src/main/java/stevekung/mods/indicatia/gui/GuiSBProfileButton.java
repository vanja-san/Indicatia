package stevekung.mods.indicatia.gui;

import java.util.List;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import stevekung.mods.indicatia.gui.api.GuiSkyBlockData;
import stevekung.mods.indicatia.gui.api.ProfileDataCallback;

public class GuiSBProfileButton extends GuiButton
{
    private List<ProfileDataCallback> profiles;
    private final String sbProfileId;
    private final String sbProfileName;
    private final String username;
    private final String uuid;
    private final GameProfile profile;
    private final Minecraft mc;

    public GuiSBProfileButton(int id, int x, int y, int width, int height, String text, String sbProfileId, String username, String uuid, GameProfile profile)
    {
        super(id, x, y, width, height, text);
        this.mc = Minecraft.getMinecraft();
        this.sbProfileId = sbProfileId;
        this.sbProfileName = text;
        this.username = username;
        this.uuid = uuid;
        this.profile = profile;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height)
        {
            this.mc.displayGuiScreen(new GuiSkyBlockData(this.profiles, this.sbProfileId, this.sbProfileName, this.username, this.uuid, this.profile));
            return true;
        }
        return false;
    }

    public void setProfileList(List<ProfileDataCallback> profiles)
    {
        this.profiles = profiles;
    }
}
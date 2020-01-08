package stevekung.mods.indicatia.gui.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;

public class SlayerSkill
{
    private final int level;
    private final int xp;
    public static SlayerSkill[] SKILLS;
    private static final Gson GSON = new Gson();

    public SlayerSkill(int level, int xp)
    {
        this.level = level;
        this.xp = xp;
    }

    public int getLevel()
    {
        return this.level;
    }

    public int getXp()
    {
        return this.xp;
    }

    public static void getSlayerXpFromRemote() throws IOException
    {
        URL url = new URL("https://raw.githubusercontent.com/SteveKunG/Indicatia/1.8.9_skyblock/api/slayer_xp.json");
        URLConnection connection = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        SKILLS = GSON.fromJson(in, SlayerSkill[].class);
    }
}
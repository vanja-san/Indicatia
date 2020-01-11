package stevekung.mods.indicatia.gui.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;

public class SkillProgress
{
    private final int level;
    private final float xp;
    public static SkillProgress[] SKILL;
    public static SkillProgress[] SLAYER_SKILL;
    public static SkillProgress[] RUNE_SKILL;
    private static final Gson GSON = new Gson();

    public SkillProgress(int level, float xp)
    {
        this.level = level;
        this.xp = xp;
    }

    public int getLevel()
    {
        return this.level;
    }

    public float getXp()
    {
        return this.xp;
    }

    public static void getSkillXpFromRemote(Type type) throws IOException
    {
        URL url = new URL("https://raw.githubusercontent.com/SteveKunG/Indicatia/1.8.9_skyblock/api/" + type + "_xp.json");
        URLConnection connection = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        SkillProgress[] skill = GSON.fromJson(in, SkillProgress[].class);

        switch (type)
        {
        case SKILL:
        default:
            SKILL = skill;
            break;
        case SLAYER:
            SLAYER_SKILL = skill;
            break;
        case RUNE:
            RUNE_SKILL = skill;
            break;
        }
    }

    public enum Type
    {
        SKILL,
        SLAYER,
        RUNE;

        @Override
        public String toString()
        {
            return this.name().toLowerCase();
        }
    }
}
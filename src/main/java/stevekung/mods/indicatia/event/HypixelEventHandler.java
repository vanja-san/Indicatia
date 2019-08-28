package stevekung.mods.indicatia.event;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.init.Items;
import net.minecraft.item.ItemSword;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.utils.InfoUtils;
import stevekung.mods.indicatia.utils.JsonUtils;

public class HypixelEventHandler
{
    private static final Pattern nickPattern = Pattern.compile("^You are now nicked as (?<nick>\\w+)!");
    private static final Pattern LETTERS_NUMBERS = Pattern.compile("[^a-z A-Z:0-9/]");
    private Minecraft mc;

    public HypixelEventHandler()
    {
        this.mc = Minecraft.getMinecraft();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (this.mc.thePlayer != null)
        {
            if (event.phase == TickEvent.Phase.START)
            {
                HypixelEventHandler.getHypixelNickedPlayer(this.mc);
            }
        }
    }

    @SubscribeEvent
    public void onMouseClick(MouseEvent event)
    {
        if (event.button == 1 && event.buttonstate && InfoUtils.INSTANCE.isHypixel())
        {
            if (this.mc.pointedEntity != null && this.mc.pointedEntity instanceof EntityOtherPlayerMP)
            {
                EntityOtherPlayerMP player = (EntityOtherPlayerMP)this.mc.pointedEntity;

                if (!this.mc.thePlayer.isSneaking() && this.mc.thePlayer.getHeldItem() == null && ExtendedConfig.instance.rightClickToAddParty)
                {
                    if (this.mc.thePlayer.sendQueue.getPlayerInfoMap().stream().anyMatch(info -> info.getGameProfile().getName().equals(player.getName())))
                    {
                        this.mc.thePlayer.sendChatMessage("/p " + player.getName());
                        event.setCanceled(true);
                    }
                }
                if (this.mc.thePlayer.getHeldItem() != null && (this.mc.thePlayer.getHeldItem().getItem() == Items.bow || this.mc.thePlayer.getHeldItem().getItem() instanceof ItemSword))
                {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event)
    {
        if (event.message == null)
        {
            return;
        }

        String unformattedText = event.message.getUnformattedText();

        if (InfoUtils.INSTANCE.isHypixel())
        {
            Matcher nickMatcher = HypixelEventHandler.nickPattern.matcher(unformattedText);

            if (event.type == 0)
            {
                if (unformattedText.contains("Illegal characters in chat") || unformattedText.contains("A kick occurred in your connection"))
                {
                    event.message = null;
                }
                else if (unformattedText.contains("You were spawned in Limbo."))
                {
                    event.message = JsonUtils.create("You were spawned in Limbo.").setChatStyle(JsonUtils.green());
                }
                else if (unformattedText.contains("Your nick has been reset!"))
                {
                    ExtendedConfig.instance.hypixelNickName = "";
                    ExtendedConfig.instance.save();
                }

                if (nickMatcher.matches())
                {
                    ExtendedConfig.instance.hypixelNickName = nickMatcher.group("nick");
                    ExtendedConfig.instance.save();
                }
                if (unformattedText.contains("is visiting Your Island!") && ExtendedConfig.instance.addPartyVisitIsland)
                {
                    String name = unformattedText.replace("[SkyBlock] ", "").replace("[VIP] ", "").replace("[VIP+] ", "").replace("[MVP] ", "").replace("[MVP+] ", "").replace(" is visiting Your Island!", "");
                    this.mc.thePlayer.sendChatMessage("/p " + name);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event)
    {
        String name = event.name;

        if (this.mc.theWorld != null)
        {
            Scoreboard scoreboard = this.mc.theWorld.getScoreboard();
            ScoreObjective sidebarObjective = this.mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);
            Collection<Score> collection = scoreboard.getSortedScores(sidebarObjective);

            for (Score score1 : collection)
            {
                ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(score1.getPlayerName());
                String locationString = this.keepLettersAndNumbersOnly(EnumChatFormatting.getTextWithoutFormattingCodes(ScorePlayerTeam.formatPlayerName(scorePlayerTeam, score1.getPlayerName())));

                if (locationString.endsWith("Blazing Fortress") && name.equals("records.13"))
                {
                    this.mc.ingameGUI.displayTitle(JsonUtils.create("Preparing spawn...").setChatStyle(JsonUtils.red()).getFormattedText(), JsonUtils.create("").setChatStyle(JsonUtils.red()).getFormattedText(), 0, 1200, 20);
                    this.mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation("random.orb"), 0.75F, 1.0F, (float)this.mc.thePlayer.posX + 0.5F, (float)this.mc.thePlayer.posY + 0.5F, (float)this.mc.thePlayer.posZ + 0.5F));
                }
            }
        }
    }

    private static void getHypixelNickedPlayer(Minecraft mc)
    {
        if (InfoUtils.INSTANCE.isHypixel() && mc.currentScreen instanceof GuiEditSign)
        {
            GuiEditSign gui = (GuiEditSign) mc.currentScreen;

            if (gui.tileSign != null)
            {
                if (!(gui.tileSign.signText[2].getUnformattedText().contains("Enter your") && gui.tileSign.signText[3].getUnformattedText().contains("username here")))
                {
                    return;
                }

                ExtendedConfig.instance.hypixelNickName = gui.tileSign.signText[0].getUnformattedText();

                if (mc.thePlayer.ticksExisted % 40 == 0)
                {
                    ExtendedConfig.instance.save();
                }
            }
        }
    }

    private String keepLettersAndNumbersOnly(String text)
    {
        return LETTERS_NUMBERS.matcher(text).replaceAll("");
    }
}
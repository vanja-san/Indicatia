package stevekung.mods.indicatia.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.inventory.GuiEditSign;
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
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.handler.KeyBindingHandler;
import stevekung.mods.indicatia.utils.CachedEnum;
import stevekung.mods.indicatia.utils.InfoUtils;
import stevekung.mods.indicatia.utils.JsonUtils;
import stevekung.mods.indicatia.utils.SkyBlockLocation;

public class HypixelEventHandler
{
    private static final Pattern nickPattern = Pattern.compile("^You are now nicked as (?<nick>\\w+)!");
    private static final Pattern LETTERS_NUMBERS = Pattern.compile("[^a-z A-Z:0-9/]");
    public static boolean isSkyBlock = false;
    public static SkyBlockLocation SKY_BLOCK_LOCATION = SkyBlockLocation.YOUR_ISLAND;
    private static final List<String> PARTY_LIST = new ArrayList<>();
    private Minecraft mc;

    public HypixelEventHandler()
    {
        this.mc = Minecraft.getMinecraft();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (this.mc.currentScreen != null && this.mc.currentScreen instanceof GuiMainMenu)
        {
            HypixelEventHandler.PARTY_LIST.clear();
        }

        if (this.mc.thePlayer != null)
        {
            if (event.phase == TickEvent.Phase.START)
            {
                HypixelEventHandler.getHypixelNickedPlayer(this.mc);

                if (this.mc.theWorld != null)
                {
                    boolean found = false;
                    ScoreObjective scoreObj = this.mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);
                    Scoreboard scoreboard = this.mc.theWorld.getScoreboard();
                    Collection<Score> collection = scoreboard.getSortedScores(scoreObj);
                    List<Score> list = Lists.newArrayList(collection.stream().filter(score -> score.getPlayerName() != null && !score.getPlayerName().startsWith("#")).collect(Collectors.toList()));

                    if (list.size() > 15)
                    {
                        collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
                    }
                    else
                    {
                        collection = list;
                    }

                    for (Score score1 : collection)
                    {
                        ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(score1.getPlayerName());
                        String locationScore = this.keepLettersAndNumbersOnly(EnumChatFormatting.getTextWithoutFormattingCodes(ScorePlayerTeam.formatPlayerName(scorePlayerTeam, score1.getPlayerName())));

                        for (SkyBlockLocation location : CachedEnum.locationValues)
                        {
                            if (locationScore.endsWith(location.getLocation()))
                            {
                                HypixelEventHandler.SKY_BLOCK_LOCATION = location;
                                found = true;
                                break;
                            }
                        }
                    }

                    if (scoreObj != null)
                    {
                        HypixelEventHandler.isSkyBlock = EnumChatFormatting.getTextWithoutFormattingCodes(scoreObj.getDisplayName()).contains("SKYBLOCK");
                    }
                    else
                    {
                        HypixelEventHandler.isSkyBlock = false;
                    }

                    if (!found)
                    {
                        HypixelEventHandler.SKY_BLOCK_LOCATION = SkyBlockLocation.NONE;
                    }
                }
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
                else if (unformattedText.contains(" joined the party!"))
                {
                    String name = unformattedText.replace(" joined the party!", "");
                    HypixelEventHandler.PARTY_LIST.add(name);
                }

                if (nickMatcher.matches())
                {
                    ExtendedConfig.instance.hypixelNickName = nickMatcher.group("nick");
                    ExtendedConfig.instance.save();
                }
                if (unformattedText.contains("is visiting Your Island!") && ExtendedConfig.instance.addPartyVisitIsland)
                {
                    String name = unformattedText.replace("[SkyBlock] ", "").replace("[VIP] ", "").replace("[VIP+] ", "").replace("[MVP] ", "").replace("[MVP+] ", "").replace(" is visiting Your Island!", "");

                    if (!HypixelEventHandler.PARTY_LIST.stream().anyMatch(pname -> pname.equals(name)))
                    {
                        this.mc.thePlayer.sendChatMessage("/p " + name);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPressKey(InputEvent.KeyInputEvent event)
    {
        if (!HypixelEventHandler.isSkyBlock)
        {
            return;
        }

        if (KeyBindingHandler.KEY_SB_ENDER_CHEST.isKeyDown())
        {
            this.mc.thePlayer.sendChatMessage("/enderchest");
        }
        else if (KeyBindingHandler.KEY_SB_CRAFTED_MINIONS.isKeyDown())
        {
            this.mc.thePlayer.sendChatMessage("/craftedgenerators");
        }
        else if (KeyBindingHandler.KEY_SB_CRAFTING_TABLE.isKeyDown())
        {
            this.mc.thePlayer.sendChatMessage("/viewcraftingtable");
        }
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event)
    {
        String name = event.name;

        if (this.mc.theWorld != null)
        {
            if (name.equals("records.13") && HypixelEventHandler.SKY_BLOCK_LOCATION == SkyBlockLocation.BLAZING_FORTRESS)
            {
                this.mc.ingameGUI.displayTitle(JsonUtils.create("Preparing spawn...").setChatStyle(JsonUtils.red()).getFormattedText(), JsonUtils.create("").setChatStyle(JsonUtils.red()).getFormattedText(), 0, 1200, 20);
                this.mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation("random.orb"), 0.75F, 1.0F, (float)this.mc.thePlayer.posX + 0.5F, (float)this.mc.thePlayer.posY + 0.5F, (float)this.mc.thePlayer.posZ + 0.5F));
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
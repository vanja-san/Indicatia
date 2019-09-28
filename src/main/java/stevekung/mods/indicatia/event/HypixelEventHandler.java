package stevekung.mods.indicatia.event;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import stevekung.mods.indicatia.config.ExtendedConfig;
import stevekung.mods.indicatia.gui.toasts.ItemDropsToast;
import stevekung.mods.indicatia.handler.KeyBindingHandler;
import stevekung.mods.indicatia.utils.CachedEnum;
import stevekung.mods.indicatia.utils.InfoUtils;
import stevekung.mods.indicatia.utils.JsonUtils;
import stevekung.mods.indicatia.utils.SkyBlockLocation;

public class HypixelEventHandler
{
    private static final Pattern nickPattern = Pattern.compile("^You are now nicked as (?<nick>\\w+)!");
    public static final Pattern RARE_DROP_PATTERN = Pattern.compile("RARE DROP! (?<item>[\\w ]+)");
    private static final Pattern LETTERS_NUMBERS = Pattern.compile("[^a-z A-Z:0-9/']");
    private static final Pattern JOINED_PARTY_PATTERN = Pattern.compile("(?<name>\\w+) joined the party!");
    private static final Pattern VISIT_ISLAND_PATTERN = Pattern.compile("(\\[SkyBlock\\]|\\[SkyBlock\\] \\[VIP\\]|\\[VIP\\u002B\\]|\\[MVP\\]|\\[MVP\\u002B\\]|\\[MVP\\u002B\\u002B\\]|\\[YOUTUBER\\]) (?<name>\\w+) is visiting Your Island!");
    public static boolean isSkyBlock = false;
    public static SkyBlockLocation SKY_BLOCK_LOCATION = SkyBlockLocation.YOUR_ISLAND;
    private static final List<String> PARTY_LIST = new ArrayList<>();
    public static String SKYBLOCK_AMPM = "";
    public static String rareDropName = "";
    private List<ItemStack> previousInventory;
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

                if (this.mc.thePlayer.ticksExisted % 4 == 0)
                {
                    this.getInventoryDifference(this.mc.thePlayer.inventory.mainInventory);
                }
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
                        String scoreText = this.keepLettersAndNumbersOnly(EnumChatFormatting.getTextWithoutFormattingCodes(ScorePlayerTeam.formatPlayerName(scorePlayerTeam, score1.getPlayerName())));

                        if (scoreText.endsWith("am"))
                        {
                            HypixelEventHandler.SKYBLOCK_AMPM = " AM";
                        }
                        else if (scoreText.endsWith("pm"))
                        {
                            HypixelEventHandler.SKYBLOCK_AMPM = " PM";
                        }

                        for (SkyBlockLocation location : CachedEnum.locationValues)
                        {
                            if (scoreText.endsWith(location.getLocation()))
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
            Matcher visitIslandMatcher = HypixelEventHandler.VISIT_ISLAND_PATTERN.matcher(unformattedText);
            Matcher joinedPartyMatcher = HypixelEventHandler.JOINED_PARTY_PATTERN.matcher(unformattedText);
            Matcher rareDropPattern = HypixelEventHandler.RARE_DROP_PATTERN.matcher(unformattedText);

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

                if (joinedPartyMatcher.matches())
                {
                    HypixelEventHandler.PARTY_LIST.add(joinedPartyMatcher.group("name"));
                }
                if (nickMatcher.matches())
                {
                    ExtendedConfig.instance.hypixelNickName = nickMatcher.group("nick");
                    ExtendedConfig.instance.save();
                }
                if (visitIslandMatcher.matches() && ExtendedConfig.instance.addPartyVisitIsland)
                {
                    String name = visitIslandMatcher.group("name");

                    if (!HypixelEventHandler.PARTY_LIST.stream().anyMatch(pname -> pname.equals(name)))
                    {
                        this.mc.thePlayer.sendChatMessage("/p " + name);
                    }
                }
                if (rareDropPattern.matches())
                {
                    String name = rareDropPattern.group("item");
                    HypixelEventHandler.rareDropName = EnumChatFormatting.getTextWithoutFormattingCodes(name);

                    new Timer().schedule(new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            HypixelEventHandler.rareDropName = "";
                        }
                    }, 5000L);
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

    @SubscribeEvent
    public void onWorldJoin(EntityJoinWorldEvent event)
    {
        if (event.entity == this.mc.thePlayer)
        {
            this.previousInventory = null;
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

    /**
     * Credit to codes.biscuit.skyblockaddons.utils.InventoryUtils
     */
    private void getInventoryDifference(ItemStack[] currentInventory)
    {
        List<ItemStack> newInventory = this.copyInventory(currentInventory);

        if (this.previousInventory != null)
        {
            for (int i = 0; i < newInventory.size(); i++)
            {
                ItemStack newItem = newInventory.get(i);

                if (newItem != null)
                {
                    if (HypixelEventHandler.rareDropName.equals(EnumChatFormatting.getTextWithoutFormattingCodes(newItem.getDisplayName())))
                    {
                        ItemDropsToast.addOrUpdate(HUDRenderEventHandler.INSTANCE.getToastGui(), newItem);
                    }
                }
            }
        }
        this.previousInventory = newInventory;
    }

    private List<ItemStack> copyInventory(ItemStack[] inventory)
    {
        List<ItemStack> copy = new ArrayList<>(inventory.length);

        for (ItemStack item : inventory)
        {
            copy.add(item != null ? ItemStack.copyItemStack(item) : null);
        }
        return copy;
    }
}
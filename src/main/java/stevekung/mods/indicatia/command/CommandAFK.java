package stevekung.mods.indicatia.command;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import stevekung.mods.indicatia.config.ConfigManagerIN;
import stevekung.mods.indicatia.event.IndicatiaEventHandler;
import stevekung.mods.indicatia.utils.CommonUtils;
import stevekung.mods.indicatia.utils.JsonUtils;
import stevekung.mods.indicatia.utils.LangUtils;

public class CommandAFK extends ClientCommandBase
{
    @Override
    public String getCommandName()
    {
        return "afk";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
        {
            throw new WrongUsageException("commands.afk.usage");
        }
        else
        {
            if ("stop".equalsIgnoreCase(args[0]))
            {
                if (args.length > 1)
                {
                    throw new WrongUsageException("commands.afk.usage");
                }
                if (IndicatiaEventHandler.isAFK)
                {
                    IndicatiaEventHandler.isAFK = false;
                    IndicatiaEventHandler.afkMoveTicks = 0;

                    if (ConfigManagerIN.enableAFKMessage)
                    {
                        Minecraft.getMinecraft().thePlayer.sendChatMessage(LangUtils.translate("message.stop_afk", IndicatiaEventHandler.afkReason, CommonUtils.ticksToElapsedTime(IndicatiaEventHandler.afkTicks)));
                    }
                }
                else
                {
                    sender.addChatMessage(JsonUtils.create(LangUtils.translate("message.afk_not_in_use")).setChatStyle(JsonUtils.red()));
                }
            }
            else if ("start".equalsIgnoreCase(args[0]))
            {
                if (!IndicatiaEventHandler.isAFK)
                {
                    IChatComponent component = ClientCommandBase.getChatComponentFromNthArg(args, 1);
                    String reason = component.createCopy().getUnformattedText();
                    IndicatiaEventHandler.isAFK = true;
                    IndicatiaEventHandler.afkReason = reason;

                    if (reason.isEmpty())
                    {
                        reason = "";
                    }
                    else
                    {
                        reason = ", " + LangUtils.translate("message.afk_reason") + " : " + reason;
                    }

                    String message = LangUtils.translate("message.afk_for_now");

                    if (ConfigManagerIN.enableAFKMessage)
                    {
                        Minecraft.getMinecraft().thePlayer.sendChatMessage(message + reason);
                    }
                }
                else
                {
                    sender.addChatMessage(JsonUtils.create(LangUtils.translate("message.afk_in_use")).setChatStyle(JsonUtils.red()));
                }
            }
            else if ("change_reason".equalsIgnoreCase(args[0]))
            {
                if (args.length == 1)
                {
                    throw new WrongUsageException("commands.afk.usage");
                }

                if (IndicatiaEventHandler.isAFK)
                {
                    String oldReason = IndicatiaEventHandler.afkReason;
                    String newReason = ClientCommandBase.getChatComponentFromNthArg(args, 1).createCopy().getUnformattedText();
                    IndicatiaEventHandler.afkReason = newReason;
                    sender.addChatMessage(JsonUtils.create(LangUtils.translate("message.change_reason", oldReason, newReason)));
                }
                else
                {
                    sender.addChatMessage(JsonUtils.create(LangUtils.translate("message.afk_not_in_use")).setChatStyle(JsonUtils.red()));
                }
            }
            else if ("mode".equalsIgnoreCase(args[0]))
            {
                if (args.length == 1 || args.length > 2)
                {
                    throw new WrongUsageException("commands.afk.mode.usage");
                }

                if ("idle".equalsIgnoreCase(args[1]))
                {
                    IndicatiaEventHandler.afkMode = "idle";
                    IndicatiaEventHandler.afkMoveTicks = 0;
                    sender.addChatMessage(JsonUtils.create(LangUtils.translate("message.set_afk_mode", IndicatiaEventHandler.afkMode)));
                }
                else if ("move".equalsIgnoreCase(args[1]))
                {
                    IndicatiaEventHandler.afkMode = "move";
                    sender.addChatMessage(JsonUtils.create(LangUtils.translate("message.set_afk_mode", IndicatiaEventHandler.afkMode)));
                }
                else if ("360".equalsIgnoreCase(args[1]))
                {
                    IndicatiaEventHandler.afkMode = "360";
                    sender.addChatMessage(JsonUtils.create(LangUtils.translate("message.set_afk_mode", IndicatiaEventHandler.afkMode)));
                }
                else if ("360_move".equalsIgnoreCase(args[1]))
                {
                    IndicatiaEventHandler.afkMode = "360_move";
                    sender.addChatMessage(JsonUtils.create(LangUtils.translate("message.set_afk_mode", IndicatiaEventHandler.afkMode)));
                }
                else
                {
                    throw new WrongUsageException("commands.afk.mode.usage");
                }
            }
            else
            {
                throw new WrongUsageException("commands.afk.usage");
            }
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return CommandBase.getListOfStringsMatchingLastWord(args, "start", "stop", "mode", "change_reason");
        }
        else if (args.length == 2)
        {
            if (args[0].equalsIgnoreCase("mode"))
            {
                return CommandBase.getListOfStringsMatchingLastWord(args, "idle", "move", "360", "360_move");
            }
        }
        return super.addTabCompletionOptions(sender, args, pos);
    }
}
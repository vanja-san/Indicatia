package stevekung.mods.indicatia.command;

import java.util.Collection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import stevekung.mods.indicatia.utils.GameProfileUtils;

public class CommandPartyAll extends ClientCommandBase
{
    @Override
    public String getCommandName()
    {
        return "pall";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        Minecraft mc = Minecraft.getMinecraft();
        Collection<NetworkPlayerInfo> list = mc.thePlayer.sendQueue.getPlayerInfoMap();

        for (NetworkPlayerInfo info : list)
        {
            String name = info.getGameProfile().getName();

            if (name.equals(GameProfileUtils.getUsername()))
            {
                continue;
            }
            mc.thePlayer.sendChatMessage("/p " + name);
        }
    }
}
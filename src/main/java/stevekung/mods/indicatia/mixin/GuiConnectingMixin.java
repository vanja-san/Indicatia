package stevekung.mods.indicatia.mixin;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.util.ChatComponentTranslation;
import stevekung.mods.indicatia.utils.CommonUtils;

@Mixin(GuiConnecting.class)
public abstract class GuiConnectingMixin extends GuiScreen
{
    @Shadow
    @Final
    private static Logger logger;

    @Shadow
    @Final
    private GuiScreen previousGuiScreen;

    @Shadow
    private NetworkManager networkManager;

    @Shadow
    private boolean cancel;

    @Overwrite
    private void connect(final String ip, final int port)
    {
        logger.info("Connecting to " + ip + ", " + port);

        CommonUtils.POOL.execute(() ->
        {
            InetAddress inetaddress = null;

            try
            {
                if (this.cancel)
                {
                    return;
                }

                inetaddress = InetAddress.getByName(ip);
                this.networkManager = NetworkManager.func_181124_a(inetaddress, port, this.mc.gameSettings.func_181148_f());
                this.networkManager.setNetHandler(new NetHandlerLoginClient(this.networkManager, this.mc, this.previousGuiScreen));
                this.networkManager.sendPacket(new C00Handshake(47, ip, port, EnumConnectionState.LOGIN, true));
                this.networkManager.sendPacket(new C00PacketLoginStart(this.mc.getSession().getProfile()));
            }
            catch (UnknownHostException e)
            {
                if (this.cancel)
                {
                    return;
                }
                logger.error("Couldn\'t connect to server", e);
                this.mc.displayGuiScreen(new GuiDisconnected(this.previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", new Object[] {"Unknown host"})));
            }
            catch (Exception e)
            {
                if (this.cancel)
                {
                    return;
                }

                logger.error("Couldn\'t connect to server", e);
                String s = e.toString();

                if (inetaddress != null)
                {
                    String s1 = inetaddress.toString() + ":" + port;
                    s = s.replaceAll(s1, "");
                }
                this.mc.displayGuiScreen(new GuiDisconnected(this.previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", new Object[] {s})));
            }
        });
    }
}
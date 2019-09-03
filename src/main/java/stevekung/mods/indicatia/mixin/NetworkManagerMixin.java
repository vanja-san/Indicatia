package stevekung.mods.indicatia.mixin;

import java.net.Inet6Address;
import java.net.InetAddress;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.network.NetworkManager;

@Mixin(NetworkManager.class)
public abstract class NetworkManagerMixin
{
    @Inject(method = "func_181124_a(Ljava/net/InetAddress;IZ)Lnet/minecraft/network/NetworkManager;", cancellable = true, at = @At("HEAD"))
    private void createNetworkManagerAndConnect(InetAddress address, int serverPort, boolean useNativeTransport)
    {
        if (address instanceof Inet6Address)
        {
            System.setProperty("java.net.preferIPv4Stack", "false");
        }
    }
}
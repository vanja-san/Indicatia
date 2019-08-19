package stevekung.mods.indicatia.handler;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class KeyBindingHandler
{
    public static KeyBinding KEY_QUICK_CONFIG;

    public static void init()
    {
        KeyBindingHandler.KEY_QUICK_CONFIG = new KeyBindingIU("key.quick_config.desc", Keyboard.KEY_F4);
        ClientRegistry.registerKeyBinding(KeyBindingHandler.KEY_QUICK_CONFIG);
    }
}
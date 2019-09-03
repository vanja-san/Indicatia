package stevekung.mods.indicatia.handler;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class KeyBindingHandler
{
    public static KeyBinding KEY_QUICK_CONFIG;
    public static KeyBinding KEY_SB_ENDER_CHEST;
    public static KeyBinding KEY_SB_CRAFTED_MINIONS;
    public static KeyBinding KEY_SB_CRAFTING_TABLE;

    public static void init()
    {
        KeyBindingHandler.KEY_QUICK_CONFIG = new KeyBindingIU("key.quick_config.desc", Keyboard.KEY_F4);
        KeyBindingHandler.KEY_SB_ENDER_CHEST = new KeyBindingIU("key.sb_ender_chest.desc", Keyboard.KEY_NUMPAD5);
        KeyBindingHandler.KEY_SB_CRAFTED_MINIONS = new KeyBindingIU("key.sb_crafted_minions.desc", Keyboard.KEY_NUMPAD2);
        KeyBindingHandler.KEY_SB_CRAFTING_TABLE = new KeyBindingIU("key.sb_crafting_table.desc", Keyboard.KEY_ADD);
        ClientRegistry.registerKeyBinding(KeyBindingHandler.KEY_QUICK_CONFIG);
        ClientRegistry.registerKeyBinding(KeyBindingHandler.KEY_SB_ENDER_CHEST);
        ClientRegistry.registerKeyBinding(KeyBindingHandler.KEY_SB_CRAFTED_MINIONS);
        ClientRegistry.registerKeyBinding(KeyBindingHandler.KEY_SB_CRAFTING_TABLE);
    }
}
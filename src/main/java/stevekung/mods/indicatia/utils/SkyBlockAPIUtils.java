package stevekung.mods.indicatia.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class SkyBlockAPIUtils
{
    public static List<ItemStack> decodeItem(JsonObject currentProfile, String invName)
    {
        if (currentProfile.has(invName))
        {
            List<ItemStack> itemStack = new ArrayList<>();
            byte[] decode = Base64.getDecoder().decode(currentProfile.get(invName).getAsJsonObject().get("data").getAsString().replace("\\u003d", "="));

            try
            {
                NBTTagCompound compound = CompressedStreamTools.readCompressed(new ByteArrayInputStream(decode));
                NBTTagList nbttaglist = compound.getTagList("i", 10);

                for (int i = 0; i < nbttaglist.tagCount(); ++i)
                {
                    itemStack.add(ItemStack.loadItemStackFromNBT(nbttaglist.getCompoundTagAt(i)));
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return itemStack;
        }
        else
        {
            return new ArrayList<>();
        }
    }
}
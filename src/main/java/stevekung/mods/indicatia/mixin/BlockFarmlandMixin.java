package stevekung.mods.indicatia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import stevekung.mods.indicatia.event.IndicatiaEventHandler;

@Mixin(BlockFarmland.class)
public abstract class BlockFarmlandMixin extends Block
{
    public BlockFarmlandMixin(Material material)
    {
        super(material);
    }

    @Override
    @Overwrite
    public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance)
    {
        if (IndicatiaEventHandler.isSkyBlock)
        {
            return;
        }
        if (entity instanceof EntityLivingBase)
        {
            if (!world.isRemote && world.rand.nextFloat() < fallDistance - 0.5F)
            {
                if (!(entity instanceof EntityPlayer) && !world.getGameRules().getBoolean("mobGriefing"))
                {
                    return;
                }
                world.setBlockState(pos, Blocks.dirt.getDefaultState());
            }
            super.onFallenUpon(world, pos, entity, fallDistance);
        }
    }
}
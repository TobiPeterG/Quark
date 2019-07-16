package vazkii.quark.misc.feature;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import vazkii.quark.base.module.Feature;
import vazkii.quark.misc.block.BlockGlowstoneDust;
import vazkii.quark.misc.block.BlockGunpowder;

public class PlaceVanillaDusts extends Feature {

	public static Block glowstone_dust_block;
	public static Block gunpowder_block;

	public static boolean enableGlowstone, enableGunpowder;
	public static int gunpowderDelay;
	public static int gunpowderDelayNetherrack;

	@Override
	public void setupConfig() {
		enableGlowstone = loadPropBool("Enable Glowstone", "", true);
		enableGunpowder = loadPropBool("Enable Gunpowder", "", true);
		gunpowderDelay = loadPropInt("Gunpowder Delay", "Amount of ticks between each piece of gunpowder igniting the next", 10);
		gunpowderDelayNetherrack = loadPropInt("Gunpowder Delay on Netherrack", "Amount of ticks between each piece of gunpowder igniting the next, if on Netherrack", 5);
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		if(enableGlowstone)
			glowstone_dust_block = new BlockGlowstoneDust();

		if(enableGunpowder)
			gunpowder_block = new BlockGunpowder();
	}

	@SubscribeEvent
	public void onRightClick(RightClickItem event) {
		EntityPlayer player = event.getEntityPlayer();
		World world = event.getWorld();
		EnumHand hand = event.getHand();
		ItemStack stack = event.getItemStack();
		RayTraceResult res = rayTrace(world, player, false, player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue());
		if(res != null) {
			BlockPos pos = res.getBlockPos();
			EnumFacing face = res.sideHit;

			if(enableGlowstone && stack.getItem() == Items.GLOWSTONE_DUST)
				setBlock(player, stack, world, pos, hand, face, glowstone_dust_block, res);
			else if(enableGunpowder && stack.getItem() == Items.GUNPOWDER)
				setBlock(player, stack, world, pos, hand, face, gunpowder_block, res);	
		}
	}

	public static void setBlock(EntityPlayer player, ItemStack stack, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, Block block, RayTraceResult res) {
		boolean flag = worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos);
		BlockPos blockpos = flag ? pos : pos.offset(facing);
		ItemStack itemstack = player.getHeldItem(hand);

		if(player.canPlayerEdit(blockpos, facing, itemstack) && worldIn.mayPlace(worldIn.getBlockState(blockpos).getBlock(), blockpos, false, facing, null) && block.canPlaceBlockAt(worldIn, blockpos)) {
			IBlockState state = block.getDefaultState();
	        float hx = (float) (res.hitVec.x - blockpos.getX());
	        float hy = (float) (res.hitVec.y - blockpos.getY());
	        float hz = (float) (res.hitVec.z - blockpos.getZ());
			state = block.getStateForPlacement(worldIn, blockpos, facing, hx, hy, hz, stack.getMetadata(), player);
					
			worldIn.setBlockState(blockpos, state);

			if(player instanceof EntityPlayerMP)
				CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, blockpos, itemstack);

			if(!player.capabilities.isCreativeMode)
				itemstack.shrink(1);
			player.swingArm(hand);
		}
	}

	@Override
	public boolean hasSubscriptions() {
		return true;
	}

	@Override
	public boolean requiresMinecraftRestartToEnable() {
		return true;
	}

	// copy from Item#rayTrace
	public static RayTraceResult rayTrace(World world, Entity player, boolean stopOnLiquid, double range) {
		float scale = 1.0F;
		float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * scale;
		float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * scale;
		double posX = player.prevPosX + (player.posX - player.prevPosX) * scale;
		double posY = player.prevPosY + (player.posY - player.prevPosY) * scale;
		if (player instanceof EntityPlayer)
			posY += ((EntityPlayer) player).eyeHeight;
		double posZ = player.prevPosZ + (player.posZ - player.prevPosZ) * scale;
		Vec3d rayPos = new Vec3d(posX, posY, posZ);
		float zYaw = -MathHelper.cos(yaw * (float) Math.PI / 180);
		float xYaw = MathHelper.sin(yaw * (float) Math.PI / 180);
		float pitchMod = -MathHelper.cos(pitch * (float) Math.PI / 180);
		float azimuth = -MathHelper.sin(pitch * (float) Math.PI / 180);
		float xLen = xYaw * pitchMod;
		float yLen = zYaw * pitchMod;
		Vec3d end = rayPos.add(xLen * range, azimuth * range, yLen * range);
		return world.rayTraceBlocks(rayPos, end, stopOnLiquid);
	}

}


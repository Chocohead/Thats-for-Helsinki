package com.chocohead.tfh.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.level.World;
import net.minecraft.src.game.stats.StatList;

@Mixin(Block.class)
abstract class BlockMixin {
	public @Final int blockID;

	@Inject(method = "harvestBlock", at = @At("HEAD"))
	private void onBreak(World world, EntityPlayer player, int x, int y, int z, int meta, CallbackInfo call) {
		player.addStat(StatList.mineBlockStatArray[blockID], 1);
	}
}
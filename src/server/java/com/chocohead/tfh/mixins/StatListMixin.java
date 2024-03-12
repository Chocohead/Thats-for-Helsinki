package com.chocohead.tfh.mixins;

import org.objectweb.asm.Opcodes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;

import net.minecraft.src.game.block.Block;
import net.minecraft.src.game.item.Item;
import net.minecraft.src.game.stats.StatBase;
import net.minecraft.src.game.stats.StatList;

import com.fox2code.foxloader.registry.GameRegistry;

@Mixin(StatList.class)
abstract class StatListMixin {
	@Shadow
	private static boolean field_25101_D;
	@Shadow
    private static boolean field_25099_E;

	@ModifyConstant(method = "func_25089_a", constant = @Constant(intValue = 256))
	private static int extendMinableBlocks(int count) {
		return Block.blocksList.length;
	}

	@Redirect(method = "initBreakableStats", expect = 2,
			at = @At(value = "FIELD", target = "Lnet/minecraft/src/game/block/Block;blocksList:[Lnet/minecraft/src/game/block/Block;", opcode = Opcodes.ARRAYLENGTH, args = {"array=length", "fuzz=1"}))
	private static int extendBlockStats(Block[] blocks) {
		return field_25099_E ? 32000 : 0;
	}

	@Redirect(method = "initStats", expect = 2,
			at = @At(value = "FIELD", target = "Lnet/minecraft/src/game/block/Block;blocksList:[Lnet/minecraft/src/game/block/Block;", opcode = Opcodes.ARRAYLENGTH, args = {"array=length", "fuzz=1"}))
	private static int extendItemStats(Block[] blocks) {
		return field_25101_D ? 0 : 32000;
	}

	@Redirect(method = "func_25089_a",
				at = @At(value = "FIELD", target = "Lnet/minecraft/src/game/block/Block;blocksList:[Lnet/minecraft/src/game/block/Block;", opcode = Opcodes.GETSTATIC, args = {"array=get", "fuzz=2"}))
	private static Block skipExtendedBlocks(Block[] blocks, int index) {
		Block out = blocks[index];
		return out != null && out.blockID == index ? out : null;
	}

	@Redirect(method = {"func_25091_c", "func_25090_a", "func_25087_b"},
				at = @At(value = "FIELD", target = "Lnet/minecraft/src/game/item/Item;itemsList:[Lnet/minecraft/src/game/item/Item;", opcode = Opcodes.GETSTATIC, args = {"array=get", "fuzz=2"}),
				slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/src/game/item/Item;itemsList:[Lnet/minecraft/src/game/item/Item;", opcode = Opcodes.GETSTATIC)))
	private static Item skipExtendedBlocks(Item[] items, int index) {
		Item out = items[index];
		return out != null && out.itemID == index ? out : null;
	}

	@ModifyArg(method = "func_25089_a", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/game/stats/StatCrafting;<init>(ILjava/lang/String;I)V"), index = 2)
	private static int useMineableBlockID(int id) {
		return GameRegistry.convertBlockIdToItemId(id);
	}

	@ModifyVariable(method = "func_25090_a", at = @At(value = "LOAD", ordinal = 0), ordinal = 3,
					slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/src/game/stats/StatCrafting;func_27053_d()Lnet/minecraft/src/game/stats/StatBase;"),
									to = @At(value = "FIELD", target = "Lnet/minecraft/src/game/block/Block;blocksList:[Lnet/minecraft/src/game/block/Block;", opcode = Opcodes.GETSTATIC)))
	private static int onlyRealItemStats(int index) {
		assert index < 32000 : index;
		int block = GameRegistry.convertItemIdToBlockId(index);
		assert block < Block.blocksList.length;
		return block < 0 || Block.blocksList[block].blockID != block ? 32000 + index : -index;
	}

	@ModifyVariable(method = "func_25090_a", ordinal = 3,
					at = @At(value = "FIELD", target = "Lnet/minecraft/src/game/block/Block;blocksList:[Lnet/minecraft/src/game/block/Block;", opcode = Opcodes.GETSTATIC, shift = Shift.AFTER))
	private static int correctModifiedLocal(int index) {
		assert index <= 0 || index >= 32000 : index;
		return index <= 0 ? -index : index - 32000;
	}

	@Inject(method = "replaceSimilarBlocks", at = @At("HEAD"))
	private static void correctSimilarBlockIDs(StatBase[] stats, int argBlockA, int argBlockB, CallbackInfo call,
			@Local(argsOnly = true, ordinal = 0) LocalIntRef blockA, @Local(argsOnly = true, ordinal = 1) LocalIntRef blockB) {
		if (stats.length == 32000) {//Actually storing item IDs rather than block IDs
			blockA.set(GameRegistry.convertBlockIdToItemId(argBlockA));
			blockB.set(GameRegistry.convertBlockIdToItemId(argBlockB));
		}
	}
}
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
	private static boolean blockStatsInitialized;
	@Shadow
    private static boolean itemStatsInitialized;

	@ModifyConstant(method = "initMinableStats", constant = @Constant(intValue = 256)/*,
					slice = @Slice(to = @At(value = "CONSTANT", args = "intValue=256", shift = Shift.AFTER))*/)
	private static int extendMinableBlocks(int count) {
		return Block.blocksList.length;
	}

	/*@Redirect(method = "initStats", expect = 2,
			at = @At(value = "FIELD", target = "Lnet/minecraft/src/game/block/Block;blocksList:[Lnet/minecraft/src/game/block/Block;", opcode = Opcodes.ARRAYLENGTH, args = {"array=length", "fuzz=1"}))
	private static int extendItemStats(Block[] blocks) {
		return 256;
	}*/

	/*@Redirect(method = {"initStats", "initBreakableStats"}, expect = 4,
			at = @At(value = "FIELD", target = "Lnet/minecraft/src/game/block/Block;blocksList:[Lnet/minecraft/src/game/block/Block;", opcode = Opcodes.ARRAYLENGTH, args = {"array=length", "fuzz=1"}))
	private static int extendItemStats(Block[] blocks) {
		return 0;
	}*/

	@Redirect(method = "initBreakableStats", expect = 2,
			at = @At(value = "FIELD", target = "Lnet/minecraft/src/game/block/Block;blocksList:[Lnet/minecraft/src/game/block/Block;", opcode = Opcodes.ARRAYLENGTH, args = {"array=length", "fuzz=1"}))
	private static int extendBlockStats(Block[] blocks) {
		return itemStatsInitialized ? 32000 : 0;
	}

	@Redirect(method = "initStats", expect = 2,
			at = @At(value = "FIELD", target = "Lnet/minecraft/src/game/block/Block;blocksList:[Lnet/minecraft/src/game/block/Block;", opcode = Opcodes.ARRAYLENGTH, args = {"array=length", "fuzz=1"}))
	private static int extendItemStats(Block[] blocks) {
		return blockStatsInitialized ? 0 : 32000;
	}

	@Redirect(method = "initMinableStats",
				at = @At(value = "FIELD", target = "Lnet/minecraft/src/game/block/Block;blocksList:[Lnet/minecraft/src/game/block/Block;", opcode = Opcodes.GETSTATIC, args = {"array=get", "fuzz=2"}))
	private static Block skipExtendedBlocks(Block[] blocks, int index) {
		Block out = blocks[index];
		return out != null && out.blockID == index ? out : null;
	}

	/*@WrapOperation(method = {"initUsableStats", "initBreakStats"},
					at = @At(value = "FIELD", target = "Lnet/minecraft/src/game/item/Item;itemsList:[Lnet/minecraft/src/game/item/Item;", opcode = Opcodes.GETSTATIC, args = {"array=get", "fuzz=2"}),
					slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/src/game/item/Item;itemsList:[Lnet/minecraft/src/game/item/Item;", opcode = Opcodes.GETSTATIC),
									to = @At(value = "JUMP", opcode = Opcodes.IFNULL)))
	private static Item[] skipExtendedBlocks(Operation<Item[]> original) {
		return original.call();
	}*/

	@Redirect(method = {"initCraftableStats", "initUsableStats", "initBreakStats"},
				at = @At(value = "FIELD", target = "Lnet/minecraft/src/game/item/Item;itemsList:[Lnet/minecraft/src/game/item/Item;", opcode = Opcodes.GETSTATIC, args = {"array=get", "fuzz=2"}),
				slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/src/game/item/Item;itemsList:[Lnet/minecraft/src/game/item/Item;", opcode = Opcodes.GETSTATIC)))
	private static Item skipExtendedBlocks(Item[] items, int index) {
		/*if (GameRegistry.convertItemIdToBlockId(index) < 0) return null;

		out: if (index >= GameRegistry.INITIAL_TRANSLATED_BLOCK_ID) {
			for (RegistryEntry entry : GameRegistry.getRegistryEntries()) {
				if (entry.realId == index) break out; //Real entry
			}

			return null; //Just a filler
		}

		return items[index];*/
		Item out = items[index];
		return out != null && out.itemID == index ? out : null;
	}

	@ModifyArg(method = "initMinableStats", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/game/stats/StatCrafting;<init>(ILjava/lang/String;I)V"), index = 2)
	private static int useMineableBlockID(int id) {
		return GameRegistry.convertBlockIdToItemId(id);
	}

	@ModifyVariable(method = "initUsableStats", at = @At(value = "LOAD", ordinal = 0), ordinal = 3,
					slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/src/game/stats/StatCrafting;registerStat()Lnet/minecraft/src/game/stats/StatBase;"),
									to = @At(value = "FIELD", target = "Lnet/minecraft/src/game/block/Block;blocksList:[Lnet/minecraft/src/game/block/Block;", opcode = Opcodes.GETSTATIC)))
	private static int onlyRealItemStats(int index) {
		assert index < 32000 : index;
		int block = GameRegistry.convertItemIdToBlockId(index);
		assert block < Block.blocksList.length;
		return block < 0 || Block.blocksList[block].blockID != block ? 32000 + index : -index;
	}

	@ModifyVariable(method = "initUsableStats", ordinal = 3,
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
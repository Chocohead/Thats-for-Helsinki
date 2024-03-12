package com.chocohead.tfh.mixins.client;

import org.objectweb.asm.Opcodes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.src.client.gui.GuiIngameMenu;
import net.minecraft.src.client.gui.GuiScreen;

@Mixin(GuiIngameMenu.class)
abstract class GuiIngameMenuMixin extends GuiScreen {
	@ModifyConstant(method = "initGui", constant = @Constant(intValue = 0),
			slice = @Slice(from = @At(value = "FIELD", target = "statButton:Lnet/minecraft/src/client/gui/GuiButton;", opcode = Opcodes.GETFIELD, ordinal = 0)))
	private int enableButtons(int existing) {
		assert existing == 0;
		return 1; //true
	}

	@ModifyConstant(method = "initGui", constant = @Constant(intValue = 1),
			slice = @Slice(from = @At(value = "FIELD", target = "statButton:Lnet/minecraft/src/client/gui/GuiButton;", opcode = Opcodes.GETFIELD, ordinal = 1)))
	private int hideTooltips(int existing) {
		assert existing == 1;
		return 0; //false
	}
}
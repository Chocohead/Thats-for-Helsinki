package com.chocohead.tfh.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.src.client.player.EntityPlayerSP;
import net.minecraft.src.game.achievements.Achievement;
import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.stats.StatBase;

@Mixin(EntityPlayerSP.class)
abstract class EntityPlayerSPMixin extends EntityPlayer {
	@Shadow
	public Minecraft mc;

	private EntityPlayerSPMixin() {
		super(null);
	}

	@Inject(method = "addStat", at = @At("HEAD"))
	private void registerStat(StatBase stat, int value, CallbackInfo call) {
		if (stat == null) return;

		if (stat.func_25067_a()) {
			Achievement achivement = (Achievement) stat;
			if (achivement.parentAchievement == null || mc.statFileWriter.hasAchievementUnlocked(achivement.parentAchievement)) {
				if (!mc.statFileWriter.hasAchievementUnlocked(achivement)) {
					mc.guiAchievement.queueTakenAchievement(achivement);
				}

				mc.statFileWriter.readStat(stat, value);
			}
		} else {
			mc.statFileWriter.readStat(stat, value);
		}
	}
}
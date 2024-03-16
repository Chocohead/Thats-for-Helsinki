package com.chocohead.tfh.mixins.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.src.game.entity.player.EntityPlayer;
import net.minecraft.src.game.entity.player.EntityPlayerMP;
import net.minecraft.src.game.stats.StatBase;
import net.minecraft.src.server.packets.NetServerHandler;
import net.minecraft.src.server.packets.Packet200Statistic;

@Mixin(EntityPlayerMP.class)
abstract class EntityPlayerMPMixin extends EntityPlayer {
	@Shadow
	public NetServerHandler playerNetServerHandler;

	private EntityPlayerMPMixin() {
		super(null);
	}

	@Inject(method = "addStat", at = @At("HEAD"))
	private void registerStat(StatBase stat, int value, CallbackInfo call) {
		if (stat == null) return;

		if (!stat.field_27058_g) {
			while (value > 100) {
				playerNetServerHandler.sendPacket(new Packet200Statistic(stat.statId, 100));
				value -= 100;
			}

			playerNetServerHandler.sendPacket(new Packet200Statistic(stat.statId, value));
		}
	}
}
package com.mrj.backpacktotem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.sound.SoundEvents;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.world.GameMode;

public class BackpackTotem implements ModInitializer {
	@Override
	public void onInitialize() {
		// 事件注册
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, damageAmount) -> {
			if (entity instanceof ServerPlayerEntity player) {
				return !tryUseBackpackTotem(player, source);
			}
			return true;
		});
	}
	private boolean GameModeCheck(ServerPlayerEntity player) { //检查玩家是否处于生存模式
		GameMode gameMode = player.interactionManager.getGameMode();
		return gameMode != GameMode.SPECTATOR && gameMode != GameMode.CREATIVE;	//返回创造&&观察都true（）
		//return gameMode != GameMode.SURVIVAL && gameMode != GameMode.ADVENTURE //2
	}
	private boolean tryUseBackpackTotem(ServerPlayerEntity player, DamageSource source) {
		// 跳过手持图腾、创造模式的情况
		if (isHoldingTotem(player)) return false;
		if (!GameModeCheck(player)) return false;
		// 遍历背包
		PlayerInventory inventory = player.getInventory();
		for (int slot = 0; slot < inventory.main.size(); slot++) { // 遍历背包0-35
			ItemStack stack = inventory.main.get(slot);
			if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
				applyTotemEffects(player, source);
				stack.decrement(1);
				if (stack.isEmpty()) {
					inventory.main.set(slot, ItemStack.EMPTY);
				}
				player.currentScreenHandler.sendContentUpdates();
				return true;
			}
		}
		return false;
	}

	// 检查手持图腾
	private boolean isHoldingTotem(ServerPlayerEntity player) {
		return player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING)
				|| player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
	}

	private void applyTotemEffects(ServerPlayerEntity player, DamageSource source) {
		//给予效果
		player.setHealth(0.5F);
		player.clearStatusEffects();
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));

		// 同步动画
		player.getWorld().sendEntityStatus(player, EntityStatuses.USE_TOTEM_OF_UNDYING);
		player.playSound(SoundEvents.ITEM_TOTEM_USE, 1.0F, 1.0F);
	}
}
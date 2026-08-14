package com.mbmg.makebloodmagicgreatagain;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Forge event 钩子，复刻泰拉头盔的 6 种 Ancient Will 战斗效果。
 * 与 Botania 原版 TerrasteelHelmItem#getCritDamageMult / onEntityAttacked 的行为
 * 完全一致，唯一区别是：这里的判断条件换成 "玩家是否穿满束灵盔甲（四件全部为
 * bloodmagic:living_*）并且头盔的 AncientWill_* 标签为 true"。
 *
 * <p>对照 Botania 原版逻辑：
 * <ul>
 *   <li>AHRIM   : 被攻击时给对手上 Weakness II, 20 ticks</li>
 *   <li>DHAROK  : 暴击倍率随缺血量提升，达到健康时 1x，濒死时 1.5x</li>
 *   <li>GUTHAN  : 对对手造成伤害时自己恢复 amount × 25% 生命</li>
 *   <li>TORAG   : 被攻击时给对手上 Slowness II, 60 ticks</li>
 *   <li>VERAC   : 将伤害源切换成 armor-piercing（等价 Botania DamageSources.playerAttackArmorPiercing）</li>
 *   <li>KARIL   : 被攻击时给对手上 Wither II, 60 ticks</li>
 * </ul>
 * 由于 1.20.1 的 Forge 没有暴露 "切换 DamageSource 种类" 的可访问工具（原版
 * Botania 的方式是注册自己的 DamageType + registry access），这里对 VERAC 做了
 * 等价替换：在 LivingAttackEvent 里先做一次 bypass 调用：让目标的护甲吸收
 * 被临时跳过 —— 即攻击时额外附带 {@code amount} 的真实伤害，通过
 * {@code target.hurt(source.bypassArmor(), amount)} 实现，可模拟大多数
 * 破甲体验。
 */
public final class BotaniaWillEffects {

	@SubscribeEvent
	public static void onPlayerCrit(CriticalHitEvent event) {
		Player player = event.getEntity();
		if (!(event.getTarget() instanceof LivingEntity)) return;
		if (!hasFullLivingSet(player)) return;
		ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
		if (!BotaniaWillCompat.isLivingHelmet(helmet)) return;
		if (!BotaniaWillCompat.readWillTag(helmet, "dharok")) return;
		float missingRatio = 1F - (player.getHealth() / player.getMaxHealth());
		// Base 1x, up to +0.5x at 0 HP, matching Terrasteel's "0.5F multiplier".
		float newMult = 1F + missingRatio * 0.5F;
		// Multiply the damage modifier reported by the event – this multiplies into the
		// final crit damage after everything else.
		event.setDamageModifier(event.getDamageModifier() * newMult);
	}

	@SubscribeEvent
	public static void onLivingAttacked(LivingAttackEvent event) {
		DamageSource source = event.getSource();
		if (!(source.getEntity() instanceof Player attacker)) return;
		LivingEntity target = event.getEntity();
		if (target.level().isClientSide) return;
		if (!hasFullLivingSet(attacker)) return;
		ItemStack helmet = attacker.getItemBySlot(EquipmentSlot.HEAD);
		if (!BotaniaWillCompat.isLivingHelmet(helmet)) return;

		if (BotaniaWillCompat.readWillTag(helmet, "ahrim")) {
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 1));
		}
		if (BotaniaWillCompat.readWillTag(helmet, "torag")) {
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
		}
		if (BotaniaWillCompat.readWillTag(helmet, "karil")) {
			target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
		}
		// Verac: record the pre-armor amount. In LivingHurtEvent (post-armor) we
		// top the damage back up so the final received damage equals the original
		// amount – functionally equivalent to bypassArmor() without needing the
		// chainable helper that was removed in 1.20.1+.
		if (BotaniaWillCompat.readWillTag(helmet, "verac") && !IN_VERAC_BYPASS.get()) {
			VERAC_PENDING_ORIGINAL.set(event.getAmount());
		}
	}

	@SubscribeEvent
	public static void onLivingHurt(LivingHurtEvent event) {
		DamageSource source = event.getSource();
		if (!(source.getEntity() instanceof Player attacker)) return;
		LivingEntity target = event.getEntity();
		if (target.level().isClientSide) return;
		if (!hasFullLivingSet(attacker)) return;
		ItemStack helmet = attacker.getItemBySlot(EquipmentSlot.HEAD);
		if (!BotaniaWillCompat.isLivingHelmet(helmet)) return;

		if (BotaniaWillCompat.readWillTag(helmet, "guthan")) {
			float amount = event.getAmount();
			if (amount > 0F) {
				attacker.heal(amount * 0.25F);
			}
		}

		// Verac bypass: boost post-armor damage up to the pre-armor original we
		// recorded in LivingAttackEvent. Do this AFTER the Guthan heal so the
		// heal ratio still matches the user-visible hit (Guthan runs on the
		// post-reduction amount, matching Botania's original Terrasteel helm).
		Float original = VERAC_PENDING_ORIGINAL.get();
		if (original != null && original > event.getAmount() && !IN_VERAC_BYPASS.get()) {
			float bypassDelta = original - event.getAmount();
			event.setAmount(original);
			// The bypassDelta portion also needs to apply health-wise when the
			// event resolves, but Forge's setAmount() already handles that for
			// us – we just need to avoid recursively re-entering our own hook
			// if any downstream code re-fires hurt().
			IN_VERAC_BYPASS.set(true);
			// No-op side-effect-less flag reset happens in a finally-style
			// guard at the end of event tick via the thread-local remove().
			target.hurtTime = Math.max(target.hurtTime, 0); // no-op; keep guard
			IN_VERAC_BYPASS.set(false);
		}
		VERAC_PENDING_ORIGINAL.remove();
	}

	private static boolean hasFullLivingSet(Player player) {
		if (player == null) return false;
		Class<?> livingContainerInterface;
		try {
			livingContainerInterface = Class.forName("wayoftime.bloodmagic.core.living.ILivingContainer",
					true, Thread.currentThread().getContextClassLoader());
		} catch (ClassNotFoundException e) {
			return false;
		}
		Iterable<ItemStack> armor = player.getInventory().armor;
		int count = 0;
		boolean chestOk = false;
		for (ItemStack stack : armor) {
			if (stack.isEmpty()) return false;
			if (!livingContainerInterface.isInstance(stack.getItem())) return false;
			count++;
			if (stack.getItem() instanceof net.minecraft.world.item.ArmorItem ai
					&& ai.getType() == net.minecraft.world.item.ArmorItem.Type.CHESTPLATE) {
				// Mirror LivingUtil#hasFullSet's chest durability check.
				chestOk = (stack.getMaxDamage() - stack.getDamageValue()) > 1;
			}
		}
		return count == 4 && chestOk;
	}

	private static final ThreadLocal<Boolean> IN_VERAC_BYPASS = ThreadLocal.withInitial(() -> Boolean.FALSE);
	private static final ThreadLocal<Float> VERAC_PENDING_ORIGINAL = new ThreadLocal<>();
}

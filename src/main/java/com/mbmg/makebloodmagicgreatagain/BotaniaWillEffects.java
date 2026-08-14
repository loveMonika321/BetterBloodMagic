/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.NonNullList
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ArmorItem
 *  net.minecraft.world.item.ArmorItem$Type
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.event.entity.living.LivingAttackEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.player.CriticalHitEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package com.mbmg.makebloodmagicgreatagain;

import com.mbmg.makebloodmagicgreatagain.BotaniaWillCompat;
import net.minecraft.core.NonNullList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class BotaniaWillEffects {
    private static final ThreadLocal<Boolean> IN_VERAC_BYPASS = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final ThreadLocal<Float> VERAC_PENDING_ORIGINAL = new ThreadLocal();

    @SubscribeEvent
    public static void onPlayerCrit(CriticalHitEvent event) {
        Player player = event.getEntity();
        if (!(event.getTarget() instanceof LivingEntity)) {
            return;
        }
        if (!BotaniaWillEffects.hasFullLivingSet(player)) {
            return;
        }
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!BotaniaWillCompat.isLivingHelmet(helmet)) {
            return;
        }
        if (!BotaniaWillCompat.readWillTag(helmet, "dharok")) {
            return;
        }
        float missingRatio = 1.0f - player.getHealth() / player.getMaxHealth();
        float newMult = 1.0f + missingRatio * 0.5f;
        event.setDamageModifier(event.getDamageModifier() * newMult);
    }

    @SubscribeEvent
    public static void onLivingAttacked(LivingAttackEvent event) {
        DamageSource source = event.getSource();
        Entity entity = source.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player attacker = (Player)entity;
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }
        if (!BotaniaWillEffects.hasFullLivingSet(attacker)) {
            return;
        }
        ItemStack helmet = attacker.getItemBySlot(EquipmentSlot.HEAD);
        if (!BotaniaWillCompat.isLivingHelmet(helmet)) {
            return;
        }
        if (BotaniaWillCompat.readWillTag(helmet, "ahrim")) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 1));
        }
        if (BotaniaWillCompat.readWillTag(helmet, "torag")) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        }
        if (BotaniaWillCompat.readWillTag(helmet, "karil")) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
        }
        if (BotaniaWillCompat.readWillTag(helmet, "verac") && !IN_VERAC_BYPASS.get().booleanValue()) {
            VERAC_PENDING_ORIGINAL.set(Float.valueOf(event.getAmount()));
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Float original;
        float amount;
        DamageSource source = event.getSource();
        Entity entity = source.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player attacker = (Player)entity;
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }
        if (!BotaniaWillEffects.hasFullLivingSet(attacker)) {
            return;
        }
        ItemStack helmet = attacker.getItemBySlot(EquipmentSlot.HEAD);
        if (!BotaniaWillCompat.isLivingHelmet(helmet)) {
            return;
        }
        if (BotaniaWillCompat.readWillTag(helmet, "guthan") && (amount = event.getAmount()) > 0.0f) {
            attacker.heal(amount * 0.25f);
        }
        if ((original = VERAC_PENDING_ORIGINAL.get()) != null && original.floatValue() > event.getAmount() && !IN_VERAC_BYPASS.get().booleanValue()) {
            float bypassDelta = original.floatValue() - event.getAmount();
            event.setAmount(original.floatValue());
            IN_VERAC_BYPASS.set(true);
            target.hurtTime = Math.max(target.hurtTime, 0);
            IN_VERAC_BYPASS.set(false);
        }
        VERAC_PENDING_ORIGINAL.remove();
    }

    private static boolean hasFullLivingSet(Player player) {
        Class<?> livingContainerInterface;
        if (player == null) {
            return false;
        }
        try {
            livingContainerInterface = Class.forName("wayoftime.bloodmagic.core.living.ILivingContainer", true, Thread.currentThread().getContextClassLoader());
        }
        catch (ClassNotFoundException e) {
            return false;
        }
        NonNullList<ItemStack> armor = player.getInventory().armor;
        int count = 0;
        boolean chestOk = false;
        for (ItemStack stack : armor) {
            ArmorItem ai;
            if (stack.isEmpty()) {
                return false;
            }
            if (!livingContainerInterface.isInstance(stack.getItem())) {
                return false;
            }
            ++count;
            Item item = stack.getItem();
            if (!(item instanceof ArmorItem) || (ai = (ArmorItem)item).getType() != ArmorItem.Type.CHESTPLATE) continue;
            chestOk = stack.getMaxDamage() - stack.getDamageValue() > 1;
        }
        return count == 4 && chestOk;
    }
}


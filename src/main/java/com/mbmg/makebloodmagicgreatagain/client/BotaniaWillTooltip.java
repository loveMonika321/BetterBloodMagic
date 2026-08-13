/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.event.entity.player.ItemTooltipEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.mbmg.makebloodmagicgreatagain.client;

import com.mbmg.makebloodmagicgreatagain.BotaniaWillCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="makebloodmagicgreatagain", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class BotaniaWillTooltip {
    private static final String BOTANIA_WILL_TAG_PREFIX = "AncientWill_";
    private static final String[] WILL_NAMES = new String[]{"ahrim", "dharok", "guthan", "karil", "torag", "verac", "akrisae", "linza"};
    private static final String[] WILL_DESC_KEYS = new String[]{"botania.armorset.will_ahrim.desc", "botania.armorset.will_dharok.desc", "botania.armorset.will_guthan.desc", "botania.armorset.will_karil.desc", "botania.armorset.will_torag.desc", "botania.armorset.will_verac.desc", "botania.armorset.will_akrisae.desc", "botania.armorset.will_linza.desc"};

    @SubscribeEvent
    public static void onLivingHelmetTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack == null || stack.m_41619_()) {
            return;
        }
        if (!BotaniaWillCompat.isLivingHelmet(stack)) {
            return;
        }
        List tooltips = event.getToolTip();
        int inserted = -1;
        for (int i = 0; i < tooltips.size(); ++i) {
            String s = ((Component)tooltips.get(i)).getString();
            if (s == null || !s.contains("\u6234\u5728\u5934\u4e0a") && !s.contains("When on head") && (!s.startsWith("+") || !s.contains("\u62a4\u7532") && !s.toLowerCase(Locale.ROOT).contains("armor"))) continue;
            inserted = i;
            break;
        }
        ArrayList<MutableComponent> willLines = new ArrayList<MutableComponent>();
        for (int i = 0; i < WILL_NAMES.length; ++i) {
            if (!stack.m_41784_().m_128471_(BOTANIA_WILL_TAG_PREFIX + WILL_NAMES[i])) continue;
            willLines.add(Component.m_237115_((String)WILL_DESC_KEYS[i]).m_130940_(ChatFormatting.GRAY));
        }
        if (willLines.isEmpty()) {
            return;
        }
        if (inserted >= 0) {
            tooltips.addAll(inserted, willLines);
        } else {
            tooltips.addAll(Math.max(0, tooltips.size() - 1), willLines);
        }
    }
}


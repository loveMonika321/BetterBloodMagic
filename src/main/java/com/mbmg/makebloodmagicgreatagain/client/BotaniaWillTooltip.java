package com.mbmg.makebloodmagicgreatagain.client;

import com.mbmg.makebloodmagicgreatagain.BotaniaWillCompat;
import com.mbmg.makebloodmagicgreatagain.MakeBloodMagicGreatAgain;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 束灵头盔 Tooltip 显示已装载的意志（仿泰拉钢头盔样式）。
 * 从 ArsNouveauPerkCompat.java 迁移而来（ARS联动已删除）。
 */
@Mod.EventBusSubscriber(modid = MakeBloodMagicGreatAgain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class BotaniaWillTooltip {

	private static final String BOTANIA_WILL_TAG_PREFIX = "AncientWill_";
	private static final String[] WILL_NAMES = {
			"ahrim", "dharok", "guthan", "karil", "torag", "verac", "akrisae", "linza"
	};
	private static final String[] WILL_DESC_KEYS = {
			"botania.armorset.will_ahrim.desc",
			"botania.armorset.will_dharok.desc",
			"botania.armorset.will_guthan.desc",
			"botania.armorset.will_karil.desc",
			"botania.armorset.will_torag.desc",
			"botania.armorset.will_verac.desc",
			"botania.armorset.will_akrisae.desc",
			"botania.armorset.will_linza.desc"
	};

	@SubscribeEvent
	public static void onLivingHelmetTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		if (stack == null || stack.isEmpty()) return;
		if (!BotaniaWillCompat.isLivingHelmet(stack)) return;
		List<Component> tooltips = event.getToolTip();

		int inserted = -1;
		for (int i = 0; i < tooltips.size(); i++) {
			String s = tooltips.get(i).getString();
			if (s != null && (s.contains("戴在头上") || s.contains("When on head")
					|| (s.startsWith("+") && (s.contains("护甲") || s.toLowerCase(Locale.ROOT).contains("armor"))))) {
				inserted = i;
				break;
			}
		}
		List<Component> willLines = new ArrayList<>();
		for (int i = 0; i < WILL_NAMES.length; i++) {
			if (stack.getOrCreateTag().getBoolean(BOTANIA_WILL_TAG_PREFIX + WILL_NAMES[i])) {
				willLines.add(Component.translatable(WILL_DESC_KEYS[i]).withStyle(ChatFormatting.GRAY));
			}
		}
		if (willLines.isEmpty()) return;
		if (inserted >= 0) {
			tooltips.addAll(inserted, willLines);
		} else {
			tooltips.addAll(Math.max(0, tooltips.size() - 1), willLines);
		}
	}
}

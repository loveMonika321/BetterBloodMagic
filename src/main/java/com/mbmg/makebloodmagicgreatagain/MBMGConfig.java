package com.mbmg.makebloodmagicgreatagain;

import net.minecraftforge.common.ForgeConfigSpec;

public final class MBMGConfig {
	public static final ForgeConfigSpec COMMON_SPEC;

	public static final ForgeConfigSpec.BooleanValue enableBotaniaWill;
	public static final ForgeConfigSpec.BooleanValue enableArsNouveauFiber;
	public static final ForgeConfigSpec.BooleanValue enableInfiniteUpgradePoints;

	static {
		final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

		builder.push("botania");
		enableBotaniaWill = builder
				.comment("让束灵头盔实现植物魔法 AncientWillContainer，可像泰拉头盔一样装上六种意志。(默认: true)")
				.define("enableAncientWill", true);
		builder.pop();

		builder.push("ars_nouveau");
		enableArsNouveauFiber = builder
				.comment("让四件束灵盔甲可在新生魔艺改衣台装线程（Perk），并可在附魔装置/工作台逐级升级：\n"
						+ "Tier0=[Ⅰ 槽 ×1]   Tier1=[Ⅱ 槽 ×2]   Tier2=[Ⅲ 槽 ×3]   Tier3=[Ⅲ 槽 ×3]\n"
						+ "(默认: true)")
				.define("enableArmorPerkSlots", true);
		builder.pop();

		builder.push("avaritia");
		enableInfiniteUpgradePoints = builder
				.comment("让束灵胸甲可通过合成无尽催化剂(infinity_catalyst)将升级点数上限提升至Integer.MAX_VALUE。合成: living_plate + infinity_catalyst。仅当检测到 avaritia 时生效。(默认: true)")
				.define("enableInfiniteUpgradePoints", true);
		builder.pop();

		COMMON_SPEC = builder.build();
	}

	private MBMGConfig() {}
}

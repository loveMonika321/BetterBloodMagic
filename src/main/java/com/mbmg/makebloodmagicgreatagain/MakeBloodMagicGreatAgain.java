package com.mbmg.makebloodmagicgreatagain;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(MakeBloodMagicGreatAgain.MOD_ID)
public class MakeBloodMagicGreatAgain {
	public static final String MOD_ID = "makebloodmagicgreatagain";
	public static final Logger LOGGER = LogUtils.getLogger();

	public static MakeBloodMagicGreatAgain INSTANCE;

	private final boolean botaniaLoaded;
	private final boolean avaritiaLoaded;
	private final boolean arsNouveauLoaded;

	public MakeBloodMagicGreatAgain() {
		INSTANCE = this;
		final ModList modList = ModList.get();
		this.botaniaLoaded   = modList.isLoaded("botania");
		this.avaritiaLoaded  = modList.isLoaded("avaritia");
		this.arsNouveauLoaded = modList.isLoaded("ars_nouveau");

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MBMGConfig.COMMON_SPEC);

		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::commonSetup);
		MBMGRecipes.register(modEventBus);

		MinecraftForge.EVENT_BUS.register(this);

		LOGGER.info("[MBMG] Starting. Dependencies -> Botania: {}, Avaritia: {}, ArsNouveau: {}",
				botaniaLoaded, avaritiaLoaded, arsNouveauLoaded);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			if (botaniaLoaded && MBMGConfig.enableBotaniaWill.get()) {
				try {
					BotaniaWillCompat.bootstrap();
					LOGGER.info("[MBMG] Botania Ancient Will compat enabled for 束灵头盔.");
				} catch (Throwable t) {
					LOGGER.error("[MBMG] Failed to bootstrap Botania Ancient Will compat", t);
				}
			}

			if (arsNouveauLoaded && MBMGConfig.enableArsNouveauFiber.get()) {
				try {
					ArsNouveauPerkCompat.bootstrap();
					LOGGER.info("[MBMG] Ars Nouveau Perk compat 注册成功：束灵四件套获得改衣台纤维能力 + 附魔装置 tier 升级");
				} catch (Throwable t) {
					LOGGER.error("[MBMG] Failed to bootstrap Ars Nouveau Perk compat", t);
				}
			}

			if (avaritiaLoaded && MBMGConfig.enableInfiniteUpgradePoints.get()) {
				try {
					AvaritiaInfinityUpgradeCompat.bootstrap();
					LOGGER.info("[MBMG] Re-Avaritia 无尽升级合成已启用: living_plate + infinity_catalyst -> maxPoints = Integer.MAX_VALUE");
				} catch (Throwable t) {
					LOGGER.error("[MBMG] Failed to bootstrap Re-Avaritia infinity upgrade compat", t);
				}
			}
		});
	}

	public boolean isBotaniaWillEnabled()          { return botaniaLoaded && MBMGConfig.enableBotaniaWill.get(); }
	public boolean isArsNouveauFiberEnabled()      { return arsNouveauLoaded && MBMGConfig.enableArsNouveauFiber.get(); }
	public boolean isInfiniteUpgradePointsEnabled(){ return avaritiaLoaded && MBMGConfig.enableInfiniteUpgradePoints.get(); }
}

package com.mbmg.makebloodmagicgreatagain;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Centralised recipe serializer registration. All custom crafting recipes used
 * by the compat modules are declared here so the {@link DeferredRegister} can
 * be wired into the mod event bus during mod construction — well before
 * {@code RegisterEvent} fires. Calling {@code DeferredRegister#register(bus)}
 * inside {@code FMLCommonSetupEvent} (as the previous per-module approach did)
 * is too late: the {@code RegisterEvent} has already passed and the serializer
 * never enters the registry, causing RecipeManager to silently skip the
 * recipe JSON files.
 */
final class MBMGRecipes {
	private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
			DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MakeBloodMagicGreatAgain.MOD_ID);

	// Botania Ancient Will attach recipe (shapeless: living_helmet + AncientWillItem)
	static final RegistryObject<RecipeSerializer<?>> ANCIENT_WILL =
			SERIALIZERS.register("ancient_will_attach_bloodmagic",
					() -> new SimpleCraftingRecipeSerializer<>(BotaniaWillCompat.MBMGAncientWillRecipe::new));

	// Re-Avaritia infinity upgrade recipe (shapeless: living_plate + infinity_catalyst)
	static final RegistryObject<RecipeSerializer<?>> INFINITY_UPGRADE =
			SERIALIZERS.register("infinity_upgrade_bloodmagic",
					() -> new SimpleCraftingRecipeSerializer<>(AvaritiaInfinityUpgradeCompat.MBMGInfinityUpgradeRecipe::new));

	// Ars Nouveau 束灵盔甲 tier 升级（工作台兜底：配合附魔装置 armor_upgrade 一起注册，确保任一可用）
	//  NBT 键 "an_stack_perks" 与 StackPerkHolder.getTagString() 保持一致
	//  tier 0→1: 束灵盔甲 + 2 blaze rod (与 AN 原版 T1 一致)
	//  tier 1→2: 束灵盔甲 + 2 ender pearl + 1 chorus fruit (与 AN 原版 T2 一致)
	//  tier 2→3: 束灵盔甲 + 1 blaze rod + 2 ender pearl + 1 chorus fruit + 1 nether_star (AN 原版无 T3，自制)
	static final RegistryObject<RecipeSerializer<?>> ARMOR_UPGRADE_T1 =
			SERIALIZERS.register("armor_upgrade_t1_bloodmagic",
					() -> new SimpleCraftingRecipeSerializer<>(ArsNouveauPerkCompat.MBMGArmorUpgradeT1Recipe::new));
	static final RegistryObject<RecipeSerializer<?>> ARMOR_UPGRADE_T2 =
			SERIALIZERS.register("armor_upgrade_t2_bloodmagic",
					() -> new SimpleCraftingRecipeSerializer<>(ArsNouveauPerkCompat.MBMGArmorUpgradeT2Recipe::new));
	static final RegistryObject<RecipeSerializer<?>> ARMOR_UPGRADE_T3 =
			SERIALIZERS.register("armor_upgrade_t3_bloodmagic",
					() -> new SimpleCraftingRecipeSerializer<>(ArsNouveauPerkCompat.MBMGArmorUpgradeT3Recipe::new));

	private MBMGRecipes() {}

	static void register(IEventBus modBus) {
		SERIALIZERS.register(modBus);
	}
}

package com.mbmg.makebloodmagicgreatagain;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Botania 泰拉意志 (Ancient Will) 联动：为束灵头盔实现装备意志的合成、NBT读写
 * 以及意志战斗效果。
 *
 * <h3>实现思路</h3>
 * <ol>
 *   <li><b>合成路径</b>：Botania 的 {@code AncientWillRecipe} 通过
 *       {@code stack.getItem() instanceof AncientWillContainer} 匹配容器，但
 *       {@code ItemLivingArmor} 来自另一个 mod，无法直接更改它实现的接口列表。
 *       因此我们注册一个额外的 {@code MBMGAncientWillRecipe}，形状完全一样
 *       （AncientWillItem + 束灵头盔），但用 ForgeRegistries 做物品 ID 匹配。</li>
 *   <li><b>NBT 读写</b>：调用 Botania 的 {@code ItemNBTHelper.setBoolean / getBoolean}
 *       直接在束灵头盔 ItemStack 上写入 {@code AncientWill_<ahrim|dharok|...>} 标签，
 *       与泰拉头盔的存储布局完全一致。</li>
 *   <li><b>战斗效果</b>：泰拉头盔的 6 种意志效果是通过
 *       {@link vazkii.botania.common.item.equipment.armor.terrasteel.TerrasteelHelmItem#getCritDamageMult}
 *       与 {@code onEntityAttacked} 在 Botania 自己的事件钩子中触发的，它们只检查
 *       玩家是否穿全套泰拉盔甲 —— 这不包含束灵盔甲。因此我们把 Botania 已经写好
 *       的同标签名逻辑借过来：订阅 LivingAttackEvent / LivingHurtEvent 等 Forge
 *       通用事件，读取束灵头盔上的 AncientWill_* 标签，然后逐条复制泰拉头盔的
 *       行为代码（Ahrim=虚弱、Guthan=吸血、Torag=减速、Verac=破甲、Karil=凋零、
 *       Dharok=按缺血量增加暴击伤害）。这样不再依赖 Botania 对 AncientWillContainer
 *       的 instanceof 判断，战斗端的效果仍然一模一样。</li>
 * </ol>
 */
public final class BotaniaWillCompat {
	static final String TAG_ANCIENT_WILL_PREFIX = "AncientWill_";
	static final String[] WILL_NAMES = {"ahrim", "dharok", "guthan", "torag", "verac", "karil"};

	private static boolean bootstrapped = false;

	private static Class<?> ANCIENT_WILL_ITEM_CLASS;
	private static Field ANCIENT_WILL_TYPE_FIELD;
	private static Method ANCIENT_WILL_ADD_METHOD;    // Botania ItemNBTHelper.setBoolean
	private static Method ANCIENT_WILL_HAS_METHOD;    // Botania ItemNBTHelper.getBoolean

	private BotaniaWillCompat() {}

	static void bootstrap() throws Throwable {
		if (bootstrapped) return;
		bootstrapped = true;

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class<?> nbtHelper = Class.forName("vazkii.botania.common.helper.ItemNBTHelper", true, cl);
		ANCIENT_WILL_ADD_METHOD = nbtHelper.getMethod("setBoolean", ItemStack.class, String.class, boolean.class);
		ANCIENT_WILL_HAS_METHOD = nbtHelper.getMethod("getBoolean", ItemStack.class, String.class, boolean.class);

		ANCIENT_WILL_ITEM_CLASS = Class.forName("vazkii.botania.common.item.AncientWillItem", true, cl);
		ANCIENT_WILL_TYPE_FIELD = ANCIENT_WILL_ITEM_CLASS.getField("type");
		ANCIENT_WILL_TYPE_FIELD.setAccessible(true);

		// Subscribe the will-effect hooks onto the main Forge event bus so we don't need
		// Botania's TerrasteelHelmItem#hasTerraArmorSet() to recognise 束灵盔甲.
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(BotaniaWillEffects.class);

		MakeBloodMagicGreatAgain.LOGGER.info("[MBMG] Botania Ancient Will compat bootstrapped for bloodmagic:living_helmet (synthesis + combat hooks).");
	}

	// ---- Runtime API used from recipes / events ----

	public static boolean isLivingHelmet(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return false;
		Item item = stack.getItem();
		if (!(item instanceof ArmorItem armor)) return false;
		if (armor.getType() != ArmorItem.Type.HELMET) return false;
		var id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item);
		return id != null && "bloodmagic".equals(id.getNamespace()) && "livinghelmet".equals(id.getPath());
	}

	static boolean isAncientWillItem(Item item) {
		return item != null && ANCIENT_WILL_ITEM_CLASS != null
				&& ANCIENT_WILL_ITEM_CLASS.isInstance(item);
	}

	static Enum<?> getWillType(ItemStack willStack) {
		if (!isAncientWillItem(willStack.getItem())) return null;
		try {
			return (Enum<?>) ANCIENT_WILL_TYPE_FIELD.get(willStack.getItem());
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	static void writeWillTag(ItemStack helmet, Enum<?> willType, boolean value) {
		try {
			String tag = TAG_ANCIENT_WILL_PREFIX + willType.name().toLowerCase();
			ANCIENT_WILL_ADD_METHOD.invoke(null, helmet, tag, value);
		} catch (ReflectiveOperationException e) {
			MakeBloodMagicGreatAgain.LOGGER.error("[MBMG] Failed to write AncientWill tag", e);
		}
	}

	static boolean readWillTag(ItemStack helmet, String lowerName) {
		try {
			Boolean b = (Boolean) ANCIENT_WILL_HAS_METHOD.invoke(null, helmet,
					TAG_ANCIENT_WILL_PREFIX + lowerName, false);
			return Boolean.TRUE.equals(b);
		} catch (ReflectiveOperationException e) {
			return false;
		}
	}

	/**
	 * Our dedicated AncientWill recipe for LivingHelmet. Matches exactly when the
	 * crafting grid contains exactly 1x bloodmagic:living_helmet and exactly 1x
	 * Botania AncientWillItem (any of the six variants). The assembled output is
	 * a copy of the helmet with the corresponding AncientWill_<type> bit flipped on.
	 */
	public static final class MBMGAncientWillRecipe extends CustomRecipe {
		public MBMGAncientWillRecipe(ResourceLocation id, CraftingBookCategory category) {
			super(id, category);
		}

		@Override
		public boolean matches(CraftingContainer inv, Level level) {
			boolean foundHelmet = false;
			boolean foundWill = false;
			for (int i = 0; i < inv.getContainerSize(); i++) {
				ItemStack s = inv.getItem(i);
				if (s.isEmpty()) continue;
				if (!foundHelmet && isLivingHelmet(s)) {
					foundHelmet = true;
					continue;
				}
				if (!foundWill && isAncientWillItem(s.getItem())) {
					foundWill = true;
					continue;
				}
				return false;
			}
			return foundHelmet && foundWill;
		}

		@Override
		public ItemStack assemble(CraftingContainer inv, RegistryAccess registries) {
			ItemStack helmet = ItemStack.EMPTY;
			Enum<?> will = null;
			for (int i = 0; i < inv.getContainerSize(); i++) {
				ItemStack s = inv.getItem(i);
				if (s.isEmpty()) continue;
				if (isLivingHelmet(s)) helmet = s;
				else will = getWillType(s);
			}
			if (helmet.isEmpty() || will == null) return ItemStack.EMPTY;
			if (readWillTag(helmet, will.name().toLowerCase())) {
				// Already have this will – disallow duplication.
				return ItemStack.EMPTY;
			}
			ItemStack out = helmet.copy();
			writeWillTag(out, will, true);
			return out;
		}

		@Override
		public boolean canCraftInDimensions(int width, int height) {
			return width * height >= 2;
		}

		@Override
		public RecipeSerializer<?> getSerializer() {
			return MBMGRecipes.ANCIENT_WILL.get();
		}
	}
}

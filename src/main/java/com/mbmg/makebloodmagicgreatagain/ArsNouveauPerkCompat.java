package com.mbmg.makebloodmagicgreatagain;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.level.Level;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 新生魔艺 Ars Nouveau 4.12.7 (Forge 1.20.1) 联动。
 *
 * 目标：让血魔法的束灵四件套 (livinghelmet / livingplate / livinglegs / livingboots)
 * 获得与法师护甲完全相同的 Perk 能力：
 *   a) 放入改衣台后能被 PerkUtil.getPerkHolder(stack) 识别为 StackPerkHolder，
 *      进入"编辑态"，可在附属方块添加/删除线程（perk）；
 *   b) 在附魔装置（Enchanting Apparatus）走 ArmorUpgradeRecipe 路径可升级，
 *      tier 提升后自动解锁更多槽位；
 *   c) 渲染层读取 ArmorPerkHolder.getSlotsForTier() 显示石板Ⅰ/Ⅱ/Ⅲ。
 *
 * 槽位布局（严格按你要求）：
 *   初始 tier=0 → 显示 T1 = [ONE]                      (1 个 I 级槽)
 *   升级到 tier=1 → 显示 T2 = [TWO, TWO]                (2 个 II 级槽)
 *   升级到 tier=2 → 显示 T3 = [THREE, THREE, THREE]     (3 个 III 级槽)
 *
 * 运行时全部通过反射+动态代理对接真实 Ars Nouveau，编译期仅依赖 stubs 下的空壳避免直接依赖冲突。
 */
final class ArsNouveauPerkCompat {

	// 真实 ARS 反射缓存
	private static Class<?> PERK_REGISTRY_CLASS;
	private static Class<?> IPERK_PROVIDER_CLASS;
	private static Class<?> ARMOR_PERK_HOLDER_CLASS;
	private static Class<?> PERK_SLOT_CLASS;
	private static Object PERK_SLOT_ONE;
	private static Object PERK_SLOT_TWO;
	private static Object PERK_SLOT_THREE;
	private static Constructor<?> APH_CTOR; // (ItemStack, List<List<PerkSlot>>) 构造

	// PerkRegistry 内部的 provider Map：<Item, IPerkProvider<ItemStack>>
	// 1.20.1 中只有这一个 map，槽位布局在 ArmorPerkHolder 构造函数中传入
	private static Map<Object, Object> PERK_PROVIDER_MAP;

	static final String NBT_KEY = "armor_perks";
	static final String NBT_TIER = "tier";
	static final String NBT_PERKS = "perks";

	static void bootstrap() throws Throwable {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		// 1) 载入真实类
		PERK_REGISTRY_CLASS    = Class.forName("com.hollingsworth.arsnouveau.api.registry.PerkRegistry", true, cl);
		IPERK_PROVIDER_CLASS   = Class.forName("com.hollingsworth.arsnouveau.api.perk.IPerkProvider", true, cl);
		PERK_SLOT_CLASS        = Class.forName("com.hollingsworth.arsnouveau.api.perk.PerkSlot", true, cl);
		ARMOR_PERK_HOLDER_CLASS = Class.forName("com.hollingsworth.arsnouveau.api.perk.ArmorPerkHolder", true, cl);

		// 2) PerkSlot 静态常量：ONE / TWO / THREE
		PERK_SLOT_ONE   = PERK_SLOT_CLASS.getField("ONE").get(null);
		PERK_SLOT_TWO   = PERK_SLOT_CLASS.getField("TWO").get(null);
		PERK_SLOT_THREE = PERK_SLOT_CLASS.getField("THREE").get(null);

		// 3) ArmorPerkHolder 构造函数：真实 JAR 里是 public ArmorPerkHolder(ItemStack stack, List<List<PerkSlot>> slotsForTier)
		Constructor<?>[] ctors = ARMOR_PERK_HOLDER_CLASS.getDeclaredConstructors();
		for (Constructor<?> c : ctors) {
			Class<?>[] ps = c.getParameterTypes();
			if (ps.length == 2 && ps[0] == ItemStack.class && List.class.isAssignableFrom(ps[1])) {
				c.setAccessible(true);
				APH_CTOR = c;
				break;
			}
		}
		if (APH_CTOR == null) {
			throw new NoSuchMethodException("ArmorPerkHolder(ItemStack, List<List<PerkSlot>>) ctor not found");
		}

		// 4) 获取 PerkRegistry.itemPerkProviderMap（ConcurrentHashMap<Item, IPerkProvider<ItemStack>>）
		for (Field f : PERK_REGISTRY_CLASS.getDeclaredFields()) {
			if (!Map.class.isAssignableFrom(f.getType()) || !Modifier.isStatic(f.getModifiers())) continue;
			f.setAccessible(true);
			String n = f.getName().toLowerCase(Locale.ROOT);
			if (n.contains("provider")) {
				PERK_PROVIDER_MAP = (Map<Object, Object>) f.get(null);
				break;
			}
		}

		MakeBloodMagicGreatAgain.LOGGER.info(
				"[MBMG] ArsNouveau 反射: provider map={}, APH ctor={}",
				PERK_PROVIDER_MAP != null, APH_CTOR != null);

		// 5) 对 4 件束灵盔甲注册
		String[] parts = {"livinghelmet", "livingplate", "livinglegs", "livingboots"};
		int ok = 0;
		for (String part : parts) {
			Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation("bloodmagic", part));
			if (i == null) {
				MakeBloodMagicGreatAgain.LOGGER.warn("[MBMG] 未找到束灵盔甲: bloodmagic:{}", part);
				continue;
			}
			if (!(i instanceof ArmorItem)) {
				MakeBloodMagicGreatAgain.LOGGER.warn("[MBMG] bloodmagic:{} 不是 ArmorItem，跳过", part);
				continue;
			}

			// Tier 布局（用真实 PerkSlot 实例构造）
			List<List<Object>> tierLayout = List.of(
					List.of(PERK_SLOT_ONE),
					List.of(PERK_SLOT_TWO, PERK_SLOT_TWO),
					List.of(PERK_SLOT_THREE, PERK_SLOT_THREE, PERK_SLOT_THREE)
			);
			registerFor(i, tierLayout);
			ok++;
		}
		MakeBloodMagicGreatAgain.LOGGER.info("[MBMG] Ars Nouveau Perk compat 注册完成：{}/4 件束灵盔甲", ok);
	}

	/**
	 * 同时注册 IPerkProvider 代理 + 槽位布局 map。
	 * - IPerkProvider 代理的 getPerkHolder(stack) 返回真实 ArmorPerkHolder 实例，
	 *   满足 `PerkUtil.getPerkHolder() instanceof StackPerkHolder` 判定，改衣台由此进入编辑态
	 * - 槽位布局 map 写入 getSlotsForTier() 读取的数据源，石板渲染会用到
	 */
	private static void registerFor(Item armorItem, List<List<Object>> tierLayout) throws Throwable {
		ensureDefaultPerksNbtForItem(armorItem);

		Object provider = Proxy.newProxyInstance(
				PERK_REGISTRY_CLASS.getClassLoader(),
				new Class<?>[]{IPERK_PROVIDER_CLASS},
				(proxy, method, args) -> {
					if ("getPerkHolder".equals(method.getName()) && args != null && args.length == 1) {
						ItemStack stack = (ItemStack) args[0];
						try {
							return APH_CTOR.newInstance(stack, tierLayout);
						} catch (Throwable t) {
							MakeBloodMagicGreatAgain.LOGGER.error("[MBMG] ArmorPerkHolder 构造失败", t);
							return null;
						}
					}
					if ("equals".equals(method.getName())) return proxy == args[0];
					if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
					if ("toString".equals(method.getName()))
						return "MBMGIPerkProvider[bloodmagic:" + ForgeRegistries.ITEMS.getKey(armorItem).getPath() + "]";
					return null;
				});

		// 路径一：如果 PerkRegistry.registerPerkProvider(ItemLike, IPerkProvider) 存在则调用
		boolean registered = false;
		try {
			Method m = PERK_REGISTRY_CLASS.getMethod(
					"registerPerkProvider",
					Class.forName("net.minecraft.world.level.ItemLike", true, PERK_REGISTRY_CLASS.getClassLoader()),
					IPERK_PROVIDER_CLASS);
			m.invoke(null, armorItem, provider);
			registered = true;
		} catch (NoSuchMethodException ignore) {
		} catch (Throwable t) {
			MakeBloodMagicGreatAgain.LOGGER.warn("[MBMG] PerkRegistry.registerPerkProvider 调用失败，降级直接写 map", t);
		}
		if (!registered && PERK_PROVIDER_MAP != null) {
			try { PERK_PROVIDER_MAP.put(armorItem, provider); registered = true; }
			catch (Throwable t) { MakeBloodMagicGreatAgain.LOGGER.error("[MBMG] 写入 IPerkProvider map 失败", t); }
		}

		MakeBloodMagicGreatAgain.LOGGER.debug("[MBMG] 注册束灵盔甲 {}: providerOK={}",
				ForgeRegistries.ITEMS.getKey(armorItem), registered);
	}

	private static void ensureDefaultPerksNbtForItem(Item item) {
		// 真实 ArmorPerkHolder 会在 ItemStack 级别写 NBT；这里预留接口（不需要批量设置 Item 默认 NBT）
	}

	/**
	 * 给 ItemStack 写默认 armor_perks NBT（tier=0，perks=[]），用于第一次放入改衣台前的兜底。
	 * （实际是由 ArmorPerkHolder 的构造函数在 getPerkHolder 时写的，这里作为外部可调用入口）
	 */
	static void ensureDefaultPerksNbt(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return;
		net.minecraft.nbt.CompoundTag perks = stack.getOrCreateTagElement(NBT_KEY);
		if (!perks.contains(NBT_TIER, net.minecraft.nbt.Tag.TAG_INT)) perks.putInt(NBT_TIER, 0);
		if (!perks.contains(NBT_PERKS, net.minecraft.nbt.Tag.TAG_LIST))
			perks.put(NBT_PERKS, new net.minecraft.nbt.ListTag());
	}

	// ====================================================================
	// 工作台兜底盔甲 tier 升级（保留 NBT / 附魔 / perk / 血魔法数据）
	//   T0→T1: 束灵盔甲 + 4 blaze_fiber     + 2 magebloom_fiber
	//   T1→T2: 束灵盔甲 + 4 end_fiber       + 2 arcane_core
	//   T2→T3: 束灵盔甲 + 4 end_fiber       + 4 arcane_core     + 1 nether_star
	// ====================================================================

	private static boolean isBMItem(ItemStack s, String namespace, String... candidates) {
		if (s == null || s.isEmpty()) return false;
		ResourceLocation rl = ForgeRegistries.ITEMS.getKey(s.getItem());
		if (rl == null || !rl.getNamespace().equals(namespace)) return false;
		String p = rl.getPath();
		for (String c : candidates) if (p.equals(c)) return true;
		return false;
	}

	private static int getArmorTier(ItemStack armor) {
		if (armor == null || !armor.hasTag()) return 0;
		var tag = armor.getTag().getCompound(NBT_KEY);
		return tag.getInt(NBT_TIER);
	}

	private static ItemStack setArmorTier(ItemStack armor, int targetTier) {
		ItemStack out = armor.copy();
		out.setCount(1);
		ensureDefaultPerksNbt(out);
		out.getTag().getCompound(NBT_KEY).putInt(NBT_TIER, targetTier);
		return out;
	}

	/** 通用束灵盔甲 tier 升级配方基类 */
	abstract static class AbstractArmorUpgradeRecipe extends CustomRecipe {
		AbstractArmorUpgradeRecipe(ResourceLocation id, CraftingBookCategory category) { super(id, category); }
		abstract int targetTier();            // 期望升级到的 tier
		abstract IngSpec[] materials();       // 材料列表

		interface IngSpec {
			boolean matches(ItemStack s);
			String name();
		}
		static IngSpec arsIng(String path) {
			return new IngSpec() {
				public boolean matches(ItemStack s) { return isBMItem(s, "ars_nouveau", path); }
				public String name() { return "ars_nouveau:" + path; }
			};
		}
		static IngSpec mcIng(String path) {
			return new IngSpec() {
				public boolean matches(ItemStack s) { return isBMItem(s, "minecraft", path); }
				public String name() { return "minecraft:" + path; }
			};
		}
		static IngSpec tag(String namespace, String path, int count) {
			// 简化版：仅用于名称展示，匹配走物品名
			return arsIng(path);
		}

		private boolean isLivingArmor(ItemStack s) {
			return isBMItem(s, "bloodmagic", "livinghelmet", "livingplate", "livinglegs", "livingboots");
		}

		@Override
		public boolean matches(CraftingContainer inv, Level level) {
			ItemStack armor = ItemStack.EMPTY;
			int[] needed = new int[materials().length];
			for (int i = 0; i < inv.getContainerSize(); i++) {
				ItemStack s = inv.getItem(i);
				if (s.isEmpty()) continue;
				if (isLivingArmor(s)) {
					if (!armor.isEmpty()) return false;
					armor = s;
					continue;
				}
				boolean consumed = false;
				for (int j = 0; j < materials().length; j++) {
					if (materials()[j].matches(s)) { needed[j]++; consumed = true; break; }
				}
				if (!consumed) return false;
			}
			if (armor.isEmpty()) return false;
			if (getArmorTier(armor) != targetTier() - 1) return false;
			for (int j = 0; j < materials().length; j++) {
				// 每个材料 1 个/位置 → 实际上我们会要求数量等于 count
				if (needed[j] < requiredCount(j)) return false;
			}
			return true;
		}

		protected abstract int requiredCount(int idx);

		@Override
		public ItemStack assemble(CraftingContainer inv, RegistryAccess registries) {
			ItemStack armor = ItemStack.EMPTY;
			for (int i = 0; i < inv.getContainerSize(); i++) {
				ItemStack s = inv.getItem(i);
				if (isLivingArmor(s)) { armor = s; break; }
			}
			if (armor.isEmpty()) return ItemStack.EMPTY;
			if (getArmorTier(armor) != targetTier() - 1) return ItemStack.EMPTY;
			return setArmorTier(armor, targetTier());
		}

		@Override
		public boolean canCraftInDimensions(int w, int h) { return w * h >= 1 + totalCount(); }

		int totalCount() {
			int t = 0;
			for (int j = 0; j < materials().length; j++) t += requiredCount(j);
			return t;
		}
	}

	/** T0→T1: 4 blaze_fiber + 2 magebloom_fiber */
	public static final class MBMGArmorUpgradeT1Recipe extends AbstractArmorUpgradeRecipe {
		static final IngSpec[] MATS = { arsIng("blaze_fiber"), arsIng("magebloom_fiber") };
		static final int[] COUNTS = { 4, 2 };
		public MBMGArmorUpgradeT1Recipe(ResourceLocation id, CraftingBookCategory cat) { super(id, cat); }
		@Override int targetTier() { return 1; }
		@Override IngSpec[] materials() { return MATS; }
		@Override protected int requiredCount(int idx) { return COUNTS[idx]; }
		@Override public RecipeSerializer<?> getSerializer() { return MBMGRecipes.ARMOR_UPGRADE_T1.get(); }
	}

	/** T1→T2: 4 end_fiber + 2 arcane_core */
	public static final class MBMGArmorUpgradeT2Recipe extends AbstractArmorUpgradeRecipe {
		static final IngSpec[] MATS = { arsIng("end_fiber"), arsIng("arcane_core") };
		static final int[] COUNTS = { 4, 2 };
		public MBMGArmorUpgradeT2Recipe(ResourceLocation id, CraftingBookCategory cat) { super(id, cat); }
		@Override int targetTier() { return 2; }
		@Override IngSpec[] materials() { return MATS; }
		@Override protected int requiredCount(int idx) { return COUNTS[idx]; }
		@Override public RecipeSerializer<?> getSerializer() { return MBMGRecipes.ARMOR_UPGRADE_T2.get(); }
	}

	/** T2→T3: 4 end_fiber + 4 arcane_core + 1 nether_star */
	public static final class MBMGArmorUpgradeT3Recipe extends AbstractArmorUpgradeRecipe {
		static final IngSpec[] MATS = { arsIng("end_fiber"), arsIng("arcane_core"), mcIng("nether_star") };
		static final int[] COUNTS = { 4, 4, 1 };
		public MBMGArmorUpgradeT3Recipe(ResourceLocation id, CraftingBookCategory cat) { super(id, cat); }
		@Override int targetTier() { return 3; }
		@Override IngSpec[] materials() { return MATS; }
		@Override protected int requiredCount(int idx) { return COUNTS[idx]; }
		@Override public RecipeSerializer<?> getSerializer() { return MBMGRecipes.ARMOR_UPGRADE_T3.get(); }
	}
}

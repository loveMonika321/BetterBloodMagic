/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.inventory.CraftingContainer
 *  net.minecraft.world.item.ArmorItem
 *  net.minecraft.world.item.ArmorItem$Type
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingBookCategory
 *  net.minecraft.world.item.crafting.CustomRecipe
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.registries.ForgeRegistries
 */
package com.mbmg.makebloodmagicgreatagain;

import com.mbmg.makebloodmagicgreatagain.BotaniaWillEffects;
import com.mbmg.makebloodmagicgreatagain.MBMGRecipes;
import com.mbmg.makebloodmagicgreatagain.MakeBloodMagicGreatAgain;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;

public final class BotaniaWillCompat {
    static final String TAG_ANCIENT_WILL_PREFIX = "AncientWill_";
    static final String[] WILL_NAMES = new String[]{"ahrim", "dharok", "guthan", "torag", "verac", "karil"};
    private static boolean bootstrapped = false;
    private static Class<?> ANCIENT_WILL_ITEM_CLASS;
    private static Field ANCIENT_WILL_TYPE_FIELD;
    private static Method ANCIENT_WILL_ADD_METHOD;
    private static Method ANCIENT_WILL_HAS_METHOD;

    private BotaniaWillCompat() {
    }

    static void bootstrap() throws Throwable {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class<?> nbtHelper = Class.forName("vazkii.botania.common.helper.ItemNBTHelper", true, cl);
        ANCIENT_WILL_ADD_METHOD = nbtHelper.getMethod("setBoolean", ItemStack.class, String.class, Boolean.TYPE);
        ANCIENT_WILL_HAS_METHOD = nbtHelper.getMethod("getBoolean", ItemStack.class, String.class, Boolean.TYPE);
        ANCIENT_WILL_ITEM_CLASS = Class.forName("vazkii.botania.common.item.AncientWillItem", true, cl);
        ANCIENT_WILL_TYPE_FIELD = ANCIENT_WILL_ITEM_CLASS.getField("type");
        ANCIENT_WILL_TYPE_FIELD.setAccessible(true);
        MinecraftForge.EVENT_BUS.register(BotaniaWillEffects.class);
        MakeBloodMagicGreatAgain.LOGGER.info("[MBMG] Botania Ancient Will compat bootstrapped for bloodmagic:living_helmet (synthesis + combat hooks).");
    }

    public static boolean isLivingHelmet(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (!(item instanceof ArmorItem)) {
            return false;
        }
        ArmorItem armor = (ArmorItem)item;
        if (armor.getType() != ArmorItem.Type.HELMET) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        return id != null && "bloodmagic".equals(id.getNamespace()) && "livinghelmet".equals(id.getPath());
    }

    static boolean isAncientWillItem(Item item) {
        return item != null && ANCIENT_WILL_ITEM_CLASS != null && ANCIENT_WILL_ITEM_CLASS.isInstance(item);
    }

    static Enum<?> getWillType(ItemStack willStack) {
        if (!BotaniaWillCompat.isAncientWillItem(willStack.getItem())) {
            return null;
        }
        try {
            return (Enum)ANCIENT_WILL_TYPE_FIELD.get(willStack.getItem());
        }
        catch (IllegalAccessException e) {
            return null;
        }
    }

    static void writeWillTag(ItemStack helmet, Enum<?> willType, boolean value) {
        try {
            String tag = TAG_ANCIENT_WILL_PREFIX + MakeBloodMagicGreatAgain.lowerName(willType);
            ANCIENT_WILL_ADD_METHOD.invoke(null, helmet, tag, value);
        }
        catch (ReflectiveOperationException e) {
            MakeBloodMagicGreatAgain.LOGGER.error("[MBMG] Failed to write AncientWill tag", (Throwable)e);
        }
    }

    static boolean readWillTag(ItemStack helmet, String lowerName) {
        try {
            Boolean b = (Boolean)ANCIENT_WILL_HAS_METHOD.invoke(null, helmet, TAG_ANCIENT_WILL_PREFIX + lowerName, false);
            return Boolean.TRUE.equals(b);
        }
        catch (ReflectiveOperationException e) {
            return false;
        }
    }

    public static final class MBMGAncientWillRecipe
    extends CustomRecipe {
        public MBMGAncientWillRecipe(ResourceLocation id, CraftingBookCategory category) {
            super(id, category);
        }

        public boolean matches(CraftingContainer inv, Level level) {
            boolean foundHelmet = false;
            boolean foundWill = false;
            for (int i = 0; i < inv.getContainerSize(); ++i) {
                ItemStack s = inv.getItem(i);
                if (s.isEmpty()) continue;
                if (!foundHelmet && BotaniaWillCompat.isLivingHelmet(s)) {
                    foundHelmet = true;
                    continue;
                }
                if (!foundWill && BotaniaWillCompat.isAncientWillItem(s.getItem())) {
                    foundWill = true;
                    continue;
                }
                return false;
            }
            return foundHelmet && foundWill;
        }

        public ItemStack assemble(CraftingContainer inv, RegistryAccess registries) {
            ItemStack helmet = ItemStack.EMPTY;
            Enum<?> will = null;
            for (int i = 0; i < inv.getContainerSize(); ++i) {
                ItemStack s = inv.getItem(i);
                if (s.isEmpty()) continue;
                if (BotaniaWillCompat.isLivingHelmet(s)) {
                    helmet = s;
                    continue;
                }
                will = BotaniaWillCompat.getWillType(s);
            }
            if (helmet.isEmpty() || will == null) {
                return ItemStack.EMPTY;
            }
            if (BotaniaWillCompat.readWillTag(helmet, MakeBloodMagicGreatAgain.lowerName(will))) {
                return ItemStack.EMPTY;
            }
            ItemStack out = helmet.copy();
            BotaniaWillCompat.writeWillTag(out, will, true);
            return out;
        }

        public boolean canCraftInDimensions(int width, int height) {
            return width * height >= 2;
        }

        public RecipeSerializer<?> getSerializer() {
            return (RecipeSerializer)MBMGRecipes.ANCIENT_WILL.get();
        }
    }
}


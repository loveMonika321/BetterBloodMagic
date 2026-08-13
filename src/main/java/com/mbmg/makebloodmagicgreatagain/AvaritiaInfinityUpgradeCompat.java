/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
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
 *  net.minecraftforge.registries.ForgeRegistries
 */
package com.mbmg.makebloodmagicgreatagain;

import com.mbmg.makebloodmagicgreatagain.MBMGRecipes;
import com.mbmg.makebloodmagicgreatagain.MakeBloodMagicGreatAgain;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

final class AvaritiaInfinityUpgradeCompat {
    static final String LIVING_STATS_TAG = "livingStats";
    static final String MAX_POINTS_KEY = "maxPoints";
    static final String UPGRADES_KEY = "upgrades";
    static final String EVOLVED_KEY = "evolved";
    private static boolean bootstrapped = false;

    private AvaritiaInfinityUpgradeCompat() {
    }

    static void bootstrap() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;
        MakeBloodMagicGreatAgain.LOGGER.info("[MBMG] Re-Avaritia \u65e0\u5c3d\u5347\u7ea7\u5408\u6210\u5df2\u6ce8\u518c: living_plate + infinity_catalyst -> maxPoints = Integer.MAX_VALUE");
    }

    static boolean isLivingPlate(ItemStack stack) {
        if (stack == null || stack.m_41619_()) {
            return false;
        }
        Item item = stack.m_41720_();
        if (!(item instanceof ArmorItem)) {
            return false;
        }
        ArmorItem armor = (ArmorItem)item;
        if (armor.m_266204_() != ArmorItem.Type.CHESTPLATE) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey((Object)item);
        return id != null && "bloodmagic".equals(id.m_135827_()) && "livingplate".equals(id.m_135815_());
    }

    static boolean isInfinityCatalyst(ItemStack stack) {
        if (stack == null || stack.m_41619_()) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey((Object)stack.m_41720_());
        return id != null && "avaritia".equals(id.m_135827_()) && "infinity_catalyst".equals(id.m_135815_());
    }

    static boolean isAlreadyMaxed(ItemStack plate) {
        if (!plate.m_41782_() || !plate.m_41783_().m_128441_(LIVING_STATS_TAG)) {
            return false;
        }
        return plate.m_41783_().m_128469_(LIVING_STATS_TAG).m_128451_(MAX_POINTS_KEY) >= Integer.MAX_VALUE;
    }

    static ItemStack applyMaxPoints(ItemStack plate) {
        CompoundTag livingStats;
        ItemStack out = plate.m_41777_();
        CompoundTag tag = out.m_41784_();
        if (tag.m_128441_(LIVING_STATS_TAG)) {
            livingStats = tag.m_128469_(LIVING_STATS_TAG);
        } else {
            livingStats = new CompoundTag();
            livingStats.m_128365_(UPGRADES_KEY, (Tag)new ListTag());
            livingStats.m_128379_(EVOLVED_KEY, true);
        }
        livingStats.m_128405_(MAX_POINTS_KEY, Integer.MAX_VALUE);
        tag.m_128365_(LIVING_STATS_TAG, (Tag)livingStats);
        return out;
    }

    public static final class MBMGInfinityUpgradeRecipe
    extends CustomRecipe {
        public MBMGInfinityUpgradeRecipe(ResourceLocation id, CraftingBookCategory category) {
            super(id, category);
        }

        public boolean matches(CraftingContainer inv, Level level) {
            boolean foundPlate = false;
            boolean foundCatalyst = false;
            for (int i = 0; i < inv.m_6643_(); ++i) {
                ItemStack s = inv.m_8020_(i);
                if (s.m_41619_()) continue;
                if (!foundPlate && AvaritiaInfinityUpgradeCompat.isLivingPlate(s)) {
                    foundPlate = true;
                    continue;
                }
                if (!foundCatalyst && AvaritiaInfinityUpgradeCompat.isInfinityCatalyst(s)) {
                    foundCatalyst = true;
                    continue;
                }
                return false;
            }
            return foundPlate && foundCatalyst;
        }

        public ItemStack assemble(CraftingContainer inv, RegistryAccess registries) {
            ItemStack plate = ItemStack.f_41583_;
            for (int i = 0; i < inv.m_6643_(); ++i) {
                ItemStack s = inv.m_8020_(i);
                if (s.m_41619_() || !AvaritiaInfinityUpgradeCompat.isLivingPlate(s)) continue;
                plate = s;
            }
            if (plate.m_41619_()) {
                return ItemStack.f_41583_;
            }
            if (AvaritiaInfinityUpgradeCompat.isAlreadyMaxed(plate)) {
                return ItemStack.f_41583_;
            }
            return AvaritiaInfinityUpgradeCompat.applyMaxPoints(plate);
        }

        public boolean m_8004_(int width, int height) {
            return width * height >= 2;
        }

        public RecipeSerializer<?> m_7707_() {
            return (RecipeSerializer)MBMGRecipes.INFINITY_UPGRADE.get();
        }
    }
}


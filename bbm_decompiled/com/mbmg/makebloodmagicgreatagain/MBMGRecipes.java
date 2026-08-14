/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package com.mbmg.makebloodmagicgreatagain;

import com.mbmg.makebloodmagicgreatagain.AvaritiaInfinityUpgradeCompat;
import com.mbmg.makebloodmagicgreatagain.BotaniaWillCompat;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

final class MBMGRecipes {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.RECIPE_SERIALIZERS, (String)"makebloodmagicgreatagain");
    static final RegistryObject<RecipeSerializer<?>> ANCIENT_WILL = SERIALIZERS.register("ancient_will_attach_bloodmagic", () -> new SimpleCraftingRecipeSerializer(BotaniaWillCompat.MBMGAncientWillRecipe::new));
    static final RegistryObject<RecipeSerializer<?>> INFINITY_UPGRADE = SERIALIZERS.register("infinity_upgrade_bloodmagic", () -> new SimpleCraftingRecipeSerializer(AvaritiaInfinityUpgradeCompat.MBMGInfinityUpgradeRecipe::new));

    private MBMGRecipes() {
    }

    static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraftforge.common.extensions.IForgeMenuType
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.RegistryObject
 */
package com.mbmg.makebloodmagicgreatagain;

import com.mbmg.makebloodmagicgreatagain.RitualSelectionMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class MBMGMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create((ResourceKey)Registries.MENU, (String)"makebloodmagicgreatagain");
    public static final RegistryObject<MenuType<RitualSelectionMenu>> RITUAL_SELECT = MENUS.register("ritual_select", () -> IForgeMenuType.create((id, inv, data) -> new RitualSelectionMenu(id, inv, data.readVarInt())));

    static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}


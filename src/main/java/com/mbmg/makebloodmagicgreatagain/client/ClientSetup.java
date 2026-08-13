/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.gui.screens.MenuScreens
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RegisterKeyMappingsEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
 */
package com.mbmg.makebloodmagicgreatagain.client;

import com.mbmg.makebloodmagicgreatagain.MBMGMenus;
import com.mbmg.makebloodmagicgreatagain.client.RitualSelectionScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid="makebloodmagicgreatagain", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public class ClientSetup {
    public static final KeyMapping KEY_OPEN_RITUAL_SELECT = new KeyMapping("mbmg.key.ritual_select", 90, "mbmg.key.categories.bloodmagic");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.m_96206_((MenuType)((MenuType)MBMGMenus.RITUAL_SELECT.get()), RitualSelectionScreen::new));
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(KEY_OPEN_RITUAL_SELECT);
    }
}


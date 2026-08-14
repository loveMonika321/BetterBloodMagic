/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.mbmg.makebloodmagicgreatagain.client;

import com.mbmg.makebloodmagicgreatagain.MBMGNetwork;
import com.mbmg.makebloodmagicgreatagain.OpenRitualSelectPacket;
import com.mbmg.makebloodmagicgreatagain.RitualSelectionMenu;
import com.mbmg.makebloodmagicgreatagain.client.ClientSetup;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="makebloodmagicgreatagain", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class ClientForgeEvents {
    private static boolean lastZDown = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            lastZDown = false;
            return;
        }
        boolean now = ClientSetup.KEY_OPEN_RITUAL_SELECT.consumeClick();
        if (!now) {
            return;
        }
        ItemStack main = mc.player.getMainHandItem();
        ItemStack off = mc.player.getOffhandItem();
        if (!RitualSelectionMenu.isRitualDiviner(main) && !RitualSelectionMenu.isRitualDiviner(off)) {
            return;
        }
        MBMGNetwork.CHANNEL.sendToServer((Object)OpenRitualSelectPacket.INSTANCE);
    }
}


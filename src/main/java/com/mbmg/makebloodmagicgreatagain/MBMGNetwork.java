/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.simple.SimpleChannel
 */
package com.mbmg.makebloodmagicgreatagain;

import com.mbmg.makebloodmagicgreatagain.OpenRitualSelectPacket;
import com.mbmg.makebloodmagicgreatagain.SelectRitualPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class MBMGNetwork {
    private static final String PROTOCOL_VERSION = "2";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("makebloodmagicgreatagain", "main"), () -> "2", "2"::equals, "2"::equals);
    private static int nextId = 0;

    public static void register() {
        CHANNEL.messageBuilder(SelectRitualPacket.class, nextId++).encoder(SelectRitualPacket::encode).decoder(SelectRitualPacket::decode).consumerMainThread(SelectRitualPacket::handle).add();
        CHANNEL.messageBuilder(OpenRitualSelectPacket.class, nextId++).encoder(OpenRitualSelectPacket::encode).decoder(OpenRitualSelectPacket::decode).consumerMainThread(OpenRitualSelectPacket::handle).add();
    }

    private MBMGNetwork() {
    }
}


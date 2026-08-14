/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.network.NetworkEvent$Context
 *  net.minecraftforge.network.NetworkHooks
 */
package com.mbmg.makebloodmagicgreatagain;

import com.mbmg.makebloodmagicgreatagain.RitualSelectionMenu;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

public class OpenRitualSelectPacket {
    public static final OpenRitualSelectPacket INSTANCE = new OpenRitualSelectPacket();

    public static void encode(OpenRitualSelectPacket msg, FriendlyByteBuf buf) {
    }

    public static OpenRitualSelectPacket decode(FriendlyByteBuf buf) {
        return INSTANCE;
    }

    public static void handle(OpenRitualSelectPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            int slot;
            ServerPlayer player = ((NetworkEvent.Context)ctx.get()).getSender();
            if (player == null) {
                return;
            }
            ItemStack main = player.m_21205_();
            ItemStack off = player.m_21206_();
            if (RitualSelectionMenu.isRitualDiviner(main)) {
                slot = player.m_150109_().f_35977_;
            } else if (RitualSelectionMenu.isRitualDiviner(off)) {
                slot = 35;
            } else {
                return;
            }
            final int heldSlot = slot;
            final boolean isOffhand = RitualSelectionMenu.isRitualDiviner(off) && !RitualSelectionMenu.isRitualDiviner(main);
            NetworkHooks.openScreen((ServerPlayer)player, (MenuProvider)new MenuProvider(){

                public AbstractContainerMenu m_7208_(int id, Inventory inv, Player p) {
                    return new RitualSelectionMenu(id, inv, isOffhand ? -1 : heldSlot);
                }

                public Component m_5446_() {
                    return Component.m_237110_((String)"mbmg.ritual_select.title", (Object[])new Object[]{Component.m_237115_((String)"item.bloodmagic.ritualdiviner")});
                }
            }, buf -> buf.m_130130_(isOffhand ? -1 : heldSlot));
        });
        ctx.get().setPacketHandled(true);
    }
}


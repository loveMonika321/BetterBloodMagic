/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.mbmg.makebloodmagicgreatagain;

import com.mbmg.makebloodmagicgreatagain.RitualSelectionMenu;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class SelectRitualPacket {
    private final ResourceLocation ritualId;

    public SelectRitualPacket(ResourceLocation ritualId) {
        this.ritualId = ritualId;
    }

    public static void encode(SelectRitualPacket msg, FriendlyByteBuf buf) {
        if (msg.ritualId != null) {
            buf.writeBoolean(true);
            buf.m_130085_(msg.ritualId);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static SelectRitualPacket decode(FriendlyByteBuf buf) {
        ResourceLocation id = null;
        if (buf.readBoolean()) {
            id = buf.m_130281_();
        }
        return new SelectRitualPacket(id);
    }

    public static void handle(SelectRitualPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            MutableComponent msgComp;
            ServerPlayer player = ((NetworkEvent.Context)ctx.get()).getSender();
            if (player == null) {
                return;
            }
            AbstractContainerMenu patt1318$temp = player.f_36096_;
            if (!(patt1318$temp instanceof RitualSelectionMenu)) {
                return;
            }
            RitualSelectionMenu menu = (RitualSelectionMenu)patt1318$temp;
            ItemStack diviner = menu.getHeldDiviner(player.m_150109_());
            if (!RitualSelectionMenu.isRitualDiviner(diviner)) {
                return;
            }
            ResourceLocation id = msg.ritualId;
            if (id != null && menu.availableRituals.stream().noneMatch(e -> id.equals((Object)e.id))) {
                return;
            }
            RitualSelectionMenu.setHeldDivinerNbt((Player)player, menu.heldSlotId, id);
            if (id != null) {
                String translateKey = "ritual." + id.m_135827_() + "." + id.m_135815_();
                msgComp = Component.m_237110_((String)"mbmg.ritual_select.selected", (Object[])new Object[]{Component.m_237110_((String)translateKey, (Object[])new Object[]{id.toString()})});
            } else {
                msgComp = Component.m_237115_((String)"mbmg.ritual_select.cleared");
            }
            player.m_240418_((Component)msgComp, true);
        });
        ctx.get().setPacketHandled(true);
    }
}


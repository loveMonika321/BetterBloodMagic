/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.registries.ForgeRegistries
 */
package com.mbmg.makebloodmagicgreatagain;

import com.mbmg.makebloodmagicgreatagain.MBMGMenus;
import com.mbmg.makebloodmagicgreatagain.MakeBloodMagicGreatAgain;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class RitualSelectionMenu
extends AbstractContainerMenu {
    private static Method RITUAL_MANAGER_GET_SORTED;
    private static Method RITUAL_MANAGER_GET_ID;
    private static Method RITUAL_MANAGER_ENABLED;
    private static Method CAN_DIVINER_PERFORM;
    private static Object RITUAL_MANAGER_INSTANCE;
    private static Class<?> RITUAL_CLASS;
    private static Class<?> ITEM_RITUAL_DIVINER_CLASS;
    public final int heldSlotId;
    public final int divinerType;
    public final List<RitualEntry> availableRituals;
    public final ResourceLocation currentRitualId;
    public static final int VISIBLE_COUNT = 10;
    public int scrollOffset = 0;

    public RitualSelectionMenu(int containerId, Inventory inv, int heldSlotId) {
        super((MenuType)MBMGMenus.RITUAL_SELECT.get(), containerId);
        this.heldSlotId = heldSlotId;
        ItemStack held = this.getHeldDiviner(inv);
        this.divinerType = RitualSelectionMenu.readDivinerType(held);
        this.currentRitualId = RitualSelectionMenu.readCurrentRitualId(held);
        this.availableRituals = RitualSelectionMenu.collectAvailable(held);
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot((Container)inv, col + row * 9 + 9, 8 + col * 18, 190 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot((Container)inv, col, 8 + col * 18, 248));
        }
    }

    public ItemStack getHeldDiviner(Inventory inv) {
        if (this.heldSlotId < 0) {
            return inv.player != null ? inv.player.getOffhandItem() : ItemStack.EMPTY;
        }
        if (this.heldSlotId < inv.getContainerSize()) {
            return inv.getItem(this.heldSlotId);
        }
        return ItemStack.EMPTY;
    }

    public static void setHeldDivinerNbt(Player player, int slot, ResourceLocation id) {
        ItemStack diviner = slot < 0 ? player.getOffhandItem() : player.getInventory().getItem(slot);
        RitualSelectionMenu.writeCurrentRitualId(diviner, id);
    }

    @Deprecated
    public static void writeCurrentRitualIdStatic(Player player, int slot, ResourceLocation id) {
        RitualSelectionMenu.setHeldDivinerNbt(player, slot, id);
    }

    public static boolean isRitualDiviner(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !"bloodmagic".equals(id.getNamespace())) {
            return false;
        }
        String p = id.getPath();
        return p.equals("ritualdiviner") || p.equals("ritualdivinerdusk");
    }

    public static int readDivinerType(ItemStack diviner) {
        if (diviner == null || diviner.isEmpty()) {
            return 0;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(diviner.getItem());
        if (id != null && "ritualdivinerdusk".equals(id.getPath())) {
            return 1;
        }
        return 0;
    }

    public static ResourceLocation readCurrentRitualId(ItemStack diviner) {
        if (diviner == null || !diviner.hasTag()) {
            return null;
        }
        String s = diviner.getTag().getString("current_ritual");
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return new ResourceLocation(s);
        }
        catch (Throwable t) {
            return null;
        }
    }

    public static void writeCurrentRitualId(ItemStack diviner, ResourceLocation ritualId) {
        if (diviner == null || diviner.isEmpty()) {
            return;
        }
        String v = ritualId != null ? ritualId.toString() : "";
        diviner.getOrCreateTag().putString("current_ritual", v);
    }

    private static List<RitualEntry> collectAvailable(ItemStack diviner) {
        ArrayList<RitualEntry> out = new ArrayList<RitualEntry>();
        try {
            if (RITUAL_MANAGER_INSTANCE == null || RITUAL_MANAGER_GET_SORTED == null) {
                MakeBloodMagicGreatAgain.LOGGER.warn("[MBMG] Ritual reflection not initialized \u2014 0 rituals");
                return out;
            }
            Object rituals = RITUAL_MANAGER_GET_SORTED.invoke(RITUAL_MANAGER_INSTANCE, new Object[0]);
            if (!(rituals instanceof Iterable)) {
                MakeBloodMagicGreatAgain.LOGGER.warn("[MBMG] getSortedRituals not Iterable: {}", rituals);
                return out;
            }
            Iterable it = (Iterable)rituals;
            Item itemObj = diviner != null && !diviner.isEmpty() ? diviner.getItem() : null;
            for (Object ritual : it) {
                try {
                    String idStr = null;
                    if (RITUAL_MANAGER_GET_ID != null) {
                        idStr = (String)RITUAL_MANAGER_GET_ID.invoke(RITUAL_MANAGER_INSTANCE, ritual);
                    }
                    if (idStr == null) continue;
                    boolean enabled = true;
                    if (RITUAL_MANAGER_ENABLED != null) {
                        enabled = Boolean.TRUE.equals(RITUAL_MANAGER_ENABLED.invoke(RITUAL_MANAGER_INSTANCE, idStr, false));
                    }
                    if (!enabled) continue;
                    boolean canPerform = true;
                    if (CAN_DIVINER_PERFORM != null && itemObj != null) {
                        canPerform = Boolean.TRUE.equals(CAN_DIVINER_PERFORM.invoke(itemObj, diviner, ritual));
                    }
                    if (!canPerform) continue;
                    ResourceLocation rl = null;
                    try {
                        rl = new ResourceLocation(idStr);
                    }
                    catch (Throwable t) {
                        continue;
                    }
                    String name = "ritual." + rl.getNamespace() + "." + rl.getPath();
                    out.add(new RitualEntry(rl, name));
                }
                catch (Throwable throwable) {}
            }
        }
        catch (Throwable t) {
            MakeBloodMagicGreatAgain.LOGGER.error("[MBMG] \u6536\u96c6\u53ef\u7528\u4eea\u5f0f\u5217\u8868\u5931\u8d25", t);
        }
        MakeBloodMagicGreatAgain.LOGGER.info("[MBMG] \u53ef\u7528\u4eea\u5f0f\u6570\u91cf: {}", (Object)out.size());
        return Collections.unmodifiableList(out);
    }

    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public boolean stillValid(Player player) {
        return RitualSelectionMenu.isRitualDiviner(this.getHeldDiviner(player.getInventory()));
    }

    static {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class<?> bloodMagicClass = Class.forName("wayoftime.bloodmagic.BloodMagic", true, cl);
            Field rmField = bloodMagicClass.getDeclaredField("RITUAL_MANAGER");
            rmField.setAccessible(true);
            RITUAL_MANAGER_INSTANCE = rmField.get(null);
            if (RITUAL_MANAGER_INSTANCE != null) {
                Class<?> rmClass = RITUAL_MANAGER_INSTANCE.getClass();
                Method[] methodArray = rmClass.getMethods();
                int n = methodArray.length;
                for (int i = 0; i < n; ++i) {
                    Method m = methodArray[i];
                    String mn = m.getName();
                    if (mn.equals("getSortedRituals") && m.getParameterCount() == 0) {
                        RITUAL_MANAGER_GET_SORTED = m;
                        continue;
                    }
                    if (mn.equals("getId") && m.getParameterCount() == 1) {
                        RITUAL_MANAGER_GET_ID = m;
                        continue;
                    }
                    if (!mn.equals("enabled") || m.getParameterCount() != 2) continue;
                    RITUAL_MANAGER_ENABLED = m;
                }
            }
            ITEM_RITUAL_DIVINER_CLASS = Class.forName("wayoftime.bloodmagic.common.item.ItemRitualDiviner", true, cl);
            for (Method m : ITEM_RITUAL_DIVINER_CLASS.getMethods()) {
                if (!m.getName().equals("canDivinerPerformRitual") || m.getParameterCount() != 2 || m.getParameterTypes()[0] != ItemStack.class) continue;
                m.setAccessible(true);
                CAN_DIVINER_PERFORM = m;
                break;
            }
            RITUAL_CLASS = Class.forName("wayoftime.bloodmagic.ritual.Ritual", true, cl);
            MakeBloodMagicGreatAgain.LOGGER.info("[MBMG] Ritual reflection init: manager={}, sorted={}, getId={}, enabled={}, canPerform={}, ritualClass={}", new Object[]{RITUAL_MANAGER_INSTANCE != null, RITUAL_MANAGER_GET_SORTED != null, RITUAL_MANAGER_GET_ID != null, RITUAL_MANAGER_ENABLED != null, CAN_DIVINER_PERFORM != null, RITUAL_CLASS != null});
        }
        catch (Throwable t) {
            MakeBloodMagicGreatAgain.LOGGER.error("[MBMG] \u521d\u59cb\u5316 RitualManager \u53cd\u5c04\u5931\u8d25", t);
        }
    }

    public static final class RitualEntry {
        public final ResourceLocation id;
        public final String displayName;

        public RitualEntry(ResourceLocation id, String displayName) {
            this.id = id;
            this.displayName = displayName == null ? id.toString() : displayName;
        }
    }
}


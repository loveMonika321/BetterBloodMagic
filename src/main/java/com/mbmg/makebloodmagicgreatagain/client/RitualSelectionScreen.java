/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.AbstractContainerMenu
 */
package com.mbmg.makebloodmagicgreatagain.client;

import com.mbmg.makebloodmagicgreatagain.MBMGNetwork;
import com.mbmg.makebloodmagicgreatagain.RitualSelectionMenu;
import com.mbmg.makebloodmagicgreatagain.SelectRitualPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class RitualSelectionScreen
extends AbstractContainerScreen<RitualSelectionMenu> {
    private static final int ENTRY_HEIGHT = 18;
    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 214;
    private static final int ENTRY_START_X_OFFSET = 8;
    private static final int ENTRY_START_Y_OFFSET = 26;
    private int leftPanelX;
    private int topPanelY;
    private int scrollBtnX;
    private int scrollUpY;
    private int scrollDownY;
    private int clearBtnX;
    private int clearBtnY;
    private final List<EntryButton> buttons = new ArrayList<EntryButton>();

    public RitualSelectionScreen(RitualSelectionMenu menu, Inventory inv, Component title) {
        super((AbstractContainerMenu)menu, inv, title);
        this.f_97726_ = 240;
        this.f_97727_ = 214;
        this.f_97731_ = 114;
    }

    protected void m_7856_() {
        super.m_7856_();
        this.leftPanelX = this.f_96543_ / 2 - 120;
        this.topPanelY = this.f_96544_ / 2 - 107;
        this.f_97735_ = this.leftPanelX;
        this.f_97736_ = this.topPanelY;
        this.scrollBtnX = this.leftPanelX + 240 - 22;
        this.scrollUpY = this.topPanelY + 26 - 2;
        this.scrollDownY = this.scrollUpY + 180 - 14;
        this.clearBtnX = this.leftPanelX + 8;
        this.clearBtnY = this.topPanelY + 26 + 180 + 4;
        this.rebuildButtons();
    }

    private void rebuildButtons() {
        for (EntryButton b : this.buttons) {
            this.m_169411_((GuiEventListener)b);
        }
        this.buttons.clear();
        List<RitualSelectionMenu.RitualEntry> list = ((RitualSelectionMenu)this.f_97732_).availableRituals;
        int maxVisible = 10;
        int visibleCount = Math.min(maxVisible, Math.max(0, list.size() - ((RitualSelectionMenu)this.f_97732_).scrollOffset));
        for (int i = 0; i < visibleCount; ++i) {
            int idx = ((RitualSelectionMenu)this.f_97732_).scrollOffset + i;
            RitualSelectionMenu.RitualEntry entry = list.get(idx);
            int y = this.topPanelY + 26 + i * 18;
            boolean selected = ((RitualSelectionMenu)this.f_97732_).currentRitualId != null && ((RitualSelectionMenu)this.f_97732_).currentRitualId.equals((Object)entry.id);
            EntryButton btn = new EntryButton(this.leftPanelX + 8, y, 208, 16, idx, entry, selected);
            this.m_142416_((GuiEventListener)btn);
            this.buttons.add(btn);
        }
    }

    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(graphics);
        super.m_88315_(graphics, mouseX, mouseY, partialTick);
        this.m_280072_(graphics, mouseX, mouseY);
    }

    protected void m_7286_(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        boolean clearHover;
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        int x = this.leftPanelX;
        int y = this.topPanelY;
        graphics.m_280509_(x, y, x + 240, y + 214, -15066578);
        graphics.m_280509_(x, y, x + 240, y + 1, -11908502);
        graphics.m_280509_(x, y, x + 1, y + 214, -11908502);
        graphics.m_280509_(x + 240 - 1, y, x + 240, y + 214, -11908502);
        graphics.m_280509_(x, y + 214 - 1, x + 240, y + 214, -11908502);
        String typeStr = ((RitualSelectionMenu)this.f_97732_).divinerType == 1 ? "\u3010\u8584\u66ae\u3011" : "";
        MutableComponent title = Component.m_237110_((String)"mbmg.ritual_select.title", (Object[])new Object[]{Component.m_237115_((String)"item.bloodmagic.ritualdiviner").getString() + typeStr});
        graphics.m_280430_(this.f_96547_, (Component)title, x + 8, y + 8, -2838729);
        int total = ((RitualSelectionMenu)this.f_97732_).availableRituals.size();
        int from = Math.min(total, ((RitualSelectionMenu)this.f_97732_).scrollOffset + 1);
        int to = Math.min(total, ((RitualSelectionMenu)this.f_97732_).scrollOffset + 10);
        String range = total == 0 ? "0" : from + "-" + to + " / " + total;
        graphics.m_280488_(this.f_96547_, range, x + 240 - 28 - this.f_96547_.m_92895_(range), y + 8, -5592406);
        this.renderScrollButton(graphics, this.scrollBtnX, this.scrollUpY, "\u25b2", mouseX, mouseY, ((RitualSelectionMenu)this.f_97732_).scrollOffset > 0);
        this.renderScrollButton(graphics, this.scrollBtnX, this.scrollDownY, "\u25bc", mouseX, mouseY, ((RitualSelectionMenu)this.f_97732_).scrollOffset + 10 < total);
        boolean canClear = ((RitualSelectionMenu)this.f_97732_).currentRitualId != null;
        boolean bl = clearHover = mouseX >= this.clearBtnX && mouseX < this.clearBtnX + 80 && mouseY >= this.clearBtnY && mouseY < this.clearBtnY + 16;
        int clearBg = !canClear ? -12965334 : (clearHover ? -9815494 : -10868182);
        graphics.m_280509_(this.clearBtnX, this.clearBtnY, this.clearBtnX + 80, this.clearBtnY + 16, clearBg);
        graphics.m_280653_(this.f_96547_, (Component)Component.m_237115_((String)"mbmg.ritual_select.clear"), this.clearBtnX + 40, this.clearBtnY + 4, canClear ? -1 : -7829368);
    }

    private void renderScrollButton(GuiGraphics g, int x, int y, String label, int mx, int my, boolean enabled) {
        boolean hover;
        boolean bl = hover = mx >= x && mx < x + 16 && my >= y && my < y + 14;
        int color = !enabled ? -14013894 : (hover ? -10855814 : -12961190);
        g.m_280509_(x, y, x + 16, y + 14, color);
        g.m_280653_(this.f_96547_, (Component)Component.m_237113_((String)label), x + 8, y + 3, enabled ? -1 : -7829368);
    }

    protected void m_280003_(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    public boolean m_6375_(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (mouseX >= (double)this.scrollBtnX && mouseX < (double)(this.scrollBtnX + 16) && mouseY >= (double)this.scrollUpY && mouseY < (double)(this.scrollUpY + 14) && ((RitualSelectionMenu)this.f_97732_).scrollOffset > 0) {
                ((RitualSelectionMenu)this.f_97732_).scrollOffset = Math.max(0, ((RitualSelectionMenu)this.f_97732_).scrollOffset - 1);
                this.rebuildButtons();
                return true;
            }
            if (mouseX >= (double)this.scrollBtnX && mouseX < (double)(this.scrollBtnX + 16) && mouseY >= (double)this.scrollDownY && mouseY < (double)(this.scrollDownY + 14) && ((RitualSelectionMenu)this.f_97732_).scrollOffset + 10 < ((RitualSelectionMenu)this.f_97732_).availableRituals.size()) {
                ++((RitualSelectionMenu)this.f_97732_).scrollOffset;
                this.rebuildButtons();
                return true;
            }
            if (((RitualSelectionMenu)this.f_97732_).currentRitualId != null && mouseX >= (double)this.clearBtnX && mouseX < (double)(this.clearBtnX + 80) && mouseY >= (double)this.clearBtnY && mouseY < (double)(this.clearBtnY + 16)) {
                MBMGNetwork.CHANNEL.sendToServer((Object)new SelectRitualPacket(null));
                this.m_7379_();
                return true;
            }
        }
        return super.m_6375_(mouseX, mouseY, button);
    }

    public boolean m_6050_(double x, double y, double deltaY) {
        int before = ((RitualSelectionMenu)this.f_97732_).scrollOffset;
        int count = ((RitualSelectionMenu)this.f_97732_).availableRituals.size();
        int max = Math.max(0, count - 10);
        if (deltaY > 0.0) {
            ((RitualSelectionMenu)this.f_97732_).scrollOffset = Math.max(0, ((RitualSelectionMenu)this.f_97732_).scrollOffset - 1);
        } else if (deltaY < 0.0) {
            ((RitualSelectionMenu)this.f_97732_).scrollOffset = Math.min(max, ((RitualSelectionMenu)this.f_97732_).scrollOffset + 1);
        }
        if (before != ((RitualSelectionMenu)this.f_97732_).scrollOffset) {
            this.rebuildButtons();
        }
        return true;
    }

    private static void onEntryClicked(RitualSelectionMenu.RitualEntry entry) {
        MBMGNetwork.CHANNEL.sendToServer((Object)new SelectRitualPacket(entry.id));
    }

    private class EntryButton
    extends Button {
        final int index;
        final RitualSelectionMenu.RitualEntry entry;
        final boolean selected;

        EntryButton(int x, int y, int w, int h, int index, RitualSelectionMenu.RitualEntry entry, boolean selected) {
            super(x, y, w, h, (Component)Component.m_237110_((String)entry.displayName, (Object[])new Object[]{entry.id.toString()}), b -> RitualSelectionScreen.onEntryClicked(entry), Button.f_252438_);
            this.index = index;
            this.entry = entry;
            this.selected = selected;
        }

        public void m_87963_(GuiGraphics g, int mx, int my, float partial) {
            boolean hover = this.f_93622_;
            int bg = this.selected ? (hover ? -10847686 : -11900374) : (hover ? -12957094 : -14009782);
            g.m_280509_(this.m_252754_(), this.m_252907_(), this.m_252754_() + this.f_93618_, this.m_252907_() + this.f_93619_, bg);
            g.m_280509_(this.m_252754_(), this.m_252907_(), this.m_252754_() + 1, this.m_252907_() + this.f_93619_, -9799014);
            g.m_280509_(this.m_252754_(), this.m_252907_() + this.f_93619_ - 1, this.m_252754_() + this.f_93618_, this.m_252907_() + this.f_93619_, -15062470);
            if (this.selected) {
                g.m_280509_(this.m_252754_() + this.f_93618_ - 2, this.m_252907_(), this.m_252754_() + this.f_93618_, this.m_252907_() + this.f_93619_, -2838729);
            }
            Component label = this.m_6035_();
            String full = label.getString();
            int maxWidth = this.f_93618_ - 8;
            Object text = full;
            if (RitualSelectionScreen.this.f_96547_.m_92895_(full) > maxWidth) {
                text = RitualSelectionScreen.this.f_96547_.m_92834_(full, maxWidth - RitualSelectionScreen.this.f_96547_.m_92895_("...")) + "...";
            }
            g.m_280430_(RitualSelectionScreen.this.f_96547_, (Component)Component.m_237113_((String)text), this.m_252754_() + 4, this.m_252907_() + (this.f_93619_ - 8) / 2, this.selected ? -2640 : -1);
            if (hover) {
                ArrayList<MutableComponent> tooltip = new ArrayList<MutableComponent>();
                tooltip.add(Component.m_237113_((String)this.entry.id.toString()).m_130944_(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
                if (this.selected) {
                    tooltip.add(Component.m_237115_((String)"mbmg.ritual_select.current_selected").m_130940_(ChatFormatting.GOLD));
                }
                g.m_280666_(RitualSelectionScreen.this.f_96547_, tooltip, mx, my);
            }
        }
    }
}


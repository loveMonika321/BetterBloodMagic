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
        super(menu, inv, title);
        this.imageWidth = 240;
        this.imageHeight = 214;
        this.inventoryLabelY = 114;
    }

    protected void init() {
        super.init();
        this.leftPanelX = this.width / 2 - 120;
        this.topPanelY = this.height / 2 - 107;
        this.leftPos = this.leftPanelX;
        this.topPos = this.topPanelY;
        this.scrollBtnX = this.leftPanelX + 240 - 22;
        this.scrollUpY = this.topPanelY + 26 - 2;
        this.scrollDownY = this.scrollUpY + 180 - 14;
        this.clearBtnX = this.leftPanelX + 8;
        this.clearBtnY = this.topPanelY + 26 + 180 + 4;
        this.rebuildButtons();
    }

    private void rebuildButtons() {
        for (EntryButton b : this.buttons) {
            this.removeWidget(b);
        }
        this.buttons.clear();
        List<RitualSelectionMenu.RitualEntry> list = ((RitualSelectionMenu)this.menu).availableRituals;
        int maxVisible = 10;
        int visibleCount = Math.min(maxVisible, Math.max(0, list.size() - ((RitualSelectionMenu)this.menu).scrollOffset));
        for (int i = 0; i < visibleCount; ++i) {
            int idx = ((RitualSelectionMenu)this.menu).scrollOffset + i;
            RitualSelectionMenu.RitualEntry entry = list.get(idx);
            int y = this.topPanelY + 26 + i * 18;
            boolean selected = ((RitualSelectionMenu)this.menu).currentRitualId != null && ((RitualSelectionMenu)this.menu).currentRitualId.equals(entry.id);
            EntryButton btn = new EntryButton(this.leftPanelX + 8, y, 208, 16, idx, entry, selected);
            this.addRenderableWidget(btn);
            this.buttons.add(btn);
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        boolean clearHover;
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        int x = this.leftPanelX;
        int y = this.topPanelY;
        graphics.fill(x, y, x + 240, y + 214, -15066578);
        graphics.fill(x, y, x + 240, y + 1, -11908502);
        graphics.fill(x, y, x + 1, y + 214, -11908502);
        graphics.fill(x + 240 - 1, y, x + 240, y + 214, -11908502);
        graphics.fill(x, y + 214 - 1, x + 240, y + 214, -11908502);
        String typeStr = ((RitualSelectionMenu)this.menu).divinerType == 1 ? "\u3010\u8584\u66ae\u3011" : "";
        MutableComponent title = Component.translatable((String)"mbmg.ritual_select.title", (Object[])new Object[]{Component.translatable((String)"item.bloodmagic.ritualdiviner").getString() + typeStr});
        graphics.drawString(this.font, (Component)title, x + 8, y + 8, -2838729);
        int total = ((RitualSelectionMenu)this.menu).availableRituals.size();
        int from = Math.min(total, ((RitualSelectionMenu)this.menu).scrollOffset + 1);
        int to = Math.min(total, ((RitualSelectionMenu)this.menu).scrollOffset + 10);
        String range = total == 0 ? "0" : from + "-" + to + " / " + total;
        graphics.drawString(this.font, range, x + 240 - 28 - this.font.width(range), y + 8, -5592406);
        this.renderScrollButton(graphics, this.scrollBtnX, this.scrollUpY, "\u25b2", mouseX, mouseY, ((RitualSelectionMenu)this.menu).scrollOffset > 0);
        this.renderScrollButton(graphics, this.scrollBtnX, this.scrollDownY, "\u25bc", mouseX, mouseY, ((RitualSelectionMenu)this.menu).scrollOffset + 10 < total);
        boolean canClear = ((RitualSelectionMenu)this.menu).currentRitualId != null;
        boolean bl = clearHover = mouseX >= this.clearBtnX && mouseX < this.clearBtnX + 80 && mouseY >= this.clearBtnY && mouseY < this.clearBtnY + 16;
        int clearBg = !canClear ? -12965334 : (clearHover ? -9815494 : -10868182);
        graphics.fill(this.clearBtnX, this.clearBtnY, this.clearBtnX + 80, this.clearBtnY + 16, clearBg);
        graphics.drawCenteredString(this.font, (Component)Component.translatable((String)"mbmg.ritual_select.clear"), this.clearBtnX + 40, this.clearBtnY + 4, canClear ? -1 : -7829368);
    }

    private void renderScrollButton(GuiGraphics g, int x, int y, String label, int mx, int my, boolean enabled) {
        boolean hover;
        boolean bl = hover = mx >= x && mx < x + 16 && my >= y && my < y + 14;
        int color = !enabled ? -14013894 : (hover ? -10855814 : -12961190);
        g.fill(x, y, x + 16, y + 14, color);
        g.drawCenteredString(this.font, (Component)Component.literal((String)label), x + 8, y + 3, enabled ? -1 : -7829368);
    }

    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (mouseX >= (double)this.scrollBtnX && mouseX < (double)(this.scrollBtnX + 16) && mouseY >= (double)this.scrollUpY && mouseY < (double)(this.scrollUpY + 14) && ((RitualSelectionMenu)this.menu).scrollOffset > 0) {
                ((RitualSelectionMenu)this.menu).scrollOffset = Math.max(0, ((RitualSelectionMenu)this.menu).scrollOffset - 1);
                this.rebuildButtons();
                return true;
            }
            if (mouseX >= (double)this.scrollBtnX && mouseX < (double)(this.scrollBtnX + 16) && mouseY >= (double)this.scrollDownY && mouseY < (double)(this.scrollDownY + 14) && ((RitualSelectionMenu)this.menu).scrollOffset + 10 < ((RitualSelectionMenu)this.menu).availableRituals.size()) {
                ++((RitualSelectionMenu)this.menu).scrollOffset;
                this.rebuildButtons();
                return true;
            }
            if (((RitualSelectionMenu)this.menu).currentRitualId != null && mouseX >= (double)this.clearBtnX && mouseX < (double)(this.clearBtnX + 80) && mouseY >= (double)this.clearBtnY && mouseY < (double)(this.clearBtnY + 16)) {
                MBMGNetwork.CHANNEL.sendToServer((Object)new SelectRitualPacket(null));
                this.onClose();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double x, double y, double deltaY) {
        int before = ((RitualSelectionMenu)this.menu).scrollOffset;
        int count = ((RitualSelectionMenu)this.menu).availableRituals.size();
        int max = Math.max(0, count - 10);
        if (deltaY > 0.0) {
            ((RitualSelectionMenu)this.menu).scrollOffset = Math.max(0, ((RitualSelectionMenu)this.menu).scrollOffset - 1);
        } else if (deltaY < 0.0) {
            ((RitualSelectionMenu)this.menu).scrollOffset = Math.min(max, ((RitualSelectionMenu)this.menu).scrollOffset + 1);
        }
        if (before != ((RitualSelectionMenu)this.menu).scrollOffset) {
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
            super(x, y, w, h, (Component)Component.translatable((String)entry.displayName, (Object[])new Object[]{entry.id.toString()}), b -> RitualSelectionScreen.onEntryClicked(entry), Button.DEFAULT_NARRATION);
            this.index = index;
            this.entry = entry;
            this.selected = selected;
        }

        public void renderWidget(GuiGraphics g, int mx, int my, float partial) {
            boolean hover = this.isHovered;
            int bg = this.selected ? (hover ? -10847686 : -11900374) : (hover ? -12957094 : -14009782);
            g.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bg);
            g.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.height, -9799014);
            g.fill(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, -15062470);
            if (this.selected) {
                g.fill(this.getX() + this.width - 2, this.getY(), this.getX() + this.width, this.getY() + this.height, -2838729);
            }
            Component label = this.getMessage();
            String full = label.getString();
            int maxWidth = this.width - 8;
            Object text = full;
            if (RitualSelectionScreen.this.font.width(full) > maxWidth) {
                text = RitualSelectionScreen.this.font.plainSubstrByWidth(full, maxWidth - RitualSelectionScreen.this.font.width("...")) + "...";
            }
            g.drawString(RitualSelectionScreen.this.font, (Component)Component.literal((String)text), this.getX() + 4, this.getY() + (this.height - 8) / 2, this.selected ? -2640 : -1);
            if (hover) {
                ArrayList<Component> tooltip = new ArrayList<Component>();
                tooltip.add(Component.literal(this.entry.id.toString()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                if (this.selected) {
                    tooltip.add(Component.translatable("mbmg.ritual_select.current_selected").withStyle(ChatFormatting.GOLD));
                }
                g.renderComponentTooltip(RitualSelectionScreen.this.font, tooltip, mx, my);
            }
        }
    }
}


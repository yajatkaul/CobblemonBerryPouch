package com.github.flandre923.berrypouch.menu.screen;

import com.github.flandre923.berrypouch.client.Point;
import com.github.flandre923.berrypouch.client.gui.layout.SlotGridLayout;
import com.github.flandre923.berrypouch.client.gui.style.BackgroundGenerator;
import com.github.flandre923.berrypouch.client.gui.style.Blitter;
import com.github.flandre923.berrypouch.client.gui.style.PaletteColor;
import com.github.flandre923.berrypouch.client.gui.style.ScreenStyle;
import com.github.flandre923.berrypouch.client.gui.style.SlotPosition;
import com.github.flandre923.berrypouch.client.gui.style.Text;
import com.github.flandre923.berrypouch.client.gui.style.TextAlignment;
import com.github.flandre923.berrypouch.mixins.SlotAccessor;
import com.github.flandre923.berrypouch.menu.ae.AEBaseMenu;
import com.github.flandre923.berrypouch.menu.ae.SlotSemantic;
import com.github.flandre923.berrypouch.menu.ae.SlotSemantics;
import com.github.flandre923.berrypouch.menu.slot.AppEngSlot;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class AEBaseScreen<T extends AEBaseMenu> extends AbstractContainerScreen<T> {
    private static final Point HIDDEN_SLOT_POS = new Point(-9999, -9999);

    public static final String TEXT_ID_DIALOG_TITLE = "dialog_title";
    public static final String TEXT_ID_PLAYER_INVENTORY = "player_inventory";

    protected final ScreenStyle style;
    private final Map<String, TextOverride> textOverrides = new HashMap<>();
    private final Set<SlotSemantic> hiddenSlots = new HashSet<>();

    protected AEBaseScreen(T menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title);
        this.style = Objects.requireNonNull(style, "style");

        if (style.getGeneratedBackground() != null) {
            this.imageWidth = style.getGeneratedBackground().getWidth();
            this.imageHeight = style.getGeneratedBackground().getHeight();
        } else if (style.getBackground() != null) {
            this.imageWidth = style.getBackground().getSrcWidth();
            this.imageHeight = style.getBackground().getSrcHeight();
        }
    }

    @Override
    protected void init() {
        super.init();
        positionSlots();
    }

    private void positionSlots() {
        for (Map.Entry<String, SlotPosition> entry : style.getSlots().entrySet()) {
            SlotSemantic semantic = SlotSemantics.getOrThrow(entry.getKey());
            if (hiddenSlots.contains(semantic)) {
                continue;
            }
            repositionSlots(semantic);
        }
    }

    private Point getSlotPosition(SlotPosition position, int semanticIndex) {
        Point pos = position.resolve(getBounds(false));
        SlotGridLayout grid = position.getGrid();
        if (grid != null) {
            pos = grid.getPosition(pos.getX(), pos.getY(), semanticIndex);
        }
        return pos;
    }

    public final void repositionSlots(SlotSemantic semantic) {
        SlotPosition position = style.getSlots().get(semantic.id());
        if (position == null) {
            return;
        }

        if (position.isHidden()) {
            menu.hideSlot(semantic.id());
            setSlotsHidden(semantic, true);
            return;
        }

        List<Slot> slots = menu.getSlots(semantic);
        for (int i = 0; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            Point pos = getSlotPosition(position, i);
            setSlotPos(slot, pos.getX(), pos.getY());
        }
    }

    private static void setSlotPos(Slot slot, int x, int y) {
        SlotAccessor accessor = (SlotAccessor) slot;
        accessor.berrypouch$setX(x);
        accessor.berrypouch$setY(y);
    }

    protected Rect2i getBounds(boolean absolute) {
        if (absolute) {
            return new Rect2i(leftPos, topPos, imageWidth, imageHeight);
        }
        return new Rect2i(0, 0, imageWidth, imageHeight);
    }

    protected void updateBeforeRender() {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.updateBeforeRender();
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltips(guiGraphics, mouseX, mouseY);
    }

    protected void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected final void renderLabels(GuiGraphics guiGraphics, int x, int y) {
        // renderLabels() is called with GUI-local coordinates (already translated by left/top)
        int ox = 0;
        int oy = 0;

        drawFG(guiGraphics, ox, oy, x, y);

        for (Map.Entry<String, Text> entry : style.getText().entrySet()) {
            TextOverride override = textOverrides.get(entry.getKey());
            drawText(guiGraphics, entry.getValue(), override);
        }
    }

    private void drawText(GuiGraphics guiGraphics, Text text, @Nullable TextOverride override) {
        if (text.getPosition() == null) {
            return;
        }
        if (override != null && override.hidden) {
            return;
        }

        int color = style.getColor(text.getColor()).toARGB();
        Point pos = text.getPosition().resolve(getBounds(false));
        float scale = text.getScale();

        Component content = text.getText();
        if (override != null && override.content != null) {
            content = override.content.copy().withStyle(content.getStyle());
        }

        List<FormattedCharSequence> lines;
        if (text.getMaxWidth() <= 0) {
            lines = List.of(content.getVisualOrderText());
        } else {
            lines = ComponentRenderUtils.wrapComponents(content, text.getMaxWidth(), font);
        }

        int y = pos.getY();
        for (FormattedCharSequence line : lines) {
            int lineWidth = this.font.width(line);
            int x = pos.getX();
            if (text.getAlign() == TextAlignment.CENTER) {
                x -= Math.round(lineWidth * scale) / 2;
            } else if (text.getAlign() == TextAlignment.RIGHT) {
                x -= Math.round(lineWidth * scale);
            }

            if (scale == 1f) {
                guiGraphics.drawString(font, line, x, y, color, false);
            } else {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(x, y, 1);
                guiGraphics.pose().scale(scale, scale, 1);
                guiGraphics.drawString(font, line, 0, 0, color, false);
                guiGraphics.pose().popPose();
            }
            y += Math.round(scale * this.font.lineHeight);
        }
    }

    @Override
    protected final void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        drawBG(guiGraphics, leftPos, topPos, mouseX, mouseY, partialTick);
    }

    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTick) {
        if (style.getGeneratedBackground() != null) {
            BackgroundGenerator.draw(style.getGeneratedBackground().getWidth(), style.getGeneratedBackground().getHeight(),
                    guiGraphics, offsetX, offsetY);
        }

        Blitter background = style.getBackground();
        if (background != null) {
            background.dest(offsetX, offsetY).blit(guiGraphics);
        }
    }

    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
    }

    protected final void renderSlotFrames(GuiGraphics guiGraphics, Blitter slotFrame) {
        PoseStack pose = guiGraphics.pose();
        for (Slot slot : this.menu.slots) {
            if (!slot.isActive()) {
                continue;
            }
            if (slot.x <= HIDDEN_SLOT_POS.getX() + 100 || slot.y <= HIDDEN_SLOT_POS.getY() + 100) {
                continue;
            }
            pose.pushPose();
            pose.translate(0, 0, 1);
            slotFrame.copy().dest(this.leftPos + slot.x - 1, this.topPos + slot.y - 1).blit(guiGraphics);
            pose.popPose();
        }
    }

    @Override
    public void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (slot instanceof AppEngSlot appEngSlot) {
            renderAppEngSlot(guiGraphics, appEngSlot);
            return;
        }
        super.renderSlot(guiGraphics, slot);
    }

    private void renderAppEngSlot(GuiGraphics guiGraphics, AppEngSlot slot) {
        if (!slot.isValid()) {
            guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x66FF6666);
        }
        super.renderSlot(guiGraphics, slot);
    }

    protected final void setTextHidden(String id, boolean hidden) {
        getOrCreateTextOverride(id).hidden = hidden;
    }

    protected final void setTextContent(String id, Component content) {
        getOrCreateTextOverride(id).content = content;
    }

    public final void setSlotsHidden(SlotSemantic semantic, boolean hidden) {
        if (hidden) {
            if (hiddenSlots.add(semantic)) {
                for (Slot slot : menu.getSlots(semantic)) {
                    setSlotPos(slot, HIDDEN_SLOT_POS.getX(), HIDDEN_SLOT_POS.getY());
                }
            }
        } else if (hiddenSlots.remove(semantic)) {
            positionSlots();
        }
    }

    private TextOverride getOrCreateTextOverride(String id) {
        return textOverrides.computeIfAbsent(id, ignored -> new TextOverride());
    }

    public ScreenStyle getStyle() {
        return style;
    }

    public final int getGuiLeft() {
        return this.leftPos;
    }

    public final int getGuiTop() {
        return this.topPos;
    }

    public final Slot getSlotUnderMouse() {
        return this.hoveredSlot;
    }

    protected final void renderCompactCount(GuiGraphics guiGraphics, int x, int y, long count) {
        String text = formatCount(count);
        float scale = getScaleForCount(text);

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(0, 0, 350);

        int textWidth = this.font.width(text);
        float tx = x + 18 - textWidth * scale - 1;
        float ty = y + 18 - 8 * scale - 1;
        pose.translate(tx, ty, 0);
        pose.scale(scale, scale, 1.0f);
        guiGraphics.drawString(this.font, text, 0, 0, 0xFFFFFF, true);
        pose.popPose();
    }

    protected String formatCount(long count) {
        if (count < 1_000L) {
            return String.valueOf(count);
        }
        if (count < 1_000_000L) {
            double k = count / 1_000.0;
            return String.format(k >= 100 ? "%.0fK" : "%.1fK", k).replace(".0K", "K");
        }
        if (count < 1_000_000_000L) {
            double m = count / 1_000_000.0;
            return String.format(m >= 100 ? "%.0fM" : "%.1fM", m).replace(".0M", "M");
        }
        if (count < 1_000_000_000_000L) {
            double b = count / 1_000_000_000.0;
            return String.format(b >= 100 ? "%.0fB" : "%.1fB", b).replace(".0B", "B");
        }
        return String.valueOf(count);
    }

    protected float getScaleForCount(String text) {
        int length = text.length();
        if (length <= 2) {
            return 1.0f;
        }
        if (length == 3) {
            return 0.85f;
        }
        if (length == 4) {
            return 0.7f;
        }
        return 0.6f;
    }

    private static final class TextOverride {
        @Nullable
        private Component content;
        private boolean hidden;
    }
}

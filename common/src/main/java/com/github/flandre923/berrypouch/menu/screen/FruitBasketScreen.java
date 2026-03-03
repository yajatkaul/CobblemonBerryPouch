package com.github.flandre923.berrypouch.menu.screen;

import com.github.flandre923.berrypouch.menu.container.FruitBasketContainer;
import com.github.flandre923.berrypouch.menu.layout.FruitBasketLayout;
import com.github.flandre923.berrypouch.ui.declarative.DeclarativeUiRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FruitBasketScreen extends AbstractContainerScreen<FruitBasketContainer> {
    public FruitBasketScreen(FruitBasketContainer menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = FruitBasketLayout.WIDTH;
        this.imageHeight = FruitBasketLayout.HEIGHT;
        this.titleLabelX = 10;
        this.titleLabelY = 6;
        this.inventoryLabelX = 16;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        DeclarativeUiRenderer.renderLayers(guiGraphics, FruitBasketLayout.STORAGE, leftPos, topPos);
        DeclarativeUiRenderer.renderSlotFrames(guiGraphics, FruitBasketLayout.STORAGE, leftPos, topPos);
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (slot.container == menu.getBasketDisplay() && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            guiGraphics.renderItem(stack, slot.x, slot.y);
            guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, slot.x, slot.y, "");
            long count = menu.getDisplayCount(slot.getContainerSlot());
            if (count > 0L) {
                renderCount(guiGraphics, slot.x, slot.y, count);
            }
            return;
        }
        super.renderSlot(guiGraphics, slot);
    }

    private void renderCount(GuiGraphics guiGraphics, int x, int y, long count) {
        String text = formatCount(count);
        float scale = text.length() <= 2 ? 1.0f : text.length() == 3 ? 0.85f : text.length() == 4 ? 0.7f : 0.6f;

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

    private static String formatCount(long count) {
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
}

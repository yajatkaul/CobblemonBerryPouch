package com.github.flandre923.berrypouch.menu.screen;

import com.github.flandre923.berrypouch.client.gui.style.StyleManager;
import com.github.flandre923.berrypouch.helper.RenderHelper;
import com.github.flandre923.berrypouch.menu.container.ApricornBasketContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ApricornBasketScreen extends AEBaseScreen<ApricornBasketContainer> {
    public ApricornBasketScreen(ApricornBasketContainer menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, StyleManager.loadStyleDoc("/screens/apricorn_basket.json"));
        setTextContent(TEXT_ID_DIALOG_TITLE, title);
        setTextContent(TEXT_ID_PLAYER_INVENTORY, this.playerInventoryTitle);
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTick) {
        super.drawBG(guiGraphics, offsetX, offsetY, mouseX, mouseY, partialTick);
        try {
            renderSlotFrames(guiGraphics, style.getImage("slot_frame"));
        } catch (IllegalStateException ignored) {
        }
        renderFixedSlotPlaceholders(guiGraphics, offsetX, offsetY);
    }

    private void renderFixedSlotPlaceholders(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        for (Slot slot : menu.slots) {
            if (slot.container == menu.getBasketDisplay() && !slot.hasItem()) {
                ItemStack preview = menu.getFixedSlotPreview(slot.getContainerSlot());
                if (!preview.isEmpty()) {
                    RenderHelper.renderGuiItemAlpha(
                            preview,
                            offsetX + slot.x,
                            offsetY + slot.y,
                            0x5F,
                            Minecraft.getInstance().getItemRenderer());
                }
            }
        }
    }

    @Override
    public void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (slot.container == menu.getBasketDisplay()) {
            if (slot.hasItem()) {
                ItemStack stack = slot.getItem();
                guiGraphics.renderItem(stack, slot.x, slot.y);
                guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, slot.x, slot.y, "");
                long count = menu.getDisplayCount(slot.getContainerSlot());
                if (count > 0L) {
                    renderCompactCount(guiGraphics, slot.x, slot.y, count);
                }
            }
            return;
        }
        super.renderSlot(guiGraphics, slot);
    }
}

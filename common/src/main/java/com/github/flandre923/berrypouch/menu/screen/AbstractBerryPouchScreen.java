package com.github.flandre923.berrypouch.menu.screen;

import com.github.flandre923.berrypouch.ModCommon;
import com.github.flandre923.berrypouch.client.gui.style.ScreenStyle;
import com.github.flandre923.berrypouch.helper.MarkedSlotsHelper;
import com.github.flandre923.berrypouch.helper.RenderHelper;
import com.github.flandre923.berrypouch.item.BerryPouch;
import com.github.flandre923.berrypouch.item.pouch.BerryPouchType;
import com.github.flandre923.berrypouch.menu.container.AbstractBerryPouchContainer;
import com.github.flandre923.berrypouch.network.PacketInvoker;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public abstract class AbstractBerryPouchScreen<T extends AbstractBerryPouchContainer>
        extends AEBaseScreen<T> {

    private static final ResourceLocation STAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "textures/gui/star.png");
    private static final int STAR_SIZE = 20;

    protected final BerryPouchType pouchType;
    protected final Minecraft minecraft;

    public AbstractBerryPouchScreen(
            T menu,
            Inventory playerInv,
            Component title,
            BerryPouchType pouchType,
            ScreenStyle style
    ) {
        super(menu, playerInv, title, style);
        this.pouchType = pouchType;
        this.minecraft = Minecraft.getInstance();

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

        renderSlotPlaceholders(guiGraphics, offsetX, offsetY);
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        renderMarkedSlotIndicators(guiGraphics, offsetX, offsetY);
    }

    private void renderSlotPlaceholders(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        for (Slot slot : menu.slots) {
            if (slot.container == menu.getPouchInventory() && !slot.hasItem()) {
                ItemStack placeholder = getPlaceholderForSlot(slot.getContainerSlot());
                if (!placeholder.isEmpty()) {
                    RenderHelper.renderGuiItemAlpha(
                            placeholder,
                            offsetX + slot.x,
                            offsetY + slot.y,
                            0x5F,
                            minecraft.getItemRenderer());
                }
            }
        }
    }

    @Override
    public void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (slot.container == menu.getPouchInventory() && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            int x = slot.x;
            int y = slot.y;

            guiGraphics.renderItem(stack, x, y);
            guiGraphics.renderItemDecorations(minecraft.font, stack, x, y, "");

            int count = stack.getCount();
            if (count > 0) {
                renderCompactCount(guiGraphics, x, y, count);
            }
            return;
        }

        super.renderSlot(guiGraphics, slot);
    }

    private void renderMarkedSlotIndicators(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        ItemStack currentPouchStack = findCurrentPouchStack();
        if (currentPouchStack.isEmpty()) {
            return;
        }

        PoseStack poseStack = guiGraphics.pose();

        for (Slot slot : menu.slots) {
            if (slot.container == menu.getPouchInventory()) {
                int pouchSlotIndex = slot.getContainerSlot();

                if (MarkedSlotsHelper.isSlotMarked(currentPouchStack, pouchSlotIndex)) {
                    poseStack.pushPose();
                    poseStack.translate(0, 0, 299);
                    int starX = offsetX + slot.x;
                    int starY = offsetY + slot.y;
                    guiGraphics.blit(STAR_TEXTURE, starX, starY, 16, 16, 0, 0, STAR_SIZE, STAR_SIZE, STAR_SIZE, STAR_SIZE);
                    poseStack.popPose();
                }
            }
        }
    }

    private ItemStack findCurrentPouchStack() {
        Player player = this.minecraft.player;
        if (player == null) {
            return ItemStack.EMPTY;
        }

        Item targetItem = this.menu.getPouchStack().getItem();
        if (!(targetItem instanceof BerryPouch)) {
            return ItemStack.EMPTY;
        }

        int openFlag = this.menu.getOpenFlag();

        switch (openFlag) {
            case 0 -> {
                ItemStack mainHandStack = player.getMainHandItem();
                if (mainHandStack.is(targetItem) && !mainHandStack.isEmpty()) {
                    return mainHandStack;
                }

                AccessoriesCapability capability = AccessoriesCapability.get(player);
                if (capability != null) {
                    SlotEntryReference equippedRef = capability.getFirstEquipped(targetItem);
                    if (equippedRef != null && !equippedRef.stack().isEmpty()) {
                        return equippedRef.stack();
                    }
                }

                ItemStack offHandStack = player.getOffhandItem();
                if (offHandStack.is(targetItem) && !offHandStack.isEmpty()) {
                    return offHandStack;
                }
            }
            case 1 -> {
                ItemStack offHandStack = player.getOffhandItem();
                if (offHandStack.is(targetItem) && !offHandStack.isEmpty()) {
                    return offHandStack;
                }

                ItemStack mainHandStack = player.getMainHandItem();
                if (mainHandStack.is(targetItem) && !mainHandStack.isEmpty()) {
                    return mainHandStack;
                }

                AccessoriesCapability capability = AccessoriesCapability.get(player);
                if (capability != null) {
                    SlotEntryReference equippedRef = capability.getFirstEquipped(targetItem);
                    if (equippedRef != null && !equippedRef.stack().isEmpty()) {
                        return equippedRef.stack();
                    }
                }
            }
            case 2 -> {
                AccessoriesCapability capability = AccessoriesCapability.get(player);
                if (capability != null) {
                    SlotEntryReference equippedRef = capability.getFirstEquipped(targetItem);
                    if (equippedRef != null && !equippedRef.stack().isEmpty()) {
                        return equippedRef.stack();
                    }
                }

                ItemStack mainHandStack = player.getMainHandItem();
                if (mainHandStack.is(targetItem) && !mainHandStack.isEmpty()) {
                    return mainHandStack;
                }

                ItemStack offHandStack = player.getOffhandItem();
                if (offHandStack.is(targetItem) && !offHandStack.isEmpty()) {
                    return offHandStack;
                }
            }
            default -> {
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_S)) {
            Slot clickedSlot = this.hoveredSlot;
            if (clickedSlot != null && clickedSlot.container == menu.getPouchInventory()) {
                int pouchSlotIndex = clickedSlot.getContainerSlot();
                PacketInvoker.sendToggleMarkSlot(pouchSlotIndex);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected abstract ItemStack getPlaceholderForSlot(int slotIndex);

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_9) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        super.onClose();
        if (minecraft.player != null) {
            minecraft.player.playSound(
                    SoundEvents.BUNDLE_DROP_CONTENTS,
                    0.5F,
                    minecraft.player.level().random.nextFloat() * 0.1F + 0.9F);
        }
    }
}
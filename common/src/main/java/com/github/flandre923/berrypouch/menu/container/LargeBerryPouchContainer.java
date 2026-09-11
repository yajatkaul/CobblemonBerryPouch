package com.github.flandre923.berrypouch.menu.container;

import com.github.flandre923.berrypouch.ModRegistries;
import com.github.flandre923.berrypouch.helper.PouchItemHelper;
import com.github.flandre923.berrypouch.item.pouch.BerryPouchType;
import com.github.flandre923.berrypouch.menu.ae.SlotSemantics;
import com.github.flandre923.berrypouch.menu.slot.BerryPouchSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class LargeBerryPouchContainer extends AbstractBerryPouchContainer {
    private static final int NATURAL_BERRY_SLOTS = 30;
    private static final int MIXED_BERRY_SLOTS = 40;
    private static final int OTHER_BAIT_START = NATURAL_BERRY_SLOTS + MIXED_BERRY_SLOTS;
    private static final int OTHER_BAIT_SLOTS = 16;

    public LargeBerryPouchContainer(int windowId, Inventory playerInv, ItemStack pouchStack, int openFlag) {
        super(ModRegistries.ModMenuTypes.BERRY_POUCH_CONTAINER_69.get(),
                windowId, playerInv, pouchStack, BerryPouchType.LARGE, openFlag);
    }

    public static boolean isBerry(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.is(net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("cobblemon", "berries")
        ));
    }

    public static boolean isOtherBaits(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.is(net.minecraft.tags.TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("berrypouch", "other_baits")
        ));
    }

    public static LargeBerryPouchContainer fromNetwork(int windowId, Inventory inv, FriendlyByteBuf buf) {
        int handFlag = buf.readInt();
        ItemStack item;
        if (handFlag == 0) {
            item = inv.player.getItemInHand(InteractionHand.MAIN_HAND);
        } else if (handFlag == 1) {
            item = inv.player.getItemInHand(InteractionHand.OFF_HAND);
        } else {
            item = PouchItemHelper.findBerryPouch(inv.player);
        }
        return new LargeBerryPouchContainer(windowId, inv, item, handFlag);
    }

    @Override
    protected void addPouchSlots() {
        for (int slotIndex = 0; slotIndex < NATURAL_BERRY_SLOTS; slotIndex++) {
            addBerrySlot(slotIndex, SlotSemantics.POUCH_NATURAL_BERRY);
        }

        for (int slotIndex = NATURAL_BERRY_SLOTS; slotIndex < OTHER_BAIT_START; slotIndex++) {
            addBerrySlot(slotIndex, SlotSemantics.POUCH_MIXED_BERRY);
        }

        for (int slotIndex = OTHER_BAIT_START; slotIndex < OTHER_BAIT_START + OTHER_BAIT_SLOTS; slotIndex++) {
            addOtherBaitsSlot(slotIndex);
        }
    }

    private void addBerrySlot(int slotIndex, com.github.flandre923.berrypouch.menu.ae.SlotSemantic semantic) {
        addSlot(new BerryPouchSlot(pouchInventory, slotIndex, 0, 0) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isBerry(stack) && BerryPouchType.LARGE.getStorageSlot()
                        .matchesSlotItem(slotIndex + 1, stack.getItem());
            }
        }, semantic);
    }

    private void addOtherBaitsSlot(int slotIndex) {
        addSlot(new BerryPouchSlot(pouchInventory, slotIndex, 0, 0) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isOtherBaits(stack);
            }
        }, SlotSemantics.POUCH_OTHER_BAIT);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot clickedSlot = this.slots.get(slotIndex);
        if (clickedSlot == null || !clickedSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack originalStack = clickedSlot.getItem();
        ItemStack returnStack = originalStack.copy();

        if (slotIndex < BerryPouchType.LARGE.getSize()) {
            int maxMove = Math.min(64, originalStack.getItem().getDefaultMaxStackSize());
            int currentCount = originalStack.getCount();
            int toMove = Math.min(currentCount, maxMove);

            ItemStack toTransfer = originalStack.copyWithCount(toMove);
            boolean moved = this.moveItemStackTo(toTransfer, BerryPouchType.LARGE.getSize(), this.slots.size(), true);

            if (moved) {
                int actuallyMoved = toMove - toTransfer.getCount();
                if (actuallyMoved > 0) {
                    pouchInventory.removeFromSlot(slotIndex, actuallyMoved);
                    return ItemStack.EMPTY;
                }
            }
            return ItemStack.EMPTY;
        } else {
            boolean moved = false;

            if (BerryPouchType.LARGE.getStorageSlot().has(originalStack.getItem())) {
                moved = this.moveItemStackToPouch(originalStack, 0, OTHER_BAIT_START);
            }

            if (!moved && originalStack.is(net.minecraft.tags.TagKey.create(
                    net.minecraft.core.registries.Registries.ITEM,
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("berrypouch", "other_baits")
            ))) {
                moved = this.moveItemStackToPouch(originalStack, OTHER_BAIT_START, BerryPouchType.LARGE.getSize());
            }

            if (!moved) {
                return ItemStack.EMPTY;
            }

            clickedSlot.set(originalStack);
        }

        return returnStack;
    }

    private boolean moveItemStackToPouch(ItemStack stack, int startSlot, int endSlot) {
        boolean moved = false;

        for (int i = startSlot; i < endSlot && !stack.isEmpty(); i++) {
            Slot slot = this.slots.get(i);
            ItemStack existing = slot.getItem();

            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack)) {
                int toAdd = stack.getCount();
                this.pouchInventory.addToSlot(i, toAdd);
                stack.setCount(0);
                moved = true;
            }
        }

        for (int i = startSlot; i < endSlot && !stack.isEmpty(); i++) {
            Slot slot = this.slots.get(i);
            if (!slot.hasItem() && slot.mayPlace(stack)) {
                this.pouchInventory.setItem(i, stack.copy());
                stack.setCount(0);
                moved = true;
            }
        }

        return moved;
    }
}
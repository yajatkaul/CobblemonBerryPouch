package com.github.flandre923.berrypouch.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FakeSlot extends AppEngSlot {
    public FakeSlot(Container inventory, int invSlot, int x, int y) {
        super(inventory, invSlot, x, y);
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
    }

    @Override
    public ItemStack remove(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    public boolean canSetFilterTo(ItemStack stack) {
        int containerSlot = this.getContainerSlot();
        return containerSlot < this.container.getContainerSize() && this.container.canPlaceItem(containerSlot, stack);
    }

    @Override
    public void set(ItemStack stack) {
        if (!canSetFilterTo(stack)) {
            return;
        }
        if (!stack.isEmpty()) {
            stack = stack.copy();
        }
        super.set(stack);
    }

    public void increase(ItemStack stack) {
        set(stack);
    }

    public void decrease(ItemStack stack) {
        ItemStack current = getItem();
        if (stack.isEmpty()) {
            current = current.copy();
            current.shrink(1);
            set(current);
        } else if (ItemStack.isSameItemSameComponents(current, stack)) {
            current = current.copy();
            current.grow(1);
            set(current);
        } else {
            stack = stack.copy();
            stack.setCount(1);
            set(stack);
        }
    }
}

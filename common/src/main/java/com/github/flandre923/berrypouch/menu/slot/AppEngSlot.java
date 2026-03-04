package com.github.flandre923.berrypouch.menu.slot;

import com.github.flandre923.berrypouch.menu.ae.AEBaseMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AppEngSlot extends Slot {
    private final int invSlot;

    private boolean hideAmount;
    private boolean slotEnabled = true;
    private boolean draggable = true;
    private boolean active = true;
    private Boolean validState;

    private AEBaseMenu menu;

    public AppEngSlot(Container container, int invSlot, int x, int y) {
        super(container, invSlot, x, y);
        this.invSlot = invSlot;
    }

    public Slot setNotDraggable() {
        this.draggable = false;
        return this;
    }

    public void clearStack() {
        this.container.setItem(invSlot, ItemStack.EMPTY);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isSlotEnabled() && super.mayPlace(stack);
    }

    @Override
    public ItemStack getItem() {
        if (!isSlotEnabled()) {
            return ItemStack.EMPTY;
        }
        return super.getItem();
    }

    @Override
    public void set(ItemStack stack) {
        if (isSlotEnabled()) {
            super.set(stack);
        }
    }

    public void initialize(ItemStack stack) {
        this.container.setItem(invSlot, stack);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.validState = null;
        if (menu != null) {
            menu.onSlotChange(this);
        }
    }

    @Override
    public boolean mayPickup(Player player) {
        return isSlotEnabled() && super.mayPickup(player);
    }

    @Override
    public ItemStack remove(int amount) {
        if (!isSlotEnabled()) {
            return ItemStack.EMPTY;
        }
        return super.remove(amount);
    }

    @Override
    public boolean isActive() {
        return isSlotEnabled() && active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSlotEnabled() {
        return slotEnabled;
    }

    public void setSlotEnabled(boolean slotEnabled) {
        this.slotEnabled = slotEnabled;
    }

    public ItemStack getDisplayStack() {
        ItemStack stack = getItem();
        if (hideAmount && !stack.isEmpty()) {
            stack = stack.copy();
            stack.setCount(1);
        }
        return stack;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public AEBaseMenu getMenu() {
        return menu;
    }

    public void setMenu(AEBaseMenu menu) {
        this.menu = menu;
    }

    public final boolean isValid() {
        if (validState == null) {
            validState = getCurrentValidationState();
        }
        return validState;
    }

    protected boolean getCurrentValidationState() {
        return true;
    }

    public void resetCachedValidation() {
        this.validState = null;
    }

    public boolean isHideAmount() {
        return hideAmount;
    }

    public void setHideAmount(boolean hideAmount) {
        this.hideAmount = hideAmount;
    }
}

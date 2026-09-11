package com.github.flandre923.berrypouch.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DisabledSlot extends Slot {
    public DisabledSlot(Container inventory, int invSlot) {
        super(inventory, invSlot, 0, 0);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }
}

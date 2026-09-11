package com.github.flandre923.berrypouch.menu.screen;

import com.github.flandre923.berrypouch.client.gui.style.StyleManager;
import com.github.flandre923.berrypouch.item.pouch.BerryPouchType;
import com.github.flandre923.berrypouch.menu.container.LargeBerryPouchContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class LargeBerryPouchScreen extends AbstractBerryPouchScreen<LargeBerryPouchContainer> {
    public LargeBerryPouchScreen(LargeBerryPouchContainer menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, BerryPouchType.LARGE, StyleManager.loadStyleDoc("/screens/berry_pouch_large.json"));
    }

    @Override
    protected ItemStack getPlaceholderForSlot(int slotIndex) {
        if (slotIndex < 70) {
            return new ItemStack(BerryPouchType.LARGE.getStorageSlot().getSlotItem(slotIndex + 1));
        }
        return ItemStack.EMPTY;
    }
}
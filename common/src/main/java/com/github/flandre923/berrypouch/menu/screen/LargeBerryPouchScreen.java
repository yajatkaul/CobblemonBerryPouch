package com.github.flandre923.berrypouch.menu.screen;

import com.github.flandre923.berrypouch.ModCommon;
import com.github.flandre923.berrypouch.item.pouch.BerryPouchType;
import com.github.flandre923.berrypouch.menu.container.LargeBerryPouchContainer;
import com.github.flandre923.berrypouch.menu.layout.LargeBerryPouchLayout;
import com.github.flandre923.berrypouch.ui.declarative.DeclarativeStorageLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class LargeBerryPouchScreen extends AbstractBerryPouchScreen<LargeBerryPouchContainer> {
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "textures/gui/berry_bag.png");

    public LargeBerryPouchScreen(LargeBerryPouchContainer menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, BerryPouchType.LARGE, TEXTURE);
    }

    @Override
    protected DeclarativeStorageLayout getLayout() {
        return LargeBerryPouchLayout.STORAGE;
    }

    @Override
    protected ItemStack getPlaceholderForSlot(int slotIndex) {
        if (slotIndex < 70) {
            // 树果槽位 (0-69)
            return new ItemStack(BerryPouchType.LARGE.getStorageSlot().getSlotItem(slotIndex + 1));
        } else {
            // Other baits槽位 (70-85) - 不显示占位符，因为这些槽位通过标签检查
            return ItemStack.EMPTY;
        }
    }
}

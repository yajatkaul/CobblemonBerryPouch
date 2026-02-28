package com.github.flandre923.berrypouch.menu.layout;

import com.github.flandre923.berrypouch.ModCommon;
import com.github.flandre923.berrypouch.ui.declarative.DeclarativeStorageLayout;
import net.minecraft.resources.ResourceLocation;

public final class FruitBasketLayout {
    private FruitBasketLayout() {
    }

    public static final int WIDTH = 190;
    public static final int HEIGHT = 185;

    private static final ResourceLocation BACKGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "textures/gui/background.png");
    private static final ResourceLocation SLOT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "textures/gui/slot.png");

    public static final DeclarativeStorageLayout STORAGE = DeclarativeStorageLayout.builder(WIDTH, HEIGHT)
            .imageLayer(BACKGROUND_TEXTURE, 0, 0, 0, 0, WIDTH, HEIGHT, 256, 256, 0)
            .slotVisual("basket_slots", SLOT_TEXTURE, 0, 0, 18, 18, 18, 18, -1, -1, 1)
            .slotVisual("player_inventory", SLOT_TEXTURE, 0, 0, 18, 18, 18, 18, -1, -1, 1)
            .slotVisual("player_hotbar", SLOT_TEXTURE, 0, 0, 18, 18, 18, 18, -1, -1, 1)
            .grid("basket_slots", DeclarativeStorageLayout.SlotRole.POUCH_BERRY, 0, 16, 18, 9, 4, 18, 18)
            .grid("player_inventory", DeclarativeStorageLayout.SlotRole.PLAYER_INVENTORY, 9, 16, 104, 9, 3, 18, 18)
            .grid("player_hotbar", DeclarativeStorageLayout.SlotRole.PLAYER_HOTBAR, 0, 16, 162, 9, 1, 18, 18)
            .build();
}

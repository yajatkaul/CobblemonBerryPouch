package com.github.flandre923.berrypouch.menu.layout;

import com.github.flandre923.berrypouch.ModCommon;
import com.github.flandre923.berrypouch.ui.declarative.DeclarativeStorageLayout;
import net.minecraft.resources.ResourceLocation;

public final class PokeBallGunLayout {
    private PokeBallGunLayout() {
    }

    public static final int WIDTH = 175;
    public static final int HEIGHT = 128;

    public static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "textures/gui/background.png");
    public static final ResourceLocation SLOT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "textures/gui/slot.png");

    public static final DeclarativeStorageLayout STORAGE = DeclarativeStorageLayout.builder(WIDTH, HEIGHT)
            .imageLayer(GUI_TEXTURE, 0, 0, 0, 0, WIDTH, HEIGHT, 223, 129, 0)
            .slotVisual("gun_slots", SLOT_TEXTURE, 0, 0, 18, 18, 18, 18, -1, -1, 1)
            .slotVisual("player_inventory", SLOT_TEXTURE, 0, 0, 18, 18, 18, 18, -1, -1, 1)
            .slotVisual("player_hotbar", SLOT_TEXTURE, 0, 0, 18, 18, 18, 18, -1, -1, 1)
            .grid("gun_slots", DeclarativeStorageLayout.SlotRole.GUN_AMMO, 0, 8, 16, 9, 1, 18, 18)
            .grid("player_inventory", DeclarativeStorageLayout.SlotRole.PLAYER_INVENTORY, 9, 8, 48, 9, 3, 18, 18)
            .grid("player_hotbar", DeclarativeStorageLayout.SlotRole.PLAYER_HOTBAR, 0, 8, 106, 9, 1, 18, 18)
            .build();
}

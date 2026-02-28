package com.github.flandre923.berrypouch.menu.layout;

import com.github.flandre923.berrypouch.ModCommon;
import com.github.flandre923.berrypouch.item.pouch.BerryPouchType;
import com.github.flandre923.berrypouch.ui.declarative.DeclarativeStorageLayout;
import net.minecraft.resources.ResourceLocation;

public final class LargeBerryPouchLayout {
    private LargeBerryPouchLayout() {
    }

    private static final ResourceLocation BACKGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "textures/gui/berry_bag.png");
    private static final ResourceLocation SLOT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "textures/gui/slot.png");

    public static final DeclarativeStorageLayout STORAGE = DeclarativeStorageLayout.builder(
                    BerryPouchType.LARGE.getGuiWidth(),
                    BerryPouchType.LARGE.getGuiHeight())
            .imageLayer(BACKGROUND_TEXTURE, 0, 0, 0, 0, 255, 257, 256, 300, 0)
            .slotVisual("natural_berries", SLOT_TEXTURE, 0, 0, 18, 18, 18, 18, -1, -1, 1)
            .slotVisual("mixed_berries", SLOT_TEXTURE, 0, 0, 18, 18, 18, 18, -1, -1, 1)
            .slotVisual("other_baits", SLOT_TEXTURE, 0, 0, 18, 18, 18, 18, -1, -1, 1)
            .slotVisual("player_inventory", SLOT_TEXTURE, 0, 0, 18, 18, 18, 18, -1, -1, 1)
            .slotVisual("player_hotbar", SLOT_TEXTURE, 0, 0, 18, 18, 18, 18, -1, -1, 1)
            .grid("natural_berries", DeclarativeStorageLayout.SlotRole.POUCH_BERRY, 0, 12, 17, 10, 3, 18, 18)
            .grid("mixed_berries", DeclarativeStorageLayout.SlotRole.POUCH_BERRY, 30, 12, 89, 10, 4, 18, 18)
            .grid("other_baits", DeclarativeStorageLayout.SlotRole.POUCH_OTHER_BAIT, 70, 210, 17, 2, 8, 18, 18)
            .grid("player_inventory", DeclarativeStorageLayout.SlotRole.PLAYER_INVENTORY, 9, 48, 176, 9, 3, 18, 18)
            .grid("player_hotbar", DeclarativeStorageLayout.SlotRole.PLAYER_HOTBAR, 0, 48, 234, 9, 1, 18, 18)
            .build();
}

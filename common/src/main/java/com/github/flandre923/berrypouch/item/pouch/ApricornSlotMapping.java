package com.github.flandre923.berrypouch.item.pouch;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Locale;

public final class ApricornSlotMapping {
    private static final List<String> ORDERED_COLORS = List.of(
            "black",
            "blue",
            "green",
            "pink",
            "red",
            "white",
            "yellow"
    );

    private static final List<ResourceLocation> ORDERED_IDS = ORDERED_COLORS.stream()
            .map(color -> ResourceLocation.fromNamespaceAndPath("cobblemon", color + "_apricorn"))
            .toList();

    private ApricornSlotMapping() {
    }

    public static int size() {
        return ORDERED_IDS.size();
    }

    public static boolean isValidSlot(int slot) {
        return slot >= 0 && slot < ORDERED_IDS.size();
    }

    public static ResourceLocation getExpectedItemId(int slot) {
        if (!isValidSlot(slot)) {
            return null;
        }
        return ORDERED_IDS.get(slot);
    }

    public static Item getExpectedItem(int slot) {
        ResourceLocation id = getExpectedItemId(slot);
        if (id == null) {
            return null;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        return item == null || item == net.minecraft.world.item.Items.AIR ? null : item;
    }

    public static int getSlotIndex(Item item) {
        return getSlotIndex(BuiltInRegistries.ITEM.getKey(item));
    }

    public static int getSlotIndex(ResourceLocation id) {
        if (id == null) {
            return -1;
        }

        int exact = ORDERED_IDS.indexOf(id);
        if (exact >= 0) {
            return exact;
        }

        if (!"cobblemon".equals(id.getNamespace())) {
            return -1;
        }

        String path = id.getPath().toLowerCase(Locale.ROOT);
        if (!path.contains("apricorn")) {
            return -1;
        }

        for (int i = 0; i < ORDERED_COLORS.size(); i++) {
            if (path.contains(ORDERED_COLORS.get(i))) {
                return i;
            }
        }

        return -1;
    }

    public static boolean matchesSlot(int slot, Item item) {
        return getSlotIndex(item) == slot;
    }
}

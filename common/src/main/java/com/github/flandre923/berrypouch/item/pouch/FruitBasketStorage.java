package com.github.flandre923.berrypouch.item.pouch;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FruitBasketStorage {
    private static final String ROOT_KEY = "FruitBasket";
    private static final String ITEMS_KEY = "Items";

    private FruitBasketStorage() {
    }

    public static long get(ItemStack basket, Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return getById(basket, id);
    }

    public static void set(ItemStack basket, Item item, long amount) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        setById(basket, id, amount);
    }

    public static long add(ItemStack basket, Item item, long amount) {
        if (amount <= 0) {
            return 0L;
        }
        long current = get(basket, item);
        long target = saturatingAdd(current, amount);
        set(basket, item, target);
        return target - current;
    }

    public static Map<ResourceLocation, Long> getAll(ItemStack basket) {
        CompoundTag itemsTag = getItemsTag(basket);
        Map<ResourceLocation, Long> result = new LinkedHashMap<>();
        for (String key : itemsTag.getAllKeys()) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            if (id == null) {
                continue;
            }
            long count = itemsTag.getLong(key);
            if (count > 0L) {
                result.put(id, count);
            }
        }
        return result.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().toString()))
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), LinkedHashMap::putAll);
    }

    public static void setAll(ItemStack basket, Map<ResourceLocation, Long> values) {
        CompoundTag root = getRootTag(basket);
        CompoundTag items = new CompoundTag();
        for (Map.Entry<ResourceLocation, Long> entry : values.entrySet()) {
            long count = Math.max(0L, entry.getValue());
            if (count > 0L) {
                items.putLong(entry.getKey().toString(), count);
            }
        }
        root.put(ITEMS_KEY, items);
        saveRootTag(basket, root);
    }

    private static long getById(ItemStack basket, ResourceLocation id) {
        CompoundTag itemsTag = getItemsTag(basket);
        return Math.max(0L, itemsTag.getLong(id.toString()));
    }

    private static void setById(ItemStack basket, ResourceLocation id, long amount) {
        CompoundTag root = getRootTag(basket);
        CompoundTag items = root.getCompound(ITEMS_KEY);
        String key = id.toString();
        if (amount <= 0L) {
            items.remove(key);
        } else {
            items.putLong(key, amount);
        }
        root.put(ITEMS_KEY, items);
        saveRootTag(basket, root);
    }

    private static CompoundTag getItemsTag(ItemStack basket) {
        return getRootTag(basket).getCompound(ITEMS_KEY);
    }

    private static CompoundTag getRootTag(ItemStack basket) {
        CustomData data = basket.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag fullTag = data.copyTag();
        return fullTag.getCompound(ROOT_KEY).copy();
    }

    private static void saveRootTag(ItemStack basket, CompoundTag rootTag) {
        CustomData data = basket.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag fullTag = data.copyTag();
        fullTag.put(ROOT_KEY, rootTag);
        basket.set(DataComponents.CUSTOM_DATA, CustomData.of(fullTag));
    }

    private static long saturatingAdd(long a, long b) {
        if (Long.MAX_VALUE - a < b) {
            return Long.MAX_VALUE;
        }
        return a + b;
    }
}

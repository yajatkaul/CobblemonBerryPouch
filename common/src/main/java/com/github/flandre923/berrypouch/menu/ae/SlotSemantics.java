package com.github.flandre923.berrypouch.menu.ae;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for {@link SlotSemantic}.
 */
public final class SlotSemantics {
    private static final Map<String, SlotSemantic> REGISTRY = new ConcurrentHashMap<>();

    private SlotSemantics() {
    }

    public static SlotSemantic register(String id, boolean playerSide) {
        return register(id, playerSide, 0);
    }

    public static SlotSemantic register(String id, boolean playerSide, int quickMovePriority) {
        SlotSemantic semantic = new SlotSemantic(id, playerSide, quickMovePriority);
        SlotSemantic existing = REGISTRY.putIfAbsent(id, semantic);
        if (existing != null) {
            throw new IllegalArgumentException("Semantic with id " + id + " was already registered");
        }
        return semantic;
    }

    public static SlotSemantic getOrThrow(String id) {
        SlotSemantic semantic = REGISTRY.get(id);
        if (semantic == null) {
            throw new IllegalArgumentException("Unknown slot semantic: " + id);
        }
        return semantic;
    }

    @Nullable
    public static SlotSemantic get(String id) {
        return REGISTRY.get(id);
    }

    public static final SlotSemantic STORAGE = register("STORAGE", false);

    public static final SlotSemantic PLAYER_INVENTORY = register("PLAYER_INVENTORY", true, 2000);
    public static final SlotSemantic PLAYER_HOTBAR = register("PLAYER_HOTBAR", true, 1000);

    // Berry Pouch semantics
    public static final SlotSemantic POUCH_NATURAL_BERRY = register("POUCH_NATURAL_BERRY", false, 3000);
    public static final SlotSemantic POUCH_MIXED_BERRY = register("POUCH_MIXED_BERRY", false, 3000);
    public static final SlotSemantic POUCH_OTHER_BAIT = register("POUCH_OTHER_BAIT", false, 2900);

    // Apricorn Basket semantics
    public static final SlotSemantic BASKET_STORAGE = register("BASKET_STORAGE", false, 3000);

    // Pokeball Gun semantics
    public static final SlotSemantic GUN_AMMO = register("GUN_AMMO", false, 2800);
}

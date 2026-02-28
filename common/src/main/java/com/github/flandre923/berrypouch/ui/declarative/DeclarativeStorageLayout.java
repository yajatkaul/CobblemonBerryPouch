package com.github.flandre923.berrypouch.ui.declarative;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public final class DeclarativeStorageLayout {
    private final int imageWidth;
    private final int imageHeight;
    private final List<SlotSpec> slots;
    private final Map<SlotRole, List<SlotSpec>> slotsByRole;
    private final List<ImageLayerSpec> imageLayers;
    private final Map<String, SlotVisualSpec> slotVisuals;

    private DeclarativeStorageLayout(int imageWidth, int imageHeight, List<SlotSpec> slots, List<ImageLayerSpec> imageLayers, Map<String, SlotVisualSpec> slotVisuals) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.slots = List.copyOf(slots);
        this.imageLayers = List.copyOf(imageLayers);
        this.slotVisuals = Map.copyOf(slotVisuals);

        var grouped = new EnumMap<SlotRole, List<SlotSpec>>(SlotRole.class);
        for (var role : SlotRole.values()) {
            grouped.put(role, new ArrayList<>());
        }
        for (var slot : slots) {
            grouped.get(slot.role()).add(slot);
        }

        var immutable = new EnumMap<SlotRole, List<SlotSpec>>(SlotRole.class);
        for (var entry : grouped.entrySet()) {
            immutable.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        this.slotsByRole = Map.copyOf(immutable);
    }

    public int imageWidth() {
        return imageWidth;
    }

    public int imageHeight() {
        return imageHeight;
    }

    public List<SlotSpec> slots() {
        return slots;
    }

    public List<ImageLayerSpec> imageLayers() {
        return imageLayers;
    }

    public SlotVisualSpec slotVisual(String group) {
        return slotVisuals.get(group);
    }

    public List<SlotSpec> slotsByRole(SlotRole role) {
        return slotsByRole.getOrDefault(role, List.of());
    }

    public static Builder builder(int imageWidth, int imageHeight) {
        return new Builder(imageWidth, imageHeight);
    }

    public enum SlotRole {
        POUCH_BERRY,
        POUCH_OTHER_BAIT,
        GUN_AMMO,
        PLAYER_INVENTORY,
        PLAYER_HOTBAR
    }

    public record SlotSpec(String group, SlotRole role, int index, int x, int y) {
    }

    public record ImageLayerSpec(ResourceLocation texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int z) {
    }

    public record SlotVisualSpec(ResourceLocation texture, int u, int v, int width, int height, int textureWidth, int textureHeight, int offsetX, int offsetY, int z) {
    }

    public static final class Builder {
        private final int imageWidth;
        private final int imageHeight;
        private final List<SlotSpec> slots = new ArrayList<>();
        private final List<ImageLayerSpec> imageLayers = new ArrayList<>();
        private final Map<String, SlotVisualSpec> slotVisuals = new java.util.HashMap<>();

        private Builder(int imageWidth, int imageHeight) {
            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;
        }

        public Builder grid(String group, SlotRole role, int startIndex, int startX, int startY, int columns, int rows, int stepX, int stepY) {
            if (columns <= 0 || rows <= 0) {
                throw new IllegalArgumentException("Grid dimensions must be positive");
            }
            int index = startIndex;
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < columns; col++) {
                    int x = startX + col * stepX;
                    int y = startY + row * stepY;
                    slots.add(new SlotSpec(group, role, index, x, y));
                    index++;
                }
            }
            return this;
        }

        public Builder slot(String group, SlotRole role, int index, int x, int y) {
            slots.add(new SlotSpec(group, role, index, x, y));
            return this;
        }

        public Builder imageLayer(ResourceLocation texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int z) {
            imageLayers.add(new ImageLayerSpec(texture, x, y, u, v, width, height, textureWidth, textureHeight, z));
            return this;
        }

        public Builder slotVisual(String group, ResourceLocation texture, int u, int v, int width, int height, int textureWidth, int textureHeight, int offsetX, int offsetY, int z) {
            slotVisuals.put(group, new SlotVisualSpec(texture, u, v, width, height, textureWidth, textureHeight, offsetX, offsetY, z));
            return this;
        }

        public DeclarativeStorageLayout build() {
            return new DeclarativeStorageLayout(imageWidth, imageHeight, slots, imageLayers, slotVisuals);
        }
    }
}

package com.github.flandre923.berrypouch.client.gui.style;

import com.github.flandre923.berrypouch.ModCommon;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages GUI styles found in resources.
 */
public final class StyleManager {
    private static final Map<String, ScreenStyle> STYLE_CACHE = new HashMap<>();

    public static final String PROP_INCLUDES = "includes";

    private StyleManager() {
    }

    public static ScreenStyle loadStyleDoc(String path) {
        try {
            ScreenStyle style = loadStyleDocInternal(path);
            style.validate();
            return style;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to find Screen JSON file: " + path + ": " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Screen JSON file: " + path, e);
        }
    }

    public static void clearCache() {
        STYLE_CACHE.clear();
    }

    private static ScreenStyle loadStyleDocInternal(String path) throws IOException {
        ScreenStyle cached = STYLE_CACHE.get(path);
        if (cached != null) {
            return cached;
        }

        Set<String> loadedFiles = new HashSet<>();
        JsonObject merged = loadMergedJsonTree(path, loadedFiles);
        ScreenStyle style = ScreenStyle.GSON.fromJson(merged, ScreenStyle.class);
        style.validate();
        STYLE_CACHE.put(path, style);
        return style;
    }

    private static String getBasePath(String path) {
        int lastSep = path.lastIndexOf('/');
        return lastSep == -1 ? "" : path.substring(0, lastSep + 1);
    }

    private static JsonObject loadMergedJsonTree(String path, Set<String> loadedFiles) throws IOException {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Path needs to start with slash");
        }

        if (path.contains("..")) {
            path = URI.create(path).normalize().toString();
        }
        if (!loadedFiles.add(path)) {
            throw new IllegalStateException("Recursive style includes: " + loadedFiles);
        }

        String basePath = getBasePath(path);
        ResourceLocation resourceId = ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, path.substring(1));

        var resourceManager = Minecraft.getInstance().getResourceManager();
        var resource = resourceManager.getResource(resourceId).orElseThrow(() -> new FileNotFoundException(resourceId.toString()));

        JsonObject document;
        try (var reader = resource.openAsReader()) {
            document = ScreenStyle.GSON.fromJson(reader, JsonObject.class);
        }

        if (document.has(PROP_INCLUDES)) {
            String[] includes = ScreenStyle.GSON.fromJson(document.get(PROP_INCLUDES), String[].class);

            List<JsonObject> layers = new ArrayList<>();
            for (String include : includes) {
                layers.add(loadMergedJsonTree(basePath + include, loadedFiles));
            }
            layers.add(document);
            document = combineLayers(layers);
        }
        return document;
    }

    private static JsonObject combineLayers(List<JsonObject> layers) {
        JsonObject result = new JsonObject();

        for (JsonObject layer : layers) {
            for (Map.Entry<String, JsonElement> entry : layer.entrySet()) {
                result.add(entry.getKey(), entry.getValue());
            }
        }

        mergeObjectKeys("slots", layers, result);
        mergeObjectKeys("text", layers, result);
        mergeObjectKeys("palette", layers, result);
        mergeObjectKeys("images", layers, result);
        mergeObjectKeys("terminalStyle", layers, result);
        mergeObjectKeys("widgets", layers, result);
        mergeObjectKeys("tooltips", layers, result);

        return result;
    }

    private static void mergeObjectKeys(String propertyName, List<JsonObject> layers, JsonObject target)
            throws JsonParseException {
        JsonObject merged = null;
        for (JsonObject layer : layers) {
            JsonElement layerEl = layer.get(propertyName);
            if (layerEl == null) {
                continue;
            }
            if (!layerEl.isJsonObject()) {
                throw new JsonParseException("Expected " + propertyName + " to be an object, but was: " + layerEl);
            }

            JsonObject layerObj = layerEl.getAsJsonObject();
            if (merged == null) {
                merged = new JsonObject();
            }
            for (Map.Entry<String, JsonElement> entry : layerObj.entrySet()) {
                merged.add(entry.getKey(), entry.getValue());
            }
        }

        if (merged != null) {
            target.add(propertyName, merged);
        }
    }
}

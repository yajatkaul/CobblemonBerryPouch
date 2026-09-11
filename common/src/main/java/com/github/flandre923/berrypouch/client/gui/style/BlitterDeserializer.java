package com.github.flandre923.berrypouch.client.gui.style;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

enum BlitterDeserializer implements JsonDeserializer<Blitter> {
    INSTANCE;

    @Override
    public Blitter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (!json.isJsonObject()) {
            throw new JsonParseException("Blitters must be objects");
        }

        JsonObject root = json.getAsJsonObject();
        String texture = GsonHelper.getAsString(root, "texture");
        int textureWidth = GsonHelper.getAsInt(root, "textureWidth", Blitter.DEFAULT_TEXTURE_WIDTH);
        int textureHeight = GsonHelper.getAsInt(root, "textureHeight", Blitter.DEFAULT_TEXTURE_HEIGHT);

        Blitter blitter;
        if (texture.contains(":")) {
            int sep = texture.indexOf(':');
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(texture.substring(0, sep), texture.substring(sep + 1));
            blitter = new Blitter(id, textureWidth, textureHeight);
        } else {
            blitter = Blitter.texture(texture, textureWidth, textureHeight);
        }

        if (root.has("srcRect")) {
            Rect2i srcRect = context.deserialize(root.get("srcRect"), Rect2i.class);
            blitter = blitter.src(srcRect);
        }

        return blitter;
    }
}

package com.github.flandre923.berrypouch.client.gui.style;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

/**
 * Deserializes {@link Rect2i} from either [x,y,width,height] or an object.
 */
public enum Rectangle2dDeserializer implements JsonDeserializer<Rect2i> {
    INSTANCE;

    @Override
    public Rect2i deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json.isJsonArray()) {
            JsonArray arr = json.getAsJsonArray();
            if (arr.size() != 4) {
                throw new JsonParseException("Rectangles expressed as arrays must have 4 elements.");
            }
            int x = arr.get(0).getAsInt();
            int y = arr.get(1).getAsInt();
            int width = arr.get(2).getAsInt();
            int height = arr.get(3).getAsInt();
            return new Rect2i(x, y, width, height);
        }

        JsonObject obj = json.getAsJsonObject();
        int x = GsonHelper.getAsInt(obj, "x", 0);
        int y = GsonHelper.getAsInt(obj, "y", 0);
        int width = GsonHelper.getAsInt(obj, "width");
        int height = GsonHelper.getAsInt(obj, "height");
        return new Rect2i(x, y, width, height);
    }
}

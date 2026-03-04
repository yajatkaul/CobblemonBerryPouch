package com.github.flandre923.berrypouch.client.gui.style;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.Rect2i;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * A screen style document defines visual aspects of a GUI screen.
 */
public class ScreenStyle {
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(Blitter.class, BlitterDeserializer.INSTANCE)
            .registerTypeAdapter(Rect2i.class, Rectangle2dDeserializer.INSTANCE)
            .registerTypeAdapter(Color.class, ColorDeserializer.INSTANCE)
            .create();

    @Nullable
    private String helpTopic;

    private final Map<String, SlotPosition> slots = new HashMap<>();
    private final Map<String, Text> text = new HashMap<>();
    private final Map<PaletteColor, Color> palette = createDefaultPalette();
    private final Map<String, Blitter> images = new HashMap<>();
    private final Map<String, WidgetStyle> widgets = new HashMap<>();
    private final Map<String, TooltipArea> tooltips = new HashMap<>();

    @Nullable
    private Blitter background;
    @Nullable
    private GeneratedBackground generatedBackground;
    @Nullable
    private TerminalStyle terminalStyle;

    public Color getColor(PaletteColor color) {
        return palette.get(color);
    }

    public Map<String, SlotPosition> getSlots() {
        return slots;
    }

    public Map<String, Text> getText() {
        return text;
    }

    public Map<String, TooltipArea> getTooltips() {
        return tooltips;
    }

    @Nullable
    public Blitter getBackground() {
        return background != null ? background.copy() : null;
    }

    @Nullable
    public GeneratedBackground getGeneratedBackground() {
        return generatedBackground;
    }

    @Nullable
    public String getHelpTopic() {
        return helpTopic;
    }

    public WidgetStyle getWidget(String id) {
        WidgetStyle widget = widgets.get(id);
        if (widget == null) {
            throw new IllegalStateException("Screen is missing required widget: " + id);
        }
        return widget;
    }

    public Blitter getImage(String id) {
        Blitter image = images.get(id);
        if (image == null) {
            throw new IllegalStateException("Screen is missing required image: " + id);
        }
        return image;
    }

    @Nullable
    public TerminalStyle getTerminalStyle() {
        return terminalStyle;
    }

    public void validate() {
        for (PaletteColor value : PaletteColor.values()) {
            if (!palette.containsKey(value)) {
                throw new RuntimeException("Palette is missing color " + value);
            }
        }
        if (terminalStyle != null) {
            terminalStyle.validate();
        }
    }

    private static Map<PaletteColor, Color> createDefaultPalette() {
        Map<PaletteColor, Color> result = new EnumMap<>(PaletteColor.class);
        result.put(PaletteColor.DEFAULT_TEXT_COLOR, Color.parse("#FF404040"));
        result.put(PaletteColor.MUTED_TEXT_COLOR, Color.parse("#FF7A7A7A"));
        result.put(PaletteColor.SELECTION_COLOR, Color.parse("#FF9CD3FF"));
        result.put(PaletteColor.TEXTFIELD_PLACEHOLDER, Color.parse("#FF9A9A9A"));
        result.put(PaletteColor.TEXTFIELD_SELECTION, Color.parse("#669CD3FF"));
        result.put(PaletteColor.TEXTFIELD_ERROR, Color.parse("#FFFF6666"));
        result.put(PaletteColor.TEXTFIELD_TEXT, Color.parse("#FFE0E0E0"));
        result.put(PaletteColor.ERROR, Color.parse("#FFFF5555"));
        return result;
    }
}

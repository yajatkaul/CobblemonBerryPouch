package com.github.flandre923.berrypouch.client.gui.style;

import net.minecraft.network.chat.Component;

public class Text {
    private Component text = Component.empty();
    private PaletteColor color = PaletteColor.DEFAULT_TEXT_COLOR;
    private Position position;
    private TextAlignment align = TextAlignment.LEFT;
    private float scale = 1.0f;
    private int maxWidth = 0;

    public Component getText() {
        return text;
    }

    public void setText(Component text) {
        this.text = text;
    }

    public PaletteColor getColor() {
        return color;
    }

    public void setColor(PaletteColor color) {
        this.color = color;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public TextAlignment getAlign() {
        return align;
    }

    public void setAlign(TextAlignment align) {
        this.align = align;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }
}

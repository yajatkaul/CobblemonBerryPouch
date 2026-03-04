package com.github.flandre923.berrypouch.client.gui.style;

public class WidgetStyle extends Position {
    private int width;
    private int height;
    private boolean hideEdge;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isHideEdge() {
        return hideEdge;
    }

    public void setHideEdge(boolean hideEdge) {
        this.hideEdge = hideEdge;
    }
}

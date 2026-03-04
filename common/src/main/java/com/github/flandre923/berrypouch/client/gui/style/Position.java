package com.github.flandre923.berrypouch.client.gui.style;

import com.github.flandre923.berrypouch.client.Point;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.Rect2i;

/**
 * Describes position of an element relative to a screen bounds box.
 */
public class Position {
    @Nullable
    private Integer left;
    @Nullable
    private Integer top;
    @Nullable
    private Integer right;
    @Nullable
    private Integer bottom;

    public Integer getLeft() {
        return left;
    }

    public void setLeft(Integer left) {
        this.left = left;
    }

    public Integer getTop() {
        return top;
    }

    public void setTop(Integer top) {
        this.top = top;
    }

    public Integer getRight() {
        return right;
    }

    public void setRight(Integer right) {
        this.right = right;
    }

    public Integer getBottom() {
        return bottom;
    }

    public void setBottom(Integer bottom) {
        this.bottom = bottom;
    }

    public Point resolve(Rect2i bounds) {
        int x;
        int y;
        if (left != null) {
            x = left;
        } else if (right != null) {
            x = bounds.getWidth() - right;
        } else {
            x = 0;
        }

        if (top != null) {
            y = top;
        } else if (bottom != null) {
            y = bounds.getHeight() - bottom;
        } else {
            y = 0;
        }

        return new Point(x, y).move(bounds.getX(), bounds.getY());
    }
}

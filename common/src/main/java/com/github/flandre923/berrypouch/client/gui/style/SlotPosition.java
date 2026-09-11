package com.github.flandre923.berrypouch.client.gui.style;

import com.github.flandre923.berrypouch.client.gui.layout.SlotGridLayout;
import org.jetbrains.annotations.Nullable;

public class SlotPosition extends Position {
    @Nullable
    private SlotGridLayout grid;

    private boolean hidden = false;

    @Nullable
    public SlotGridLayout getGrid() {
        return grid;
    }

    public void setGrid(@Nullable SlotGridLayout grid) {
        this.grid = grid;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(@Nullable Boolean hidden) {
        this.hidden = Boolean.TRUE.equals(hidden);
    }
}

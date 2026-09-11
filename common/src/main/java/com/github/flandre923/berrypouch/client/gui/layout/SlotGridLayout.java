package com.github.flandre923.berrypouch.client.gui.layout;

import com.github.flandre923.berrypouch.client.Point;

public enum SlotGridLayout {
    BREAK_AFTER_10COLS {
        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return getRowBreakPosition(x, y, semanticIdx, 10);
        }
    },

    BREAK_AFTER_9COLS {
        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return getRowBreakPosition(x, y, semanticIdx, 9);
        }
    },

    BREAK_AFTER_7COLS {
        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return getRowBreakPosition(x, y, semanticIdx, 7);
        }
    },

    BREAK_AFTER_3COLS {
        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return getRowBreakPosition(x, y, semanticIdx, 3);
        }
    },

    BREAK_AFTER_2COLS {
        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return getRowBreakPosition(x, y, semanticIdx, 2);
        }
    },

    HORIZONTAL {
        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return new Point(x, y).move(semanticIdx * 18, 0);
        }
    },

    VERTICAL {
        @Override
        public Point getPosition(int x, int y, int semanticIdx) {
            return new Point(x, y).move(0, semanticIdx * 18);
        }
    };

    private static Point getRowBreakPosition(int x, int y, int semanticIdx, int cols) {
        int row = semanticIdx / cols;
        int col = semanticIdx % cols;
        return new Point(x, y).move(col * 18, row * 18);
    }

    public abstract Point getPosition(int x, int y, int semanticIdx);
}

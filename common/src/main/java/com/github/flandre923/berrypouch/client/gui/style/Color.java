package com.github.flandre923.berrypouch.client.gui.style;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Color {
    private static final Pattern PATTERN = Pattern.compile("^#([0-9a-fA-F]{2}){3,4}$");

    private final int r;
    private final int g;
    private final int b;
    private final int a;

    public Color(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public static Color parse(String string) {
        Matcher m = PATTERN.matcher(string);
        if (!m.matches()) {
            throw new IllegalArgumentException("Color must have format #AARRGGBB (" + string + ")");
        }

        int r;
        int g;
        int b;
        int a = 255;
        if (string.length() == 7) {
            r = Integer.valueOf(string.substring(1, 3), 16);
            g = Integer.valueOf(string.substring(3, 5), 16);
            b = Integer.valueOf(string.substring(5, 7), 16);
        } else {
            a = Integer.valueOf(string.substring(1, 3), 16);
            r = Integer.valueOf(string.substring(3, 5), 16);
            g = Integer.valueOf(string.substring(5, 7), 16);
            b = Integer.valueOf(string.substring(7, 9), 16);
        }

        return new Color(r, g, b, a);
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public int getA() {
        return a;
    }

    public int toARGB() {
        return a << 24 | r << 16 | g << 8 | b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Color color)) {
            return false;
        }
        return r == color.r && g == color.g && b == color.b && a == color.a;
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, g, b, a);
    }
}

package com.github.flandre923.berrypouch.client.gui.style;

import com.github.flandre923.berrypouch.ModCommon;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import java.util.Objects;

/**
 * Utility for drawing textured rectangles in GUIs.
 */
public final class Blitter {
    public static final int DEFAULT_TEXTURE_WIDTH = 256;
    public static final int DEFAULT_TEXTURE_HEIGHT = 256;

    private final ResourceLocation texture;
    private final int referenceWidth;
    private final int referenceHeight;

    private int r = 255;
    private int g = 255;
    private int b = 255;
    private int a = 255;

    private Rect2i srcRect;
    private Rect2i destRect = new Rect2i(0, 0, 0, 0);
    private boolean blending = true;
    private TextureTransform transform = TextureTransform.NONE;
    private int zOffset;

    Blitter(ResourceLocation texture, int referenceWidth, int referenceHeight) {
        this.texture = texture;
        this.referenceWidth = referenceWidth;
        this.referenceHeight = referenceHeight;
    }

    public static Blitter texture(ResourceLocation file) {
        return texture(file, DEFAULT_TEXTURE_WIDTH, DEFAULT_TEXTURE_HEIGHT);
    }

    public static Blitter texture(String file) {
        return texture(file, DEFAULT_TEXTURE_WIDTH, DEFAULT_TEXTURE_HEIGHT);
    }

    public static Blitter texture(ResourceLocation file, int referenceWidth, int referenceHeight) {
        return new Blitter(file, referenceWidth, referenceHeight);
    }

    public static Blitter texture(String file, int referenceWidth, int referenceHeight) {
        return new Blitter(ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "textures/" + file),
                referenceWidth, referenceHeight);
    }

    public Blitter copy() {
        Blitter result = new Blitter(texture, referenceWidth, referenceHeight);
        result.srcRect = srcRect;
        result.destRect = destRect;
        result.r = r;
        result.g = g;
        result.b = b;
        result.a = a;
        result.blending = blending;
        result.transform = transform;
        result.zOffset = zOffset;
        return result;
    }

    public int getSrcX() {
        return srcRect == null ? 0 : srcRect.getX();
    }

    public int getSrcY() {
        return srcRect == null ? 0 : srcRect.getY();
    }

    public int getSrcWidth() {
        return srcRect == null ? destRect.getWidth() : srcRect.getWidth();
    }

    public int getSrcHeight() {
        return srcRect == null ? destRect.getHeight() : srcRect.getHeight();
    }

    public Blitter src(int x, int y, int w, int h) {
        this.srcRect = new Rect2i(x, y, w, h);
        return this;
    }

    public Blitter src(Rect2i rect) {
        return src(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public Blitter srcWidth(int w) {
        this.srcRect = new Rect2i(srcRect.getX(), srcRect.getY(), w, srcRect.getHeight());
        return this;
    }

    public Blitter srcHeight(int h) {
        this.srcRect = new Rect2i(srcRect.getX(), srcRect.getY(), srcRect.getWidth(), h);
        return this;
    }

    public Blitter dest(int x, int y, int w, int h) {
        this.destRect = new Rect2i(x, y, w, h);
        return this;
    }

    public Blitter dest(int x, int y) {
        return dest(x, y, 0, 0);
    }

    public Blitter dest(Rect2i rect) {
        return dest(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public Blitter color(float r, float g, float b) {
        this.r = (int) (Mth.clamp(r, 0, 1) * 255);
        this.g = (int) (Mth.clamp(g, 0, 1) * 255);
        this.b = (int) (Mth.clamp(b, 0, 1) * 255);
        return this;
    }

    public Blitter colorArgb(int packedArgb) {
        this.a = FastColor.ARGB32.alpha(packedArgb);
        this.r = FastColor.ARGB32.red(packedArgb);
        this.g = FastColor.ARGB32.green(packedArgb);
        this.b = FastColor.ARGB32.blue(packedArgb);
        return this;
    }

    public Blitter colorRgb(int packedRgb) {
        float r = (packedRgb >> 16 & 255) / 255.0F;
        float g = (packedRgb >> 8 & 255) / 255.0F;
        float b = (packedRgb & 255) / 255.0F;
        return color(r, g, b);
    }

    public Blitter opacity(float a) {
        this.a = (int) (Mth.clamp(a, 0, 1) * 255);
        return this;
    }

    public Blitter color(float r, float g, float b, float a) {
        return color(r, g, b).opacity(a);
    }

    public Blitter transform(TextureTransform transform) {
        this.transform = Objects.requireNonNull(transform);
        return this;
    }

    public Blitter blending(boolean enable) {
        this.blending = enable;
        return this;
    }

    public Blitter zOffset(int offset) {
        this.zOffset = offset;
        return this;
    }

    public void blit(GuiGraphics guiGraphics) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, this.texture);

        float minU;
        float minV;
        float maxU;
        float maxV;
        if (srcRect == null) {
            minU = 0;
            minV = 0;
            maxU = 1;
            maxV = 1;
        } else {
            minU = srcRect.getX() / (float) referenceWidth;
            minV = srcRect.getY() / (float) referenceHeight;
            maxU = (srcRect.getX() + srcRect.getWidth()) / (float) referenceWidth;
            maxV = (srcRect.getY() + srcRect.getHeight()) / (float) referenceHeight;
        }

        switch (transform) {
            case MIRROR_H -> {
                float tmp = minU;
                minU = maxU;
                maxU = tmp;
            }
            case MIRROR_V -> {
                float tmp = minV;
                minV = maxV;
                maxV = tmp;
            }
            default -> {
            }
        }

        float x1 = destRect.getX();
        float y1 = destRect.getY();
        float x2 = x1;
        float y2 = y1;
        if (destRect.getWidth() != 0 && destRect.getHeight() != 0) {
            x2 += destRect.getWidth();
            y2 += destRect.getHeight();
        } else if (srcRect != null) {
            x2 += srcRect.getWidth();
            y2 += srcRect.getHeight();
        }

        Matrix4f matrix = guiGraphics.pose().last().pose();
        var bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.addVertex(matrix, x1, y2, zOffset).setUv(minU, maxV).setColor(r, g, b, a);
        bufferBuilder.addVertex(matrix, x2, y2, zOffset).setUv(maxU, maxV).setColor(r, g, b, a);
        bufferBuilder.addVertex(matrix, x2, y1, zOffset).setUv(maxU, minV).setColor(r, g, b, a);
        bufferBuilder.addVertex(matrix, x1, y1, zOffset).setUv(minU, minV).setColor(r, g, b, a);

        if (blending) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        } else {
            RenderSystem.disableBlend();
        }
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }
}

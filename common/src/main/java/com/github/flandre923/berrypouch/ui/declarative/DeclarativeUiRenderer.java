package com.github.flandre923.berrypouch.ui.declarative;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;

public final class DeclarativeUiRenderer {
    private DeclarativeUiRenderer() {
    }

    public static void renderLayers(GuiGraphics guiGraphics, DeclarativeStorageLayout layout, int leftPos, int topPos) {
        PoseStack pose = guiGraphics.pose();
        for (var layer : layout.imageLayers()) {
            pose.pushPose();
            pose.translate(0, 0, layer.z());
            guiGraphics.blit(layer.texture(), leftPos + layer.x(), topPos + layer.y(), layer.u(), layer.v(), layer.width(), layer.height(), layer.textureWidth(), layer.textureHeight());
            pose.popPose();
        }
    }

    public static void renderSlotFrames(GuiGraphics guiGraphics, DeclarativeStorageLayout layout, int leftPos, int topPos) {
        PoseStack pose = guiGraphics.pose();
        for (var slot : layout.slots()) {
            var visual = layout.slotVisual(slot.group());
            if (visual == null) {
                continue;
            }
            pose.pushPose();
            pose.translate(0, 0, visual.z());
            guiGraphics.blit(visual.texture(), leftPos + slot.x() + visual.offsetX(), topPos + slot.y() + visual.offsetY(), visual.u(), visual.v(), visual.width(), visual.height(), visual.textureWidth(), visual.textureHeight());
            pose.popPose();
        }
    }
}

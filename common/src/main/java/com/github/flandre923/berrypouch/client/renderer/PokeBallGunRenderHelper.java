package com.github.flandre923.berrypouch.client.renderer;

import com.github.flandre923.berrypouch.client.config.PokeBallGunTransformSettings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PokeBallGunRenderHelper {

    public static void renderPokeBallOnGun(
            ItemStack selectedBall,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int combinedLight,
            int combinedOverlay,
            ItemRenderer itemRenderer,
            Level level,
            int seed
    ) {
        if (selectedBall.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        applyBallTransform(displayContext, poseStack);

        BakedModel ballModel = itemRenderer.getModel(selectedBall, level, null, seed);
        itemRenderer.render(
                selectedBall,
                ItemDisplayContext.NONE,
                false,
                poseStack,
                multiBufferSource,
                combinedLight,
                combinedOverlay,
                ballModel
        );

        poseStack.popPose();
    }

    private static void applyBallTransform(ItemDisplayContext displayContext, PoseStack poseStack) {
        PokeBallGunTransformSettings.Transform transform =
                displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                        || displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                        ? PokeBallGunTransformSettings.getFirstPerson()
                        : PokeBallGunTransformSettings.getOther();

        poseStack.translate(transform.translateX, transform.translateY, transform.translateZ);
        if (transform.rotateX != 0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(transform.rotateX));
        }
        if (transform.rotateY != 0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees(transform.rotateY));
        }
        if (transform.rotateZ != 0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(transform.rotateZ));
        }
        poseStack.scale(transform.scaleX, transform.scaleY, transform.scaleZ);

        float baseRotationY = PokeBallGunTransformSettings.getBaseRotationY();
        if (baseRotationY != 0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees(baseRotationY));
        }
    }
}

package com.github.flandre923.berrypouch.client.renderer;

import com.cobblemon.mod.common.item.PokeBallItem;
import com.github.flandre923.berrypouch.item.PokeBallGun;
import com.github.flandre923.berrypouch.item.pouch.PokeBallGunHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class PokeBallGunRenderHelper {
    
    private static final float BALL_SCALE = 0.5f;
    private static final float BALL_OFFSET_X = 0.0f;
    private static final float BALL_OFFSET_Y = 0.0f;
    private static final float BALL_OFFSET_Z = 0.3125f;
    
    public static boolean shouldRenderPokeBall(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof PokeBallGun;
    }
    
    public static ItemStack getSelectedPokeBall(ItemStack gunStack) {
        if (!shouldRenderPokeBall(gunStack)) {
            return ItemStack.EMPTY;
        }
        return PokeBallGunHelper.getSelectedItem(gunStack);
    }
    
    public static void renderPokeBallOnGun(ItemStack gunStack, PoseStack poseStack, 
                                           MultiBufferSource bufferSource, int packedLight,
                                           ItemRenderer itemRenderer) {
        ItemStack ballStack = getSelectedPokeBall(gunStack);
        if (ballStack.isEmpty() || !(ballStack.getItem() instanceof PokeBallItem)) {
            return;
        }
        
        poseStack.pushPose();
        
        poseStack.translate(BALL_OFFSET_X, BALL_OFFSET_Y, BALL_OFFSET_Z);
        poseStack.scale(BALL_SCALE, BALL_SCALE, BALL_SCALE);
        
        BakedModel ballModel = itemRenderer.getModel(ballStack, null, null, 0);
        
        itemRenderer.render(
            ballStack, 
            ItemDisplayContext.GUI, 
            false, 
            poseStack, 
            bufferSource,
            packedLight, 
            OverlayTexture.NO_OVERLAY, 
            ballModel
        );
        
        poseStack.popPose();
    }
}

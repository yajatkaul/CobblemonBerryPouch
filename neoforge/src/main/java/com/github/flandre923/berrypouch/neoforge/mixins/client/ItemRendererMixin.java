package com.github.flandre923.berrypouch.neoforge.mixins.client;

import com.github.flandre923.berrypouch.client.renderer.PokeBallGunRenderHelper;
import com.github.flandre923.berrypouch.item.PokeBallGun;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    
    @Inject(
        method = "render",
        at = @At("RETURN")
    )
    private void onRenderItem(ItemStack stack, ItemDisplayContext displayContext, boolean leftHanded, 
                              PoseStack poseStack, 
                              net.minecraft.client.renderer.MultiBufferSource bufferSource,
                              int combinedLight, int combinedOverlay, BakedModel model, 
                              CallbackInfo ci) {
        if (stack.getItem() instanceof PokeBallGun) {
            PokeBallGunRenderHelper.renderPokeBallOnGun(
                stack, poseStack, bufferSource, combinedLight, (ItemRenderer)(Object)this
            );
        }
    }
}

package com.github.flandre923.berrypouch.neoforge.mixins.client;

import com.github.flandre923.berrypouch.client.renderer.PokeBallGunRenderHelper;
import com.github.flandre923.berrypouch.item.PokeBallGun;
import com.github.flandre923.berrypouch.item.pouch.PokeBallGunHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Inject(
            method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At("TAIL")
    )
    private void berrypouch$renderGunBall(ItemStack stack, ItemDisplayContext displayContext, boolean leftHanded, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, BakedModel model, CallbackInfo ci) {
        if (!(stack.getItem() instanceof PokeBallGun)) {
            return;
        }

        if (displayContext == ItemDisplayContext.GUI) {
            return;
        }

        ItemStack selectedBall = PokeBallGunHelper.getSelectedItem(stack);
        if (selectedBall.isEmpty()) {
            return;
        }

        PokeBallGunRenderHelper.renderPokeBallOnGun(
                selectedBall,
                displayContext,
                poseStack,
                buffer,
                light,
                overlay,
                (ItemRenderer) (Object) this,
                Minecraft.getInstance().level,
                0
        );
    }
}

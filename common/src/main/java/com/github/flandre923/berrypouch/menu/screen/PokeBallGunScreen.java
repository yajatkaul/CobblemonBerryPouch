package com.github.flandre923.berrypouch.menu.screen;

import com.github.flandre923.berrypouch.ModCommon;
import com.github.flandre923.berrypouch.menu.container.PokeBallGunContainer;
import com.github.flandre923.berrypouch.menu.layout.PokeBallGunLayout;
import com.github.flandre923.berrypouch.ui.declarative.DeclarativeStorageLayout;
import com.github.flandre923.berrypouch.ui.declarative.DeclarativeUiRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PokeBallGunScreen extends AbstractContainerScreen<PokeBallGunContainer> {
    private static final ResourceLocation STAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "textures/gui/star.png");
    private static final int STAR_SIZE = 20;

    public PokeBallGunScreen(PokeBallGunContainer menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = PokeBallGunLayout.WIDTH;
        this.imageHeight = PokeBallGunLayout.HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94; // 调整标签位置
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gui, mouseX, mouseY, partialTicks);
        super.render(gui, mouseX, mouseY, partialTicks);
        renderSelectionHighlight(gui);
        this.renderTooltip(gui, mouseX, mouseY);
    }


    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        DeclarativeUiRenderer.renderLayers(gui, PokeBallGunLayout.STORAGE, leftPos, topPos);
        DeclarativeUiRenderer.renderSlotFrames(gui, PokeBallGunLayout.STORAGE, leftPos, topPos);
    }


    private void renderSelectionHighlight(GuiGraphics gui) {
        int selectedIndex = menu.getSelectedIndex();
        PoseStack poseStack = gui.pose();

        for (DeclarativeStorageLayout.SlotSpec slot : PokeBallGunLayout.STORAGE.slotsByRole(DeclarativeStorageLayout.SlotRole.GUN_AMMO)) {
            if (slot.index() == selectedIndex) {
                poseStack.pushPose();
                poseStack.translate(0, 0, 250);
                gui.blit(STAR_TEXTURE, this.leftPos + slot.x() - 2, this.topPos + slot.y() - 2, 0, 0, STAR_SIZE, STAR_SIZE, STAR_SIZE, STAR_SIZE);
                poseStack.popPose();
                break;
            }
        }
    }


}

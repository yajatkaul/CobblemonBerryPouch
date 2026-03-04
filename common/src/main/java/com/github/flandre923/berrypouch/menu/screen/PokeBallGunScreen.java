package com.github.flandre923.berrypouch.menu.screen;

import com.github.flandre923.berrypouch.ModCommon;
import com.github.flandre923.berrypouch.client.gui.style.StyleManager;
import com.github.flandre923.berrypouch.menu.ae.SlotSemantics;
import com.github.flandre923.berrypouch.menu.container.PokeBallGunContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class PokeBallGunScreen extends AEBaseScreen<PokeBallGunContainer> {
    private static final ResourceLocation STAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ModCommon.MOD_ID, "textures/gui/star.png");
    private static final int STAR_SIZE = 20;

    public PokeBallGunScreen(PokeBallGunContainer menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, StyleManager.loadStyleDoc("/screens/pokeball_gun.json"));
        setTextContent(TEXT_ID_DIALOG_TITLE, title);
        setTextContent(TEXT_ID_PLAYER_INVENTORY, this.playerInventoryTitle);
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTick) {
        super.drawBG(guiGraphics, offsetX, offsetY, mouseX, mouseY, partialTick);
        try {
            renderSlotFrames(guiGraphics, style.getImage("slot_frame"));
        } catch (IllegalStateException ignored) {
        }
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        renderSelectionHighlight(guiGraphics, offsetX, offsetY);
    }

    private void renderSelectionHighlight(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        int selectedIndex = menu.getSelectedIndex();
        PoseStack poseStack = guiGraphics.pose();

        for (Slot slot : menu.getSlots(SlotSemantics.GUN_AMMO)) {
            if (slot.getContainerSlot() == selectedIndex) {
                poseStack.pushPose();
                poseStack.translate(0, 0, 250);
                guiGraphics.blit(
                        STAR_TEXTURE,
                        offsetX + slot.x - 2,
                        offsetY + slot.y - 2,
                        0,
                        0,
                        STAR_SIZE,
                        STAR_SIZE,
                        STAR_SIZE,
                        STAR_SIZE);
                poseStack.popPose();
                break;
            }
        }
    }
}
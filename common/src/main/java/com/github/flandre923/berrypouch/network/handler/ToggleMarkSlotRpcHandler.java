package com.github.flandre923.berrypouch.network.handler;

import com.github.flandre923.berrypouch.ModCommon;
import com.github.flandre923.berrypouch.helper.MarkedSlotsHelper;
import com.github.flandre923.berrypouch.item.BerryPouch;
import com.github.flandre923.berrypouch.item.pouch.BerryPouchType;
import com.github.flandre923.berrypouch.menu.container.AbstractBerryPouchContainer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

public final class ToggleMarkSlotRpcHandler {
    private ToggleMarkSlotRpcHandler() {
    }

    public static void handle(ServerPlayer player, int slotIndex) {
        if (player.containerMenu instanceof AbstractBerryPouchContainer pouchContainer) {
            ItemStack pouchStack = pouchContainer.getPouchStack();
            if (!pouchStack.isEmpty() && pouchStack.getItem() instanceof BerryPouch) {
                BerryPouchType pouchType = ((BerryPouch) pouchStack.getItem()).getPouchType();
                if (slotIndex >= 0 && slotIndex < pouchType.getSize()) {
                    MarkedSlotsHelper.toggleMarkedSlot(pouchStack, slotIndex);
                    player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5f, 1.2f);
                    pouchContainer.broadcastChanges();
                } else {
                    ModCommon.LOG.warn("Player {} tried to toggle invalid slot index {} for pouch {}", player.getName().getString(), slotIndex, pouchStack.getHoverName().getString());
                }
            } else {
                ModCommon.LOG.warn("Player {} sent ToggleMarkSlotPacket but the container's pouchStack is invalid", player.getName().getString());
            }
        } else {
            ModCommon.LOG.warn("Player {} sent ToggleMarkSlotPacket but is not in a BerryPouchContainer", player.getName().getString());
        }
    }
}

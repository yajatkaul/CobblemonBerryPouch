package com.github.flandre923.berrypouch.network.handler;

import com.cobblemon.mod.common.item.interactive.PokerodItem;
import com.github.flandre923.berrypouch.event.FishingRodEventHandler;
import com.github.flandre923.berrypouch.helper.PouchDataHelper;
import com.github.flandre923.berrypouch.item.BerryPouch;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ToggleAutoBerryRpcHandler {
    private ToggleAutoBerryRpcHandler() {
    }

    public static void handle(ServerPlayer player) {
        boolean oldState = PouchDataHelper.isAutoBerryEnabled(player);
        boolean newState = !oldState;
        PouchDataHelper.setAutoBerryEnabled(player, newState);

        if (oldState) {
            Level level = player.level();
            InteractionHand[] hands = {InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND};
            for (InteractionHand hand : hands) {
                ItemStack heldStack = player.getItemInHand(hand);
                if (FishingRodEventHandler.isCobblemonFishingRod(heldStack)) {
                    ItemStack currentBait = PokerodItem.Companion.getBaitStackOnRod(heldStack);
                    BerryPouch.onPickupItem(currentBait, player);
                    if (!currentBait.isEmpty()) {
                        PokerodItem.Companion.setBait(heldStack, ItemStack.EMPTY);
                        break;
                    }
                }
            }
        }

        Component message = Component.translatable("message.berrypouch.auto_berry_toggled." + newState);
        player.sendSystemMessage(message, true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, 0.5f, 1.2f);
    }
}

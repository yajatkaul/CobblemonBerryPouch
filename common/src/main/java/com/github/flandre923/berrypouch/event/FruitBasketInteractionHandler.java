package com.github.flandre923.berrypouch.event;

import com.github.flandre923.berrypouch.item.FruitBasketItem;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public final class FruitBasketInteractionHandler {
    private FruitBasketInteractionHandler() {
    }

    public static void register() {
        InteractionEvent.RIGHT_CLICK_BLOCK.register(FruitBasketInteractionHandler::onRightClickBlock);
    }

    private static EventResult onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return EventResult.pass();
        }

        boolean handled = FruitBasketItem.handleRightClickBlock(serverPlayer, hand, pos);
        return handled ? EventResult.interruptTrue() : EventResult.pass();
    }
}

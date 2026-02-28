package com.github.flandre923.berrypouch.network.handler;

import com.github.flandre923.berrypouch.item.BerryPouch;
import com.github.flandre923.berrypouch.item.pouch.BerryPouchManager;
import io.wispforest.accessories.api.AccessoriesCapability;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class OpenPouchRpcHandler {
    private OpenPouchRpcHandler() {
    }

    public static void handle(ServerPlayer player) {
        Level level = player.level();
        AccessoriesCapability accessoriesCap = AccessoriesCapability.get(player);
        if (accessoriesCap == null) return;

        accessoriesCap.getEquipped(stack -> stack.getItem() instanceof BerryPouch)
                .stream()
                .findFirst()
                .ifPresent(entry -> {
                    ItemStack pouchStack = entry.stack();
                    if (!pouchStack.isEmpty() && pouchStack.getItem() instanceof BerryPouch && !player.level().isClientSide) {
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUNDLE_INSERT, SoundSource.BLOCKS, 0.5f, level.random.nextFloat() * 0.1F + 0.9F);
                        BerryPouchManager.openPouchGUI(player, pouchStack, 2);
                    }
                });
    }
}

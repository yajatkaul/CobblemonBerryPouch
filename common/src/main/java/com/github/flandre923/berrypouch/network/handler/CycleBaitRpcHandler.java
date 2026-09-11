package com.github.flandre923.berrypouch.network.handler;

import com.cobblemon.mod.common.item.interactive.PokerodItem;
import com.github.flandre923.berrypouch.ModCommon;
import com.github.flandre923.berrypouch.event.FishingRodEventHandler;
import com.github.flandre923.berrypouch.helper.MarkedSlotsHelper;
import com.github.flandre923.berrypouch.helper.PouchDataHelper;
import com.github.flandre923.berrypouch.item.BerryPouch;
import com.github.flandre923.berrypouch.item.PokeBallGun;
import com.github.flandre923.berrypouch.item.pouch.BerryPouchManager;
import com.github.flandre923.berrypouch.item.pouch.PokeBallGunHelper;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CycleBaitRpcHandler {
    private CycleBaitRpcHandler() {
    }

    public static void handle(ServerPlayer player, boolean isMainHand, boolean isLeftCycle) {
        InteractionHand hand = isMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.getItem() instanceof PokeBallGun) {
            handleCyclePokeBallGun(player, heldStack, isLeftCycle);
            return;
        }
        if (FishingRodEventHandler.isCobblemonFishingRod(heldStack)) {
            handleCycleBaitRequest(player, heldStack, isLeftCycle);
            return;
        }
        player.sendSystemMessage(Component.translatable("message.berrypouch.not_holding_valid_item"), true);
    }

    private static void handleCyclePokeBallGun(ServerPlayer player, ItemStack gunStack, boolean isLeftCycle) {
        if (isLeftCycle) {
            PokeBallGunHelper.cyclePrev(gunStack);
        } else {
            PokeBallGunHelper.cycleNext(gunStack);
        }

        ItemStack selectedItem = PokeBallGunHelper.getSelectedItem(gunStack);

        if (!selectedItem.isEmpty()) {
            player.sendSystemMessage(
                    Component.translatable("message.berrypouch.switched_pokeball", selectedItem.getHoverName()),
                    true
            );
        } else {
            player.sendSystemMessage(
                    Component.translatable("message.berrypouch.slot_empty", PokeBallGunHelper.getSelectedIndex(gunStack) + 1),
                    true
            );
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, 0.5f, 1.5f);
    }

    private static void handleCycleBaitRequest(ServerPlayer player, ItemStack heldStack, boolean isLeftCycle) {
        Level level = player.level();

        AccessoriesCapability capability = AccessoriesCapability.get(player);
        if (capability == null) return;

        Optional<SlotEntryReference> pouchRefOpt = Optional.ofNullable(capability.getFirstEquipped(stack -> stack.getItem() instanceof BerryPouch));
        if (pouchRefOpt.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.berrypouch.need_pouch"), true);
            return;
        }

        ItemStack pouchStack = pouchRefOpt.get().stack();
        if (!(pouchStack.getItem() instanceof BerryPouch)) {
            return;
        }

        List<Integer> markedSlots = MarkedSlotsHelper.getMarkedSlots(pouchStack);
        boolean preferMarked = !markedSlots.isEmpty();

        SimpleContainer pouchItems = BerryPouchManager.getInventory(pouchStack, level);
        List<ItemStack> availableMarkedItemTypes = new ArrayList<>();
        if (preferMarked) {
            for (int markedIndex : markedSlots) {
                if (markedIndex >= 0 && markedIndex < pouchItems.getContainerSize()) {
                    ItemStack stackInSlot = pouchItems.getItem(markedIndex);
                    if (!stackInSlot.isEmpty() && FishingRodEventHandler.isCobblemonBerry(stackInSlot)) {
                        boolean alreadyAdded = false;
                        for (ItemStack existing : availableMarkedItemTypes) {
                            if (ItemStack.isSameItemSameComponents(existing, stackInSlot)) {
                                alreadyAdded = true;
                                break;
                            }
                        }
                        if (!alreadyAdded) {
                            availableMarkedItemTypes.add(stackInSlot.copy());
                        }
                    }
                }
            }
        }

        List<ItemStack> allAvailableItemTypes = new ArrayList<>();
        for (ItemStack stackInSlot : pouchItems.getItems()) {
            if (!stackInSlot.isEmpty() && FishingRodEventHandler.isCobblemonBerry(stackInSlot)) {
                boolean alreadyAdded = false;
                for (ItemStack existing : allAvailableItemTypes) {
                    if (ItemStack.isSameItemSameComponents(existing, stackInSlot)) {
                        alreadyAdded = true;
                        break;
                    }
                }
                if (!alreadyAdded) {
                    allAvailableItemTypes.add(stackInSlot.copy());
                }
            }
        }

        Item currentBaitItem = null;
        ItemStack currentBaitStackOnRod = PokerodItem.Companion.getBaitStackOnRod(heldStack);
        boolean returnedToPouch = false;
        if (!currentBaitStackOnRod.isEmpty()) {
            currentBaitItem = currentBaitStackOnRod.getItem();
            ItemStack baitToReturn = currentBaitStackOnRod.copy();

            for (int i = 0; i < pouchItems.getContainerSize(); i++) {
                ItemStack slotStack = pouchItems.getItem(i);
                if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(slotStack, baitToReturn)) {
                    int space = slotStack.getMaxStackSize() - slotStack.getCount();
                    if (space > 0) {
                        slotStack.grow(1);
                        pouchItems.setItem(i, slotStack);
                        returnedToPouch = true;
                        break;
                    }
                }
            }

            if (returnedToPouch) {
                PokerodItem.Companion.setBait(heldStack, ItemStack.EMPTY);
            } else {
                ItemStack remaining = pouchItems.addItem(baitToReturn);
                if (remaining.isEmpty()) {
                    returnedToPouch = true;
                } else {
                    ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY() + player.getEyeHeight(), player.getZ(), remaining);
                    itemEntity.setPickUpDelay(10);
                    level.addFreshEntity(itemEntity);
                    player.sendSystemMessage(Component.translatable("message.berrypouch.bait_dropped", Component.translatable(currentBaitItem.getDescriptionId())), true);
                }
                PokerodItem.Companion.setBait(heldStack, ItemStack.EMPTY);
            }
        }

        if (preferMarked && availableMarkedItemTypes.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.berrypouch.marked_bait_exhausted_switching_none"), true);
            PokerodItem.Companion.setBait(heldStack, ItemStack.EMPTY);
            if (returnedToPouch) {
                pouchItems.setChanged();
            }
            player.inventoryMenu.broadcastChanges();
            if (player.containerMenu != player.inventoryMenu) {
                player.containerMenu.broadcastChanges();
            }
            return;
        }

        List<ItemStack> finalAvailableBerryTypes;
        boolean cyclingMarked = false;

        if (preferMarked) {
            finalAvailableBerryTypes = availableMarkedItemTypes;
            cyclingMarked = true;
        } else if (!allAvailableItemTypes.isEmpty()) {
            finalAvailableBerryTypes = allAvailableItemTypes;
        } else {
            player.sendSystemMessage(Component.translatable("message.berrypouch.no_bait_in_pouch"), true);
            if (returnedToPouch) {
                pouchItems.setChanged();
            }
            return;
        }

        int currentIndex = currentBaitStackOnRod != null && !currentBaitStackOnRod.isEmpty()
                ? findBaitIndex(finalAvailableBerryTypes, currentBaitStackOnRod)
                : -1;
        if (currentIndex == -1 && !finalAvailableBerryTypes.isEmpty()) {
            currentIndex = isLeftCycle ? 0 : finalAvailableBerryTypes.size() - 1;
        } else if (finalAvailableBerryTypes.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.berrypouch.no_bait_in_pouch"), true);
            if (returnedToPouch) {
                pouchItems.setChanged();
            }
            return;
        }

        int nextIndex = isLeftCycle
                ? (currentIndex - 1 + finalAvailableBerryTypes.size()) % finalAvailableBerryTypes.size()
                : (currentIndex + 1) % finalAvailableBerryTypes.size();

        if (nextIndex < 0 || nextIndex >= finalAvailableBerryTypes.size()) {
            ModCommon.LOG.error("Error calculating next bait index. Current: {}, Next: {}, Size: {}", currentIndex, nextIndex, finalAvailableBerryTypes.size());
            if (returnedToPouch) {
                pouchItems.setChanged();
            }
            return;
        }

        ItemStack nextBaitStack = finalAvailableBerryTypes.get(nextIndex);
        Item nextBaitItem = nextBaitStack.getItem();
        boolean deducted = false;
        ItemStack baitToSet = ItemStack.EMPTY;
        if (cyclingMarked) {
            for (int markedIndex : markedSlots) {
                if (markedIndex >= 0 && markedIndex < pouchItems.getContainerSize()) {
                    ItemStack stackInSlot = pouchItems.getItem(markedIndex);
                    if (!stackInSlot.isEmpty() && ItemStack.isSameItemSameComponents(stackInSlot, nextBaitStack)) {
                        baitToSet = stackInSlot.copyWithCount(1);
                        stackInSlot.shrink(1);
                        pouchItems.setItem(markedIndex, stackInSlot);
                        deducted = true;
                        break;
                    }
                }
            }
        } else {
            for (int i = 0; i < pouchItems.getContainerSize(); i++) {
                ItemStack stackInSlot = pouchItems.getItem(i);
                if (!stackInSlot.isEmpty() && ItemStack.isSameItemSameComponents(stackInSlot, nextBaitStack)) {
                    baitToSet = stackInSlot.copyWithCount(1);
                    stackInSlot.shrink(1);
                    pouchItems.setItem(i, stackInSlot);
                    deducted = true;
                    break;
                }
            }
        }

        if (!deducted) {
            ModCommon.LOG.warn("Failed to deduct bait {} even though it was expected to be available.", nextBaitItem);
            player.sendSystemMessage(Component.translatable("message.berrypouch.berry_missing_after_selection"), true);

            if (returnedToPouch) {
                pouchItems.setChanged();
            }
            PokerodItem.Companion.setBait(heldStack, ItemStack.EMPTY);
            player.inventoryMenu.broadcastChanges();
            if (player.containerMenu != player.inventoryMenu) {
                player.containerMenu.broadcastChanges();
            }
            return;
        }

        PokerodItem.Companion.setBait(heldStack, baitToSet);
        PouchDataHelper.setLastUsedBait(pouchStack, nextBaitItem);
        pouchItems.setChanged();

        player.sendSystemMessage(
                Component.translatable("message.berrypouch.switched_bait", baitToSet.getHoverName()),
                true
        );
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, 0.5f, 1.5f);

        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) {
            player.containerMenu.broadcastChanges();
        }
    }

    private static int findBaitIndex(List<ItemStack> baitList, ItemStack target) {
        if (target == null || target.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < baitList.size(); i++) {
            if (ItemStack.isSameItemSameComponents(baitList.get(i), target)) {
                return i;
            }
        }
        return -1;
    }
}

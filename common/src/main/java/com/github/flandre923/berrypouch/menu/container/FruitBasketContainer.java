package com.github.flandre923.berrypouch.menu.container;

import com.github.flandre923.berrypouch.ModRegistries;
import com.github.flandre923.berrypouch.item.FruitBasketItem;
import com.github.flandre923.berrypouch.item.pouch.FruitBasketStorage;
import com.github.flandre923.berrypouch.menu.layout.FruitBasketLayout;
import com.github.flandre923.berrypouch.ui.declarative.DeclarativeStorageLayout;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FruitBasketContainer extends AbstractContainerMenu {
    private static final int BASKET_SIZE = FruitBasketLayout.BASKET_SLOT_COUNT;
    private static final int MAX_EXTRACT_PER_ACTION = 64;
    private static final TagKey<Item> APRICORN_TAG =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("berrypouch", "apricorns"));

    private final ItemStack basketStack;
    private final int openFlag;
    private final SimpleContainer basketDisplay = new SimpleContainer(BASKET_SIZE);
    private final List<Long> displayCounts = new ArrayList<>(BASKET_SIZE);

    public FruitBasketContainer(int containerId, Inventory playerInv, ItemStack basketStack, int openFlag) {
        super(ModRegistries.ModMenuTypes.FRUIT_BASKET_MENU.get(), containerId);
        this.basketStack = basketStack;
        this.openFlag = openFlag;
        while (displayCounts.size() < BASKET_SIZE) {
            displayCounts.add(0L);
        }

        rebuildDisplay();
        addBasketSlots();
        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);
    }

    public static FruitBasketContainer fromNetwork(int windowId, Inventory inv, FriendlyByteBuf buf) {
        int handFlag = buf.readInt();
        ItemStack stack = handFlag == 0 ? inv.player.getMainHandItem() : inv.player.getOffhandItem();
        return new FruitBasketContainer(windowId, inv, stack, handFlag);
    }

    public long getDisplayCount(int slot) {
        if (slot < 0 || slot >= displayCounts.size()) {
            return 0L;
        }
        return displayCounts.get(slot);
    }

    public ItemStack getBasketStack() {
        return basketStack;
    }

    public SimpleContainer getBasketDisplay() {
        return basketDisplay;
    }

    public int getOpenFlag() {
        return openFlag;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot clicked = this.slots.get(index);
        if (clicked == null || !clicked.hasItem()) {
            return ItemStack.EMPTY;
        }

        if (index < BASKET_SIZE) {
            ItemStack extracted = extractFromBasketSlot(index);
            if (extracted.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack toMove = extracted.copy();
            boolean moved = this.moveItemStackTo(toMove, BASKET_SIZE, this.slots.size(), true);
            int movedCount = extracted.getCount() - toMove.getCount();

            if (!moved || movedCount <= 0) {
                insertIntoBasket(extracted, extracted.getCount());
                return ItemStack.EMPTY;
            }

            if (!toMove.isEmpty()) {
                insertIntoBasket(toMove, toMove.getCount());
            }
//            return extracted.copyWithCount(movedCount);
            return ItemStack.EMPTY;
        }

        ItemStack playerStack = clicked.getItem();
        if (!isApricorn(playerStack)) {
            return ItemStack.EMPTY;
        }

        int moved = insertIntoBasket(playerStack, playerStack.getCount());
        if (moved <= 0) {
            return ItemStack.EMPTY;
        }

        clicked.setChanged();
        return playerStack.copyWithCount(moved);
    }

    @Override
    public boolean stillValid(Player player) {
        if (basketStack.isEmpty() || !(basketStack.getItem() instanceof FruitBasketItem)) {
            return false;
        }
        return ItemStack.matches(player.getMainHandItem(), basketStack) || ItemStack.matches(player.getOffhandItem(), basketStack);
    }

    private void rebuildDisplay() {
        for (int i = 0; i < BASKET_SIZE; i++) {
            basketDisplay.setItem(i, ItemStack.EMPTY);
            displayCounts.set(i, 0L);
        }

        Map<ResourceLocation, Long> items = FruitBasketStorage.getAll(basketStack);
        int index = 0;
        for (Map.Entry<ResourceLocation, Long> entry : items.entrySet()) {
            if (index >= BASKET_SIZE) {
                break;
            }
            Item item = BuiltInRegistries.ITEM.get(entry.getKey());
            if (item == null || item == ItemStack.EMPTY.getItem()) {
                continue;
            }
            basketDisplay.setItem(index, new ItemStack(item));
            displayCounts.set(index, entry.getValue());
            index++;
        }
    }

    private void addBasketSlots() {
        for (DeclarativeStorageLayout.SlotSpec slotSpec : FruitBasketLayout.STORAGE.slotsByRole(DeclarativeStorageLayout.SlotRole.POUCH_BERRY)) {
            addSlot(new Slot(basketDisplay, slotSpec.index(), slotSpec.x(), slotSpec.y()) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return isApricorn(stack);
                }

                @Override
                public boolean mayPickup(Player player) {
                    return this.hasItem();
                }

                @Override
                public ItemStack remove(int amount) {
                    return extractFromBasketSlot(this.index);
                }

                @Override
                public ItemStack getItem() {
                    return basketDisplay.getItem(this.index);
                }

                @Override
                public boolean hasItem() {
                    return !basketDisplay.getItem(this.index).isEmpty() && getDisplayCount(this.index) > 0L;
                }

                @Override
                public void setByPlayer(ItemStack stack, ItemStack previous) {
                    if (!stack.isEmpty()) {
                        insertIntoBasket(stack, stack.getCount());
                    }
                }

                @Override
                public ItemStack safeInsert(ItemStack stack) {
                    return safeInsert(stack, stack.getCount());
                }

                @Override
                public ItemStack safeInsert(ItemStack stack, int increment) {
                    if (stack.isEmpty() || !isApricorn(stack) || increment <= 0) {
                        return stack;
                    }
                    int moved = insertIntoBasket(stack, Math.min(increment, stack.getCount()));
                    return moved > 0 ? stack : stack;
                }
            });
        }
    }

    private static boolean isApricorn(ItemStack stack) {
        return !stack.isEmpty() && stack.is(APRICORN_TAG);
    }

    private ItemStack extractFromBasketSlot(int slot) {
        ItemStack marker = basketDisplay.getItem(slot);
        if (marker.isEmpty()) {
            return ItemStack.EMPTY;
        }

        long available = displayCounts.get(slot);
        if (available <= 0L) {
            return ItemStack.EMPTY;
        }

        int take = (int) Math.min(available, MAX_EXTRACT_PER_ACTION);
        if (take <= 0) {
            return ItemStack.EMPTY;
        }

        Item item = marker.getItem();
        long current = FruitBasketStorage.get(basketStack, item);
        if (current <= 0L) {
            return ItemStack.EMPTY;
        }

        long next = Math.max(0L, current - take);
        FruitBasketStorage.set(basketStack, item, next);
        rebuildDisplay();

        return new ItemStack(item, take);
    }

    private int insertIntoBasket(ItemStack source, int amount) {
        if (!isApricorn(source) || amount <= 0) {
            return 0;
        }
        int moved = Math.min(amount, source.getCount());
        if (moved <= 0) {
            return 0;
        }

        long inserted = FruitBasketStorage.add(basketStack, source.getItem(), moved);
        int actualMoved = (int) Math.min(inserted, moved);
        if (actualMoved > 0) {
            source.shrink(actualMoved);
            rebuildDisplay();
        }
        return actualMoved;
    }

    private void addPlayerInventory(Inventory playerInv) {
        for (DeclarativeStorageLayout.SlotSpec slotSpec : FruitBasketLayout.STORAGE.slotsByRole(DeclarativeStorageLayout.SlotRole.PLAYER_INVENTORY)) {
            addSlot(new Slot(playerInv, slotSpec.index(), slotSpec.x(), slotSpec.y()));
        }
    }

    private void addPlayerHotbar(Inventory playerInv) {
        for (DeclarativeStorageLayout.SlotSpec slotSpec : FruitBasketLayout.STORAGE.slotsByRole(DeclarativeStorageLayout.SlotRole.PLAYER_HOTBAR)) {
            addSlot(new Slot(playerInv, slotSpec.index(), slotSpec.x(), slotSpec.y()));
        }
    }
}

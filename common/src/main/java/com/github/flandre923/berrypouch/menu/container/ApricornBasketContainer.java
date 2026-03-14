package com.github.flandre923.berrypouch.menu.container;

import com.github.flandre923.berrypouch.ModRegistries;
import com.github.flandre923.berrypouch.helper.TransferPlanner;
import com.github.flandre923.berrypouch.item.ApricornBasketItem;
import com.github.flandre923.berrypouch.item.pouch.ApricornSlotMapping;
import com.github.flandre923.berrypouch.item.pouch.ApricornBasketStorage;
import com.github.flandre923.berrypouch.menu.ae.AEBaseMenu;
import com.github.flandre923.berrypouch.menu.ae.SlotSemantics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApricornBasketContainer extends AEBaseMenu {
    private static final int BASKET_COLUMNS = ApricornSlotMapping.size();
    private static final int BASKET_ROWS = 1;
    private static final int BASKET_SIZE = BASKET_COLUMNS * BASKET_ROWS;
    private static final int MAX_EXTRACT_PER_ACTION = 64;
    private static final TagKey<Item> APRICORN_TAG =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("berrypouch", "apricorns"));

    private final ItemStack basketStack;
    private final int openFlag;
    private final SimpleContainer basketDisplay = new SimpleContainer(BASKET_SIZE);
    private final List<Long> displayCounts = new ArrayList<>(BASKET_SIZE);

    public ApricornBasketContainer(int containerId, Inventory playerInv, ItemStack basketStack, int openFlag) {
        super(ModRegistries.ModMenuTypes.APRICORN_BASKET_MENU.get(), containerId, playerInv);
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

    public static ApricornBasketContainer fromNetwork(int windowId, Inventory inv, FriendlyByteBuf buf) {
        int handFlag = buf.readInt();
        ItemStack stack;
        if (handFlag == 0) {
            stack = inv.player.getItemInHand(InteractionHand.MAIN_HAND);
        } else if (handFlag == 1) {
            stack = inv.player.getItemInHand(InteractionHand.OFF_HAND);
        } else {
            stack = ItemStack.EMPTY;
        }
        return new ApricornBasketContainer(windowId, inv, stack, handFlag);
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
            transferFromBasketSlot(index, MAX_EXTRACT_PER_ACTION, createPlayerInventoryTargets());
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
        if (basketStack.isEmpty() || !(basketStack.getItem() instanceof ApricornBasketItem)) {
            return false;
        }
        return ItemStack.matches(player.getMainHandItem(), basketStack) || ItemStack.matches(player.getOffhandItem(), basketStack);
    }

    private void rebuildDisplay() {
        for (int i = 0; i < BASKET_SIZE; i++) {
            basketDisplay.setItem(i, ItemStack.EMPTY);
            displayCounts.set(i, 0L);
        }

        Map<ResourceLocation, Long> items = ApricornBasketStorage.getAll(basketStack);
        long[] fixedCounts = new long[BASKET_SIZE];
        Item[] markers = new Item[BASKET_SIZE];

        for (Map.Entry<ResourceLocation, Long> entry : items.entrySet()) {
            Item item = BuiltInRegistries.ITEM.get(entry.getKey());
            if (item == null || item == Items.AIR) {
                continue;
            }
            int fixedSlot = ApricornSlotMapping.getSlotIndex(entry.getKey());
            if (fixedSlot < 0 || fixedSlot >= BASKET_SIZE) {
                continue;
            }

            long count = Math.max(0L, entry.getValue());
            if (count <= 0L) {
                continue;
            }

            fixedCounts[fixedSlot] = saturatingAdd(fixedCounts[fixedSlot], count);
            if (markers[fixedSlot] == null) {
                markers[fixedSlot] = item;
            }
        }

        for (int i = 0; i < BASKET_SIZE; i++) {
            long count = fixedCounts[i];
            if (count <= 0L) {
                continue;
            }

            Item marker = markers[i];
            if (marker == null || marker == Items.AIR) {
                marker = ApricornSlotMapping.getExpectedItem(i);
            }
            if (marker == null || marker == Items.AIR) {
                continue;
            }

            basketDisplay.setItem(i, new ItemStack(marker));
            displayCounts.set(i, count);
        }
    }

    private void addBasketSlots() {
        for (int slotIndex = 0; slotIndex < BASKET_SIZE; slotIndex++) {
            addSlot(new Slot(basketDisplay, slotIndex, 0, 0) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    if (!isApricorn(stack)) {
                        return false;
                    }
                    return ApricornSlotMapping.matchesSlot(this.index, stack.getItem());
                }

                @Override
                public boolean mayPickup(Player player) {
                    return this.hasItem();
                }

                @Override
                public ItemStack remove(int amount) {
                    return extractFromBasketSlot(this.index, amount);
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
                    insertIntoBasket(stack, Math.min(increment, stack.getCount()));
                    return stack;
                }
            }, SlotSemantics.BASKET_STORAGE);
        }
    }

    private static boolean isApricorn(ItemStack stack) {
        return !stack.isEmpty() && stack.is(APRICORN_TAG);
    }

    private ItemStack extractFromBasketSlot(int slot, int maxExtract) {
        ItemStack marker = basketDisplay.getItem(slot);
        if (marker.isEmpty()) {
            return ItemStack.EMPTY;
        }

        long available = displayCounts.get(slot);
        if (available <= 0L) {
            return ItemStack.EMPTY;
        }

        int take = (int) Math.min(available, Math.max(0, maxExtract));
        if (take <= 0) {
            return ItemStack.EMPTY;
        }

        Item item = marker.getItem();
        long current = ApricornBasketStorage.get(basketStack, item);
        if (current <= 0L) {
            return ItemStack.EMPTY;
        }

        long next = Math.max(0L, current - take);
        ApricornBasketStorage.set(basketStack, item, next);
        rebuildDisplay();

        return new ItemStack(item, take);
    }

    private List<TransferPlanner.Target<ItemStack>> createPlayerInventoryTargets() {
        return List.of(this::moveToPlayerInventory);
    }

    private boolean transferFromBasketSlot(int slot, int maxExtract, List<TransferPlanner.Target<ItemStack>> targets) {
        ItemStack extracted = extractFromBasketSlot(slot, maxExtract);
        if (extracted.isEmpty()) {
            return false;
        }

        ItemStack itemType = extracted.copyWithCount(1);
        int remaining = TransferPlanner.transfer(itemType, extracted.getCount(), targets);

        if (remaining > 0) {
            insertIntoBasket(itemType.copyWithCount(remaining), remaining);
        }
        return remaining < extracted.getCount();
    }

    private int moveToPlayerInventory(ItemStack itemType, int amount) {
        ItemStack toMove = itemType.copyWithCount(amount);
        this.moveItemStackTo(toMove, BASKET_SIZE, this.slots.size(), true);
        return amount - toMove.getCount();
    }

    private int insertIntoBasket(ItemStack source, int amount) {
        if (!isApricorn(source) || amount <= 0) {
            return 0;
        }
        if (ApricornSlotMapping.getSlotIndex(source.getItem()) < 0) {
            return 0;
        }

        int moved = Math.min(amount, source.getCount());
        if (moved <= 0) {
            return 0;
        }

        long inserted = ApricornBasketStorage.add(basketStack, source.getItem(), moved);
        int actualMoved = (int) Math.min(inserted, moved);
        if (actualMoved > 0) {
            source.shrink(actualMoved);
            rebuildDisplay();
        }
        return actualMoved;
    }

    public ItemStack getFixedSlotPreview(int slot) {
        Item expected = ApricornSlotMapping.getExpectedItem(slot);
        if (expected == null || expected == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(expected);
    }

    private static long saturatingAdd(long a, long b) {
        if (Long.MAX_VALUE - a < b) {
            return Long.MAX_VALUE;
        }
        return a + b;
    }

    private void addPlayerInventory(Inventory playerInv) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 0, 0), SlotSemantics.PLAYER_INVENTORY);
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInv) {
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(playerInv, i, 0, 0), SlotSemantics.PLAYER_HOTBAR);
        }
    }
}

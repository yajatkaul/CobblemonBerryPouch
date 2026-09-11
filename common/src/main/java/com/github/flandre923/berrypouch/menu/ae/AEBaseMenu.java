package com.github.flandre923.berrypouch.menu.ae;

import com.github.flandre923.berrypouch.menu.slot.AppEngSlot;
import com.github.flandre923.berrypouch.menu.slot.DisabledSlot;
import com.github.flandre923.berrypouch.menu.slot.FakeSlot;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AEBaseMenu extends AbstractContainerMenu {
    private final Inventory playerInventory;
    @Nullable
    private final Object host;

    private final Set<Integer> lockedPlayerInventorySlots = new HashSet<>();
    private final Map<Slot, SlotSemantic> semanticBySlot = new HashMap<>();
    private final Map<SlotSemantic, List<Slot>> slotsBySemantic = new HashMap<>();
    private final Set<Slot> clientSideSlots = new HashSet<>();

    private boolean menuValid = true;

    protected AEBaseMenu(MenuType<?> menuType, int id, Inventory playerInventory) {
        this(menuType, id, playerInventory, null);
    }

    protected AEBaseMenu(MenuType<?> menuType, int id, Inventory playerInventory, @Nullable Object host) {
        super(menuType, id);
        this.playerInventory = playerInventory;
        this.host = host;
    }

    public Inventory getPlayerInventory() {
        return playerInventory;
    }

    @Nullable
    public Object getTarget() {
        return host;
    }

    public Player getPlayer() {
        return playerInventory.player;
    }

    public void lockPlayerInventorySlot(int invSlot) {
        if (invSlot < 0 || invSlot >= playerInventory.getContainerSize()) {
            throw new IllegalArgumentException("Cannot lock player inventory slot " + invSlot);
        }
        lockedPlayerInventorySlots.add(invSlot);
    }

    public boolean isPlayerInventorySlotLocked(int invSlot) {
        return lockedPlayerInventorySlots.contains(invSlot);
    }

    protected final void createPlayerInventorySlots(Inventory playerInventory) {
        if (!getSlots(SlotSemantics.PLAYER_INVENTORY).isEmpty()) {
            throw new IllegalStateException("Player inventory was already created");
        }

        for (int i = 0; i < playerInventory.items.size(); i++) {
            Slot slot = lockedPlayerInventorySlots.contains(i)
                    ? new DisabledSlot(playerInventory, i)
                    : new Slot(playerInventory, i, 0, 0);
            SlotSemantic semantic = i < Inventory.getSelectionSize()
                    ? SlotSemantics.PLAYER_HOTBAR
                    : SlotSemantics.PLAYER_INVENTORY;
            addSlot(slot, semantic);
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType == ClickType.SWAP && isPlayerInventorySlotLocked(Inventory.SLOT_OFFHAND)) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    protected Slot addSlot(Slot slot, SlotSemantic semantic) {
        Slot added = this.addSlot(slot);
        semanticBySlot.put(added, semantic);
        slotsBySemantic.computeIfAbsent(semantic, key -> new ArrayList<>()).add(added);
        return added;
    }

    public Slot addClientSideSlot(Slot slot, SlotSemantic semantic) {
        if (!isClientSide()) {
            throw new IllegalStateException("Can only add client-side slots on client");
        }
        if (!clientSideSlots.add(slot)) {
            throw new IllegalStateException("Client-side slot already exists");
        }

        slot.index = slots.size();
        slots.add(slot);

        if (semantic != null) {
            semanticBySlot.put(slot, semantic);
            slotsBySemantic.computeIfAbsent(semantic, key -> new ArrayList<>()).add(slot);
        }
        return slot;
    }

    public void removeClientSideSlot(Slot slot) {
        if (slots.get(slot.index) != slot) {
            throw new IllegalStateException("Trying to remove slot which isn't currently in menu");
        }
        if (!clientSideSlots.remove(slot)) {
            throw new IllegalStateException("Trying to remove non-client-side slot");
        }

        slots.remove(slot.index);
        semanticBySlot.remove(slot);
        slotsBySemantic.values().forEach(list -> list.remove(slot));
        for (int i = slot.index; i < slots.size(); i++) {
            slots.get(i).index = i;
        }
    }

    public boolean isClientSideSlot(@Nullable Slot slot) {
        return slot != null && clientSideSlots.contains(slot);
    }

    public List<Slot> getSlots(SlotSemantic semantic) {
        return slotsBySemantic.getOrDefault(semantic, List.of());
    }

    @Nullable
    public SlotSemantic getSlotSemantic(Slot slot) {
        return semanticBySlot.get(slot);
    }

    @Override
    protected Slot addSlot(Slot newSlot) {
        if (newSlot instanceof AppEngSlot appEngSlot) {
            appEngSlot.setMenu(this);
        }
        return super.addSlot(newSlot);
    }

    @Override
    public void initializeContents(int stateId, List<ItemStack> items, ItemStack carried) {
        for (int i = 0; i < items.size(); ++i) {
            Slot slot = this.getSlot(i);
            if (slot instanceof AppEngSlot aeSlot) {
                aeSlot.initialize(items.get(i));
            } else {
                slot.set(items.get(i));
            }
        }

        this.setCarried(carried);
    }

    protected int getQuickMovePriority(Slot slot) {
        SlotSemantic semantic = getSlotSemantic(slot);
        return semantic == null ? 0 : semantic.quickMovePriority();
    }

    public boolean isPlayerSideSlot(Slot slot) {
        if (slot.container == playerInventory) {
            return true;
        }
        SlotSemantic semantic = semanticBySlot.get(slot);
        return semantic != null && semantic.playerSide();
    }

    public void hideSlot(String semanticId) {
        SlotSemantic semantic = SlotSemantics.get(semanticId);
        if (semantic == null) {
            return;
        }
        if (canSlotsBeHidden(semantic)) {
            for (Slot slot : getSlots(semantic)) {
                if (slot instanceof AppEngSlot appEngSlot) {
                    appEngSlot.setSlotEnabled(false);
                }
            }
        }
    }

    protected boolean canSlotsBeHidden(SlotSemantic semantic) {
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int idx) {
        if (isClientSide()) {
            return ItemStack.EMPTY;
        }
        if (idx < 0 || idx >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot clickSlot = this.slots.get(idx);
        if (!clickSlot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }

        ItemStack stackToMove = clickSlot.getItem();
        if (stackToMove.isEmpty()) {
            return ItemStack.EMPTY;
        }

        boolean fromPlayerSide = isPlayerSideSlot(clickSlot);
        if (fromPlayerSide) {
            int transferred = transferStackToMenu(stackToMove.copy());
            if (transferred > 0) {
                clickSlot.remove(transferred);
            }
        }

        stackToMove = clickSlot.getItem();
        if (stackToMove.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack original = stackToMove.copy();
        stackToMove = quickMoveToOtherSlots(stackToMove, fromPlayerSide);

        if (!ItemStack.matches(original, stackToMove)) {
            clickSlot.setByPlayer(stackToMove.isEmpty() ? ItemStack.EMPTY : stackToMove);
        }
        return ItemStack.EMPTY;
    }

    private ItemStack quickMoveToOtherSlots(ItemStack stackToMove, boolean fromPlayerSide) {
        List<Slot> destinationSlots = getQuickMoveDestinationSlots(stackToMove, fromPlayerSide);

        if (destinationSlots.isEmpty() && fromPlayerSide) {
            for (Slot candidate : this.slots) {
                if (candidate instanceof FakeSlot && !isPlayerSideSlot(candidate)) {
                    ItemStack destination = candidate.getItem();
                    if (ItemStack.isSameItemSameComponents(destination, stackToMove)) {
                        break;
                    } else if (destination.isEmpty()) {
                        candidate.set(stackToMove.copy());
                        this.broadcastChanges();
                        break;
                    }
                }
            }
            return stackToMove;
        }

        for (Slot destination : destinationSlots) {
            if (destination.hasItem() && (stackToMove = destination.safeInsert(stackToMove)).isEmpty()) {
                return stackToMove;
            }
        }
        for (Slot destination : destinationSlots) {
            if (!destination.hasItem() && (stackToMove = destination.safeInsert(stackToMove)).isEmpty()) {
                return stackToMove;
            }
        }
        return stackToMove;
    }

    protected List<Slot> getQuickMoveDestinationSlots(ItemStack stackToMove, boolean fromPlayerSide) {
        List<Slot> destinationSlots = new ArrayList<>();
        for (Slot candidate : this.slots) {
            if (isValidQuickMoveDestination(candidate, stackToMove, fromPlayerSide)) {
                destinationSlots.add(candidate);
            }
        }
        destinationSlots.sort(Comparator.comparingInt(this::getQuickMovePriority).reversed());
        return destinationSlots;
    }

    protected boolean isValidQuickMoveDestination(Slot candidateSlot, ItemStack stackToMove, boolean fromPlayerSide) {
        return isPlayerSideSlot(candidateSlot) != fromPlayerSide
                && !(candidateSlot instanceof FakeSlot)
                && candidateSlot.mayPlace(stackToMove);
    }

    protected int transferStackToMenu(ItemStack input) {
        return 0;
    }

    public void swapSlotContents(int slotA, int slotB) {
        Slot a = this.getSlot(slotA);
        Slot b = this.getSlot(slotB);
        if (a == null || b == null) {
            return;
        }

        ItemStack isA = a.getItem();
        ItemStack isB = b.getItem();
        if (isA.isEmpty() && isB.isEmpty()) {
            return;
        }
        if (!isA.isEmpty() && !a.mayPickup(this.getPlayerInventory().player)) {
            return;
        }
        if (!isB.isEmpty() && !b.mayPickup(this.getPlayerInventory().player)) {
            return;
        }
        if (!isB.isEmpty() && !a.mayPlace(isB)) {
            return;
        }
        if (!isA.isEmpty() && !b.mayPlace(isA)) {
            return;
        }

        ItemStack testA = isB.isEmpty() ? ItemStack.EMPTY : isB.copy();
        ItemStack testB = isA.isEmpty() ? ItemStack.EMPTY : isA.copy();

        if (!testA.isEmpty() && testA.getCount() > a.getMaxStackSize()) {
            if (!testB.isEmpty()) {
                return;
            }
            int totalA = testA.getCount();
            testA.setCount(a.getMaxStackSize());
            testB = testA.copy();
            testB.setCount(totalA - testA.getCount());
        }

        if (!testB.isEmpty() && testB.getCount() > b.getMaxStackSize()) {
            if (!testA.isEmpty()) {
                return;
            }
            int totalB = testB.getCount();
            testB.setCount(b.getMaxStackSize());
            testA = testB.copy();
            testA.setCount(totalB - testB.getCount());
        }

        a.set(testA);
        b.set(testB);
    }

    public void onSlotChange(Slot slot) {
    }

    public boolean isValidForSlot(Slot slot, ItemStack stack) {
        return true;
    }

    public boolean isValidMenu() {
        return menuValid;
    }

    public void setValidMenu(boolean valid) {
        this.menuValid = valid;
    }

    public boolean isClientSide() {
        return getPlayer().level().isClientSide();
    }

    protected boolean isServerSide() {
        return !isClientSide();
    }
}

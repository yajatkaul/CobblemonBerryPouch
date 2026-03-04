package com.github.flandre923.berrypouch.menu.container;

import com.cobblemon.mod.common.item.PokeBallItem;
import com.github.flandre923.berrypouch.ModRegistries;
import com.github.flandre923.berrypouch.item.PokeBallGun;
import com.github.flandre923.berrypouch.item.pouch.PokeBallGunHelper;
import com.github.flandre923.berrypouch.item.pouch.PokeBallGunInventory;
import com.github.flandre923.berrypouch.menu.ae.AEBaseMenu;
import com.github.flandre923.berrypouch.menu.ae.SlotSemantics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PokeBallGunContainer extends AEBaseMenu {
    private static final int GUN_SLOTS = 9;

    private final ItemStack gunStack;
    private final PokeBallGunInventory gunInventory;
    private int selectedIndex;

    public PokeBallGunContainer(int containerId, Inventory playerInv, ItemStack gunStack) {
        super(ModRegistries.ModMenuTypes.POKEBALL_GUN_MENU.get(), containerId, playerInv);
        this.gunStack = gunStack;
        this.gunInventory = new PokeBallGunInventory(gunStack, GUN_SLOTS);

        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return PokeBallGunHelper.getSelectedIndex(gunStack);
            }

            @Override
            public void set(int value) {
                selectedIndex = value;
                PokeBallGunHelper.updateSelectedItemId(gunStack);
            }
        });

        addGunInventory();
        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);
    }

    public static PokeBallGunContainer fromNetwork(int windowId, Inventory inv, FriendlyByteBuf buf) {
        return new PokeBallGunContainer(windowId, inv, findGunStack(inv));
    }

    private static ItemStack findGunStack(Inventory inv) {
        ItemStack main = inv.player.getMainHandItem();
        if (main.getItem() instanceof PokeBallGun) {
            return main;
        }

        ItemStack offhand = inv.player.getOffhandItem();
        if (offhand.getItem() instanceof PokeBallGun) {
            return offhand;
        }

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof PokeBallGun) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index < GUN_SLOTS) {
                if (!this.moveItemStackTo(stackInSlot, GUN_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (PokeBallSlot.isPokeBall(stackInSlot)) {
                    if (!this.moveItemStackTo(stackInSlot, 0, GUN_SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            PokeBallGunHelper.updateSelectedItemId(gunStack);
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return !gunStack.isEmpty();
    }

    private void addGunInventory() {
        for (int i = 0; i < GUN_SLOTS; i++) {
            addSlot(new PokeBallSlot(gunInventory, i, 0, 0), SlotSemantics.GUN_AMMO);
        }
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

    public class PokeBallSlot extends Slot {
        public PokeBallSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isPokeBall(stack);
        }

        public static boolean isPokeBall(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            return stack.getItem() instanceof PokeBallItem;
        }
    }

    public SimpleContainer getGunInventory() {
        return this.gunInventory;
    }

    public ItemStack getGunStack() {
        return this.gunStack;
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }
}
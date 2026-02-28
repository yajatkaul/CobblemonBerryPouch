package com.github.flandre923.berrypouch.item;

import com.github.flandre923.berrypouch.item.pouch.FruitBasketStorage;
import com.github.flandre923.berrypouch.menu.container.FruitBasketContainer;
import dev.architectury.registry.menu.ExtendedMenuProvider;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FruitBasketItem extends Item {
    private static final int MAX_BFS_BLOCKS = 192;
    private static final int SEARCH_RADIUS = 2;
    private static final int MAX_HARVESTED_FRUIT_BLOCKS = 64;
    private static final TagKey<Item> APRICORN_TAG =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("berrypouch", "apricorns"));

    public FruitBasketItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer) {
            openGui(serverPlayer, hand);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            boolean handled = handleRightClickBlock(serverPlayer, context.getHand(), context.getClickedPos());
            if (handled) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    public static boolean handleRightClickBlock(ServerPlayer player, InteractionHand hand, BlockPos origin) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof FruitBasketItem basketItem)) {
            return false;
        }

        if (player.isShiftKeyDown()) {
            openGui(player, hand);
            return true;
        }

        ServerLevel level = player.serverLevel();
        if (!isApricornFruitBlock(level.getBlockState(origin))) {
            return false;
        }

        long inserted = basketItem.harvestChainApricorns(level, player, stack, origin);
        if (inserted > 0L) {
            level.playSound(null, origin, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 0.8f, 1.0f);
            return true;
        }
        return false;
    }

    public long getStoredCount(ItemStack basket, Item item) {
        return FruitBasketStorage.get(basket, item);
    }

    public static boolean onPickupItem(ItemEntity itemEntity, Player player) {
        ItemStack itemStack = itemEntity.getItem();
        if (itemStack.isEmpty() || !isApricornItem(itemStack)) {
            return false;
        }

        int openBasketSlot = getOpenBasketSlot(player);
        if (openBasketSlot >= 0) {
            return false;
        }

        if (tryInsertIntoBasket(itemStack, player)) {
            if (!player.level().isClientSide) {
                player.level().playSound(
                        null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ITEM_PICKUP,
                        SoundSource.PLAYERS,
                        0.2F,
                        ((player.level().random.nextFloat() - player.level().random.nextFloat()) * 0.7F + 1.0F) * 2.0F
                );
            }
            if (itemStack.isEmpty() || itemStack.getCount() == 0) {
                itemEntity.discard();
            }
            return true;
        }
        return false;
    }

    private static boolean tryInsertIntoBasket(ItemStack stack, Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (invStack.getItem() instanceof FruitBasketItem) {
                long inserted = FruitBasketStorage.add(invStack, stack.getItem(), stack.getCount());
                int moved = (int) Math.min(inserted, stack.getCount());
                if (moved > 0) {
                    stack.shrink(moved);
                    return true;
                }
            }
        }
        return false;
    }

    private static int getOpenBasketSlot(Player player) {
        if (!(player.containerMenu instanceof FruitBasketContainer container)) {
            return -1;
        }
        ItemStack containerStack = container.getBasketStack();
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (ItemStack.matches(inventory.getItem(i), containerStack)) {
                return i;
            }
        }
        return -1;
    }

    public void setStoredCount(ItemStack basket, Item item, long amount) {
        FruitBasketStorage.set(basket, item, amount);
    }

    private long harvestChainApricorns(ServerLevel level, Player player, ItemStack basketStack, BlockPos start) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> queued = new HashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start);
        queued.add(start);

        long totalInserted = 0L;
        int harvestedFruitBlocks = 0;

        while (!queue.isEmpty() && visited.size() < MAX_BFS_BLOCKS && harvestedFruitBlocks < MAX_HARVESTED_FRUIT_BLOCKS) {
            BlockPos pos = queue.poll();
            queued.remove(pos);
            if (!visited.add(pos)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (!isApricornFruitBlock(state)) {
                continue;
            }

            if (isRipe(state)) {
                List<ItemStack> drops = Block.getDrops(state, level, pos, null, player, ItemStack.EMPTY);
                for (ItemStack drop : drops) {
                    if (drop.isEmpty() || !isApricornItem(drop)) {
                        continue;
                    }
                    long inserted = FruitBasketStorage.add(basketStack, drop.getItem(), drop.getCount());
                    totalInserted += inserted;
                }
                level.setBlock(pos, toUnripeState(state), Block.UPDATE_CLIENTS);
                harvestedFruitBlocks++;

                if (harvestedFruitBlocks >= MAX_HARVESTED_FRUIT_BLOCKS) {
                    break;
                }
            }

            for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
                for (int dy = -SEARCH_RADIUS; dy <= SEARCH_RADIUS; dy++) {
                    for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) {
                            continue;
                        }

                        BlockPos next = pos.offset(dx, dy, dz);
                        if (visited.contains(next) || queued.contains(next)) {
                            continue;
                        }
                        if (!isApricornFruitBlock(level.getBlockState(next))) {
                            continue;
                        }

                        queue.add(next);
                        queued.add(next);
                    }
                }
            }
        }

        return totalInserted;
    }

    private static boolean isApricornFruitBlock(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String path = id.getPath();
        return id.getNamespace().equals("cobblemon") && path.contains("apricorn") && !path.contains("leaves");
    }

    private static boolean isApricornItem(ItemStack stack) {
        return stack.is(APRICORN_TAG);
    }

    private static boolean isRipe(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            String name = property.getName();
            if (property instanceof BooleanProperty boolProperty && isRipeBooleanProperty(name)) {
                if (Boolean.TRUE.equals(state.getValue(boolProperty))) {
                    return true;
                }
            }
            if (property instanceof IntegerProperty intProperty && isRipeIntegerProperty(name)) {
                int value = state.getValue(intProperty);
                if (value >= intProperty.getPossibleValues().stream().max(Integer::compareTo).orElse(0)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static BlockState toUnripeState(BlockState state) {
        BlockState result = state;
        for (Property<?> property : state.getProperties()) {
            String name = property.getName();
            if (property instanceof BooleanProperty boolProperty && isRipeBooleanProperty(name)) {
                result = result.setValue(boolProperty, false);
            }
            if (property instanceof IntegerProperty intProperty && isRipeIntegerProperty(name)) {
                int min = intProperty.getPossibleValues().stream().min(Integer::compareTo).orElse(0);
                result = result.setValue(intProperty, min);
            }
        }
        return result;
    }

    private static boolean isRipeBooleanProperty(String name) {
        return name.equals("ripe") || name.equals("has_fruit") || name.equals("fruiting") || name.equals("berries");
    }

    private static boolean isRipeIntegerProperty(String name) {
        return name.equals("age") || name.equals("fruit") || name.equals("stage") || name.equals("berries");
    }

    private static void openGui(ServerPlayer player, InteractionHand hand) {
        MenuRegistry.openExtendedMenu(player, new ExtendedMenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("container.berrypouch.fruit_basket");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player p) {
                ItemStack basket = p.getItemInHand(hand);
                return new FruitBasketContainer(containerId, inventory, basket, hand == InteractionHand.MAIN_HAND ? 0 : 1);
            }

            @Override
            public void saveExtraData(FriendlyByteBuf buf) {
                buf.writeInt(hand == InteractionHand.MAIN_HAND ? 0 : 1);
            }
        });
    }
}

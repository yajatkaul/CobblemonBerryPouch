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
        Level level = context.getLevel();
        ItemStack basketStack = context.getItemInHand();
        InteractionHand hand = context.getHand();

        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) {
                openGui(serverPlayer, hand);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        BlockPos origin = context.getClickedPos();
        if (!isApricornLeaves(serverLevel.getBlockState(origin))) {
            return InteractionResult.PASS;
        }

        long inserted = harvestChainApricorns(serverLevel, player, basketStack, origin);
        if (inserted > 0L) {
            serverLevel.playSound(null, origin, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 0.8f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public long getStoredCount(ItemStack basket, Item item) {
        return FruitBasketStorage.get(basket, item);
    }

    public void setStoredCount(ItemStack basket, Item item, long amount) {
        FruitBasketStorage.set(basket, item, amount);
    }

    private long harvestChainApricorns(ServerLevel level, Player player, ItemStack basketStack, BlockPos start) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start);

        long totalInserted = 0L;

        while (!queue.isEmpty() && visited.size() < MAX_BFS_BLOCKS) {
            BlockPos pos = queue.poll();
            if (!visited.add(pos)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (!isApricornLeaves(state)) {
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
            }

            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (!visited.contains(next) && isApricornLeaves(level.getBlockState(next))) {
                    queue.add(next);
                }
            }
        }

        return totalInserted;
    }

    private static boolean isApricornLeaves(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String path = id.getPath();
        return id.getNamespace().equals("cobblemon") && path.contains("apricorn") && path.contains("leaves");
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

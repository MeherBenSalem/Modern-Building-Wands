package com.nightbeam.reconstructedwands.wand.supply;

import com.nightbeam.reconstructedwands.config.ServerConfig;
import com.nightbeam.reconstructedwands.config.WandsConfig;
import com.nightbeam.reconstructedwands.wand.WandSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Where a wand takes its blocks from, in order: loose stacks in the main inventory, shulker boxes and bundles, the
 * bound container, the Ender Chest, and last the hotbar and offhand (so the stacks you hold are kept longest).
 * Only plain stacks are used: a named or otherwise customised block item is never placed by a wand.
 */
public final class Supply {
    private interface Source {
        int count(ItemStack plain);

        /** Takes up to {@code amount}; returns how many were taken. */
        int take(ItemStack plain, int amount);
    }

    private final Player player;
    private final boolean infinite;
    private final List<Source> sources = new ArrayList<>();
    private final Map<Item, Integer> counts = new HashMap<>();

    private Supply(Player player, WandSettings settings) {
        this.player = player;
        this.infinite = player.isCreative();
        if (infinite) {
            return;
        }
        ServerConfig config = WandsConfig.server();
        Inventory inventory = player.getInventory();
        List<ItemStack> items = inventory.items;
        List<ItemStack> main = items.subList(Inventory.getSelectionSize(), items.size());
        List<ItemStack> hotbar = items.subList(0, Inventory.getSelectionSize());
        ItemStack offhand = inventory.offhand.get(0);

        sources.add(loose(main));
        for (List<ItemStack> part : List.of(main, hotbar, List.of(offhand))) {
            for (ItemStack stack : part) {
                ContainerItems.Kind kind = ContainerItems.kind(stack, config);
                if (kind != null) {
                    sources.add(containerItem(stack, kind));
                }
            }
        }
        if (player.level().isClientSide()) {
            // The client cannot see the Ender Chest or the bound container; the server sends their counts.
            sources.add(remote());
        } else {
            Container bound = boundContainer(player, settings);
            if (bound != null) {
                sources.add(container(bound));
            }
            if (config.enderChest) {
                sources.add(container(player.getEnderChestInventory()));
            }
        }
        sources.add(loose(hotbar));
        sources.add(loose(List.of(offhand)));
    }

    public static Supply of(Player player, WandSettings settings) {
        return new Supply(player, settings);
    }

    public boolean infinite() {
        return infinite;
    }

    /** How many plain {@code item}s the player can use (cached per supply). */
    public int count(Item item) {
        if (infinite) {
            return Integer.MAX_VALUE;
        }
        return counts.computeIfAbsent(item, it -> {
            ItemStack plain = new ItemStack(it);
            long total = 0;
            for (Source source : sources) {
                total += source.count(plain);
            }
            return (int) Math.min(Integer.MAX_VALUE, total);
        });
    }

    /** Takes one plain {@code item}. */
    public boolean take(Item item) {
        if (infinite) {
            return true;
        }
        ItemStack plain = new ItemStack(item);
        for (Source source : sources) {
            if (source.take(plain, 1) == 1) {
                counts.computeIfPresent(item, (it, n) -> n - 1);
                player.getInventory().setChanged();
                return true;
            }
        }
        return false;
    }

    /** Hands {@code stack} to the player: into the inventory, or dropped at their feet if it is full. */
    public void give(ItemStack stack) {
        give(player, stack);
        counts.remove(stack.getItem());
    }

    public static void give(Player player, ItemStack stack) {
        if (stack.isEmpty() || player.isCreative()) {
            return;
        }
        ItemStack rest = stack.copy();
        if (!player.getInventory().add(rest) && !rest.isEmpty()) {
            player.drop(rest, false);
        }
        player.getInventory().setChanged();
    }

    /** Whether the player holds at least {@code count} of each stack (plain match). Used to take back drops on undo. */
    public static boolean has(Player player, List<ItemStack> stacks) {
        if (player.isCreative()) {
            return true;
        }
        Map<Item, Integer> needed = new HashMap<>();
        for (ItemStack stack : stacks) {
            needed.merge(stack.getItem(), stack.getCount(), Integer::sum);
        }
        for (Map.Entry<Item, Integer> entry : needed.entrySet()) {
            ItemStack plain = new ItemStack(entry.getKey());
            int have = 0;
            for (ItemStack stack : player.getInventory().items) {
                if (ItemStack.isSameItemSameTags(stack, plain)) {
                    have += stack.getCount();
                }
            }
            ItemStack offhand = player.getInventory().offhand.get(0);
            if (ItemStack.isSameItemSameTags(offhand, plain)) {
                have += offhand.getCount();
            }
            if (have < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    /** Removes {@code stacks} from the inventory; call {@link #has} first. */
    public static void takeBack(Player player, List<ItemStack> stacks) {
        if (player.isCreative()) {
            return;
        }
        List<ItemStack> all = new ArrayList<>(player.getInventory().items);
        all.add(player.getInventory().offhand.get(0));
        for (ItemStack wanted : stacks) {
            int left = wanted.getCount();
            ItemStack plain = new ItemStack(wanted.getItem());
            for (ItemStack stack : all) {
                if (left == 0) {
                    break;
                }
                if (ItemStack.isSameItemSameTags(stack, plain)) {
                    int n = Math.min(left, stack.getCount());
                    stack.shrink(n);
                    left -= n;
                }
            }
        }
        player.getInventory().setChanged();
    }

    /** The container the wand is bound to, if it is usable right now. */
    public static @Nullable Container boundContainer(Player player, WandSettings settings) {
        ServerConfig config = WandsConfig.server();
        WandSettings.Spot spot = settings.bound();
        if (!config.boundContainers || spot == null) {
            return null;
        }
        Level level = player.level();
        if (!spot.dimension().equals(dimensionId(level))) {
            return null;
        }
        BlockPos pos = spot.pos();
        if (!level.isLoaded(pos) || pos.distSqr(player.blockPosition()) > (double) config.boundRange * config.boundRange) {
            return null;
        }
        return containerAt(level, pos);
    }

    /** A vanilla container at {@code pos}: chests (double chests whole), barrels, placed shulker boxes, modded containers. */
    public static @Nullable Container containerAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock chest) {
            return ChestBlock.getContainer(chest, state, level, pos, true);
        }
        BlockEntity entity = level.getBlockEntity(pos);
        return entity instanceof Container container ? container : null;
    }

    public static String dimensionId(Level level) {
        return level.dimension().location().toString();
    }

    /** Client side: counts of blocks in the Ender Chest and the bound container, from the server. */
    public static void setRemoteCounts(Map<Item, Integer> counts) {
        remoteCounts = Map.copyOf(counts);
    }

    /** Server side: the blocks in the Ender Chest and the bound container, for {@link #setRemoteCounts} on the client. */
    public static Map<Item, Integer> remoteCounts(Player player, WandSettings settings) {
        Map<Item, Integer> counts = new HashMap<>();
        List<Container> containers = new ArrayList<>();
        Container bound = boundContainer(player, settings);
        if (bound != null) {
            containers.add(bound);
        }
        if (WandsConfig.server().enderChest) {
            containers.add(player.getEnderChestInventory());
        }
        for (Container container : containers) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (stack.getItem() instanceof BlockItem
                        && ItemStack.isSameItemSameTags(stack, new ItemStack(stack.getItem()))) {
                    counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
                }
            }
        }
        return counts;
    }

    private static Map<Item, Integer> remoteCounts = Map.of();

    private static Source remote() {
        return new Source() {
            @Override
            public int count(ItemStack plain) {
                return remoteCounts.getOrDefault(plain.getItem(), 0);
            }

            @Override
            public int take(ItemStack plain, int amount) {
                return 0;
            }
        };
    }

    private static Source loose(List<ItemStack> stacks) {
        return new Source() {
            @Override
            public int count(ItemStack plain) {
                int total = 0;
                for (ItemStack stack : stacks) {
                    if (ItemStack.isSameItemSameTags(stack, plain)) {
                        total += stack.getCount();
                    }
                }
                return total;
            }

            @Override
            public int take(ItemStack plain, int amount) {
                int taken = 0;
                for (ItemStack stack : stacks) {
                    if (taken == amount) {
                        break;
                    }
                    if (ItemStack.isSameItemSameTags(stack, plain)) {
                        int n = Math.min(amount - taken, stack.getCount());
                        stack.shrink(n);
                        taken += n;
                    }
                }
                return taken;
            }
        };
    }

    private static Source containerItem(ItemStack container, ContainerItems.Kind kind) {
        return new Source() {
            @Override
            public int count(ItemStack plain) {
                int total = 0;
                for (ItemStack stack : ContainerItems.read(container, kind)) {
                    if (ItemStack.isSameItemSameTags(stack, plain)) {
                        total += stack.getCount();
                    }
                }
                return total;
            }

            @Override
            public int take(ItemStack plain, int amount) {
                List<ItemStack> items = ContainerItems.read(container, kind);
                int taken = 0;
                for (ItemStack stack : items) {
                    if (taken == amount) {
                        break;
                    }
                    if (ItemStack.isSameItemSameTags(stack, plain)) {
                        int n = Math.min(amount - taken, stack.getCount());
                        stack.shrink(n);
                        taken += n;
                    }
                }
                if (taken > 0) {
                    ContainerItems.write(container, kind, items);
                }
                return taken;
            }
        };
    }

    private static Source container(Container container) {
        return new Source() {
            @Override
            public int count(ItemStack plain) {
                int total = 0;
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack stack = container.getItem(i);
                    if (ItemStack.isSameItemSameTags(stack, plain)) {
                        total += stack.getCount();
                    }
                }
                return total;
            }

            @Override
            public int take(ItemStack plain, int amount) {
                int taken = 0;
                for (int i = 0; i < container.getContainerSize() && taken < amount; i++) {
                    ItemStack stack = container.getItem(i);
                    if (ItemStack.isSameItemSameTags(stack, plain)) {
                        taken += container.removeItem(i, amount - taken).getCount();
                    }
                }
                if (taken > 0) {
                    container.setChanged();
                }
                return taken;
            }
        };
    }
}

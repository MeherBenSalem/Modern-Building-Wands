package com.nigthbeam.reconstructedwands.wand.supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import com.nigthbeam.reconstructedwands.REConstructedWands;
import com.nigthbeam.reconstructedwands.api.IWandSupplier;
import com.nigthbeam.reconstructedwands.basics.ReplacementRegistry;
import com.nigthbeam.reconstructedwands.basics.WandUtil;
import com.nigthbeam.reconstructedwands.basics.option.WandOptions;
import com.nigthbeam.reconstructedwands.basics.pool.IPool;
import com.nigthbeam.reconstructedwands.basics.pool.OrderedPool;
import com.nigthbeam.reconstructedwands.containers.ContainerManager;
import com.nigthbeam.reconstructedwands.wand.undo.PlaceSnapshot;

import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Default WandSupplier. Takes items from player inventory in order.
 */
public class SupplierInventory implements IWandSupplier {
    protected final Player player;
    protected final WandOptions options;

    protected HashMap<BlockItem, Integer> itemCounts;
    protected IPool<BlockItem> itemPool;

    public SupplierInventory(Player player, WandOptions options) {
        this.player = player;
        this.options = options;
    }

    @Override
    public void getSupply(@Nullable BlockItem target) {
        itemCounts = new LinkedHashMap<>();
        ItemStack offhandStack = player.getItemInHand(InteractionHand.OFF_HAND);

        itemPool = new OrderedPool<>();

        // Block in offhand -> override
        if (!offhandStack.isEmpty() && offhandStack.getItem() instanceof BlockItem blockItem) {
            addBlockItem(blockItem);
        }
        // Otherwise use target block
        else if (target != null && target != Items.AIR) {
            addBlockItem(target);

            // Add replacement items
            if (options.match.get() != WandOptions.MATCH.EXACT) {
                for (Item it : ReplacementRegistry.getMatchingSet(target)) {
                    if (it instanceof BlockItem blockItem)
                        addBlockItem(blockItem);
                }
            }
        }
    }

    protected void addBlockItem(BlockItem item) {
        int count = WandUtil.countItem(player, item);
        if (count > 0) {
            itemCounts.put(item, count);
            itemPool.add(item);
        }
    }

    @Override
    @Nullable
    public PlaceSnapshot getPlaceSnapshot(Level world, BlockPos pos, BlockHitResult rayTraceResult,
            @Nullable BlockState supportingBlock) {
        if (!WandUtil.isPositionPlaceable(world, player, pos, options.replace.get()))
            return null;
        itemPool.reset();

        while (true) {
            BlockItem item = itemPool.draw();
            if (item == null)
                return null;

            int count = itemCounts.get(item);
            if (count == 0)
                continue;

            PlaceSnapshot placeSnapshot = PlaceSnapshot.get(world, player, rayTraceResult, pos, item, supportingBlock,
                    options);
            if (placeSnapshot != null) {
                int newCount = count - 1;
                itemCounts.put(item, newCount);

                if (newCount == 0)
                    itemPool.remove(item);

                return placeSnapshot;
            }
        }
    }

    @Override
    public int takeItemStack(ItemStack stack) {
        int count = stack.getCount();
        Item item = stack.getItem();

        if (player.getInventory().items == null)
            return count;
        if (player.isCreative())
            return 0;

        List<ItemStack> hotbar = WandUtil.getHotbarWithOffhand(player);
        List<ItemStack> mainInv = WandUtil.getMainInv(player);

        // Take items from main inv, loose items first
        count = takeItemsInvList(count, item, mainInv, false);
        count = takeItemsInvList(count, item, mainInv, true);

        // Take items from hotbar, containers first
        count = takeItemsInvList(count, item, hotbar, true);
        count = takeItemsInvList(count, item, hotbar, false);

        return count;
    }

    private int takeItemsInvList(int count, Item item, List<ItemStack> inv, boolean container) {
        ContainerManager containerManager = REConstructedWands.instance.containerManager;

        for (ItemStack stack : inv) {
            if (count == 0)
                break;

            if (container) {
                count = containerManager.useItems(player, new ItemStack(item), stack, count);
            }

            if (!container && WandUtil.stackEquals(stack, item)) {
                int toTake = Math.min(count, stack.getCount());
                stack.shrink(toTake);
                count -= toTake;
                player.getInventory().setChanged();
            }
        }
        return count;
    }
}

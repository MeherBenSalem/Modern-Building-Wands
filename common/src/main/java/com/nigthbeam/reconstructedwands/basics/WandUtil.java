package com.nigthbeam.reconstructedwands.basics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.nigthbeam.reconstructedwands.REConstructedWands;
import com.nigthbeam.reconstructedwands.items.wand.ItemWand;
import com.nigthbeam.reconstructedwands.platform.Services;
import com.nigthbeam.reconstructedwands.wand.WandItemUseContext;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Utility methods for wand operations.
 */
public class WandUtil {
    public static boolean stackEquals(ItemStack stackA, ItemStack stackB) {
        return ItemStack.isSameItemSameComponents(stackA, stackB);
    }

    public static boolean stackEquals(ItemStack stackA, Item item) {
        ItemStack stackB = new ItemStack(item);
        return stackEquals(stackA, stackB);
    }

    @Nullable
    public static ItemStack holdingWand(Player player) {
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!mainHand.isEmpty() && mainHand.getItem() instanceof ItemWand) {
            return mainHand;
        }
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!offHand.isEmpty() && offHand.getItem() instanceof ItemWand) {
            return offHand;
        }
        return null;
    }

    public static BlockPos posFromVec(Vec3 vec) {
        return new BlockPos(
                (int) Math.round(vec.x), (int) Math.round(vec.y), (int) Math.round(vec.z));
    }

    public static Vec3 entityPositionVec(Entity entity) {
        return new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ());
    }

    public static Vec3 blockPosVec(BlockPos pos) {
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    public static List<ItemStack> getHotbar(Player player) {
        return player.getInventory().items.subList(0, 9);
    }

    public static List<ItemStack> getHotbarWithOffhand(Player player) {
        ArrayList<ItemStack> inventory = new ArrayList<>(player.getInventory().items.subList(0, 9));
        inventory.addAll(player.getInventory().offhand);
        return inventory;
    }

    public static List<ItemStack> getMainInv(Player player) {
        return player.getInventory().items.subList(9, player.getInventory().items.size());
    }

    public static List<ItemStack> getFullInv(Player player) {
        ArrayList<ItemStack> inventory = new ArrayList<>(player.getInventory().offhand);
        inventory.addAll(player.getInventory().items);
        return inventory;
    }

    public static int blockDistance(BlockPos p1, BlockPos p2) {
        return Math.max(Math.abs(p1.getX() - p2.getX()), Math.abs(p1.getZ() - p2.getZ()));
    }

    public static boolean placeBlock(Level world, Player player, BlockState block, BlockPos pos,
            @Nullable BlockItem item) {
        return Services.BLOCK_EVENTS.placeBlock(world, player, block, pos, item);
    }

    public static boolean removeBlock(Level world, Player player, @Nullable BlockState block, BlockPos pos) {
        return Services.BLOCK_EVENTS.removeBlock(world, player, block, pos);
    }

    public static int countItem(Player player, Item item) {
        if (player.getInventory().items == null)
            return 0;
        if (player.isCreative())
            return Integer.MAX_VALUE;

        int total = 0;
        var containerManager = REConstructedWands.instance.containerManager;
        List<ItemStack> inventory = WandUtil.getFullInv(player);

        for (ItemStack stack : inventory) {
            if (stack == null || stack.isEmpty())
                continue;

            if (WandUtil.stackEquals(stack, item)) {
                total += stack.getCount();
            } else {
                int amount = containerManager.countItems(player, new ItemStack(item), stack);
                if (amount == Integer.MAX_VALUE)
                    return Integer.MAX_VALUE;
                total += amount;
            }
        }
        return total;
    }

    private static boolean isPositionModifiable(Level world, Player player, BlockPos pos) {
        // Is position out of world?
        if (!world.isInWorldBounds(pos))
            return false;

        // Is block modifiable?
        if (!world.mayInteract(player, pos))
            return false;

        // Limit range
        int maxRange = Services.CONFIG.getMaxRange();
        if (maxRange > 0 && WandUtil.blockDistance(player.blockPosition(), pos) > maxRange)
            return false;

        return true;
    }

    /**
     * Tests if a wand can place a block at a certain position.
     * This check is independent of the used block.
     */
    public static boolean isPositionPlaceable(Level world, Player player, BlockPos pos, boolean replace) {
        if (!isPositionModifiable(world, player, pos))
            return false;

        // If replace mode is off, target has to be air
        if (world.isEmptyBlock(pos))
            return true;

        // Otherwise, check if the block can be replaced by a generic block
        return replace && world.getBlockState(pos).canBeReplaced(
                new WandItemUseContext(world, player,
                        new BlockHitResult(new Vec3(0, 0, 0), Direction.DOWN, pos, false),
                        pos, (BlockItem) Items.STONE));
    }

    public static boolean isBlockRemovable(Level world, Player player, BlockPos pos) {
        if (!isPositionModifiable(world, player, pos))
            return false;

        if (!player.isCreative()) {
            return !(world.getBlockState(pos).getDestroySpeed(world, pos) <= -1) && world.getBlockEntity(pos) == null;
        }
        return true;
    }

    public static boolean isBlockPermeable(Level world, BlockPos pos) {
        return world.isEmptyBlock(pos) || world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();
    }

    public static boolean entitiesCollidingWithBlock(Level world, BlockState blockState, BlockPos pos) {
        VoxelShape shape = blockState.getCollisionShape(world, pos);
        if (!shape.isEmpty()) {
            AABB blockBB = shape.bounds().move(pos);
            return !world.getEntitiesOfClass(LivingEntity.class, blockBB, Predicate.not(Entity::isSpectator)).isEmpty();
        }
        return false;
    }

    public static Direction fromVector(Vec3 vector) {
        return Direction.getApproximateNearest((float) vector.x, (float) vector.y, (float) vector.z);
    }
}

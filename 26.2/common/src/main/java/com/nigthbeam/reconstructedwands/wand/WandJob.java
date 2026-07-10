package com.nigthbeam.reconstructedwands.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import com.nigthbeam.reconstructedwands.REConstructedWands;
import com.nigthbeam.reconstructedwands.Constants;
import com.nigthbeam.reconstructedwands.api.IWandAction;
import com.nigthbeam.reconstructedwands.api.IWandSupplier;
import com.nigthbeam.reconstructedwands.basics.option.WandOptions;
import com.nigthbeam.reconstructedwands.items.wand.ItemWand;
import com.nigthbeam.reconstructedwands.platform.Services;
import com.nigthbeam.reconstructedwands.wand.supplier.SupplierInventory;
import com.nigthbeam.reconstructedwands.wand.supplier.SupplierRandom;
import com.nigthbeam.reconstructedwands.wand.undo.ISnapshot;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A wand job represents a block placement/destruction operation.
 */
public class WandJob {
    public final Player player;
    public final Level world;
    public final BlockHitResult rayTraceResult;
    public final WandOptions options;
    public final ItemStack wand;
    public final ItemWand wandItem;

    private final IWandAction wandAction;
    private final IWandSupplier wandSupplier;

    private List<ISnapshot> placeSnapshots;

    public WandJob(Player player, Level world, BlockHitResult rayTraceResult, ItemStack wand) {
        this.player = player;
        this.world = world;
        this.rayTraceResult = rayTraceResult;
        this.placeSnapshots = new ArrayList<>();

        this.wand = wand;
        this.wandItem = (ItemWand) wand.getItem();
        options = new WandOptions(wand);

        // Select wand action and supplier based on options
        wandSupplier = options.random.get() ? new SupplierRandom(player, options)
                : new SupplierInventory(player, options);
        wandAction = options.cores.get().getWandAction();

        wandSupplier.getSupply(getTargetItem(world, rayTraceResult));
    }

    @Nullable
    private static BlockItem getTargetItem(Level world, BlockHitResult rayTraceResult) {
        Item tgitem = world.getBlockState(rayTraceResult.getBlockPos()).getBlock().asItem();
        if (!(tgitem instanceof BlockItem))
            return null;
        return (BlockItem) tgitem;
    }

    public void getSnapshots() {
        int limit;
        // Infinity wand gets enhanced limit in creative mode
        if (player.isCreative() && wandItem.isInfinityWand()) {
            limit = Services.CONFIG.getLimitCreative();
        } else {
            limit = Math.min(wandItem.remainingDurability(wand), wandAction.getLimit(wand));
        }

        if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
            placeSnapshots = wandAction.getSnapshots(world, player, rayTraceResult, wand, options, wandSupplier, limit);
        } else {
            placeSnapshots = wandAction.getSnapshotsFromAir(world, player, rayTraceResult, wand, options, wandSupplier,
                    limit);
        }
    }

    public Set<BlockPos> getBlockPositions() {
        return placeSnapshots.stream().map(ISnapshot::getPos).collect(Collectors.toSet());
    }

    public int blockCount() {
        return placeSnapshots.size();
    }

    @SuppressWarnings("deprecation")
    public boolean doIt() {
        ArrayList<ISnapshot> executed = new ArrayList<>();

        for (ISnapshot snapshot : placeSnapshots) {
            if (wand.isEmpty() || wandItem.remainingDurability(wand) == 0)
                break;

            if (snapshot.execute(world, player, rayTraceResult)) {
                if (player.isCreative()) {
                    executed.add(snapshot);
                } else {
                    // If the item can't be taken, undo the placement
                    if (wandSupplier.takeItemStack(snapshot.getRequiredItems()) == 0) {
                        executed.add(snapshot);
                        wand.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                    } else {
                        Constants.LOG.info("Item could not be taken. Remove block: {}",
                                snapshot.getBlockState().getBlock().toString());
                        snapshot.forceRestore(world);
                    }
                }
            }
        }
        placeSnapshots = executed;

        // Play place sound
        if (!placeSnapshots.isEmpty()) {
            SoundType sound = placeSnapshots.get(0).getBlockState().getSoundType();
            world.playSound(null, player.blockPosition(), sound.getPlaceSound(), SoundSource.BLOCKS, sound.getVolume(),
                    sound.getPitch());

            // Add to job history for undo
            REConstructedWands.instance.undoHistory.add(player, world, placeSnapshots);
        }

        return !placeSnapshots.isEmpty();
    }
}

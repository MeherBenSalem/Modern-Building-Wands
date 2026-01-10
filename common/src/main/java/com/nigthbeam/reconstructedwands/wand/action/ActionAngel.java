package com.nigthbeam.reconstructedwands.wand.action;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import com.nigthbeam.reconstructedwands.api.IWandAction;
import com.nigthbeam.reconstructedwands.api.IWandSupplier;
import com.nigthbeam.reconstructedwands.basics.WandUtil;
import com.nigthbeam.reconstructedwands.basics.option.WandOptions;
import com.nigthbeam.reconstructedwands.platform.Services;
import com.nigthbeam.reconstructedwands.wand.undo.ISnapshot;
import com.nigthbeam.reconstructedwands.wand.undo.PlaceSnapshot;

import org.jetbrains.annotations.NotNull;
import java.util.LinkedList;
import java.util.List;

/**
 * Angel action: place blocks behind the target block or in mid-air.
 */
public class ActionAngel implements IWandAction {
    @Override
    public int getLimit(ItemStack wand) {
        String wandType = getWandType(wand);
        return Services.CONFIG.getWandAngel(wandType);
    }

    @NotNull
    @Override
    public List<ISnapshot> getSnapshots(Level world, Player player, BlockHitResult rayTraceResult,
            ItemStack wand, WandOptions options, IWandSupplier supplier, int limit) {
        LinkedList<ISnapshot> placeSnapshots = new LinkedList<>();

        Direction placeDirection = rayTraceResult.getDirection();
        BlockPos currentPos = rayTraceResult.getBlockPos();
        BlockState supportingBlock = world.getBlockState(currentPos);

        for (int i = 0; i < limit; i++) {
            currentPos = currentPos.offset(placeDirection.getOpposite().getNormal());

            PlaceSnapshot snapshot = supplier.getPlaceSnapshot(world, currentPos, rayTraceResult, supportingBlock);
            if (snapshot != null) {
                placeSnapshots.add(snapshot);
                break;
            }
        }
        return placeSnapshots;
    }

    @NotNull
    @Override
    public List<ISnapshot> getSnapshotsFromAir(Level world, Player player, BlockHitResult rayTraceResult,
            ItemStack wand, WandOptions options, IWandSupplier supplier, int limit) {
        LinkedList<ISnapshot> placeSnapshots = new LinkedList<>();

        if (!player.isCreative() && !Services.CONFIG.isAngelFalling() && player.fallDistance > 10) {
            return placeSnapshots;
        }

        Vec3 playerVec = WandUtil.entityPositionVec(player);
        Vec3 lookVec = player.getLookAngle().multiply(2, 2, 2);
        Vec3 placeVec = playerVec.add(lookVec);
        BlockPos currentPos = WandUtil.posFromVec(placeVec);

        PlaceSnapshot snapshot = supplier.getPlaceSnapshot(world, currentPos, rayTraceResult, null);
        if (snapshot != null)
            placeSnapshots.add(snapshot);

        return placeSnapshots;
    }

    protected String getWandType(ItemStack wand) {
        return Services.REGISTRY.getItemKey(wand.getItem()).getPath();
    }
}

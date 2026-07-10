package com.nigthbeam.reconstructedwands.wand.supplier;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import com.nigthbeam.reconstructedwands.basics.WandUtil;
import com.nigthbeam.reconstructedwands.basics.option.WandOptions;
import com.nigthbeam.reconstructedwands.basics.pool.RandomPool;

import org.jetbrains.annotations.Nullable;
import java.util.LinkedHashMap;

/**
 * Random WandSupplier. Takes random items from player hotbar.
 */
public class SupplierRandom extends SupplierInventory {
    public SupplierRandom(Player player, WandOptions options) {
        super(player, options);
    }

    @Override
    public void getSupply(@Nullable BlockItem target) {
        itemCounts = new LinkedHashMap<>();

        // Random mode -> add all items from hotbar
        itemPool = new RandomPool<>(player.getRandom());

        for (ItemStack stack : WandUtil.getHotbarWithOffhand(player)) {
            if (stack.getItem() instanceof BlockItem blockItem) {
                addBlockItem(blockItem);
            }
        }
    }
}

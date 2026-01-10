package com.nigthbeam.reconstructedwands.containers;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.nigthbeam.reconstructedwands.api.IContainerHandler;

import java.util.ArrayList;

/**
 * Manages container handlers for extended inventory access.
 */
public class ContainerManager {
    private final ArrayList<IContainerHandler> handlers;

    public ContainerManager() {
        handlers = new ArrayList<>();
    }

    public boolean register(IContainerHandler handler) {
        return handlers.add(handler);
    }

    public int countItems(Player player, ItemStack itemStack, ItemStack inventoryStack) {
        for (IContainerHandler handler : handlers) {
            if (handler.matches(player, itemStack, inventoryStack)) {
                return handler.countItems(player, itemStack, inventoryStack);
            }
        }
        return 0;
    }

    public int useItems(Player player, ItemStack itemStack, ItemStack inventoryStack, int count) {
        for (IContainerHandler handler : handlers) {
            if (handler.matches(player, itemStack, inventoryStack)) {
                return handler.useItems(player, itemStack, inventoryStack, count);
            }
        }
        return count;
    }
}

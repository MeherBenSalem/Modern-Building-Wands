package com.nigthbeam.reconstructedwands.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import com.nigthbeam.reconstructedwands.api.IWandUpgrade;
import com.nigthbeam.reconstructedwands.basics.option.WandOptions;
import com.nigthbeam.reconstructedwands.items.wand.ItemWand;
import com.nigthbeam.reconstructedwands.items.wand.ItemWandBasic;
import com.nigthbeam.reconstructedwands.platform.Services;

import org.jetbrains.annotations.NotNull;

/**
 * Custom crafting recipe that allows combining a wand with a core upgrade.
 * Place a wand + core in a crafting grid to install the core on the wand.
 */
public class RecipeWandUpgrade extends CustomRecipe {

    public RecipeWandUpgrade(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        ItemStack wand = null;
        IWandUpgrade upgrade = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                if (wand == null && stack.getItem() instanceof ItemWand) {
                    wand = stack;
                } else if (upgrade == null && stack.getItem() instanceof IWandUpgrade) {
                    upgrade = (IWandUpgrade) stack.getItem();
                } else {
                    // More than 2 items, or duplicate types
                    return false;
                }
            }
        }

        if (wand == null || upgrade == null) {
            return false;
        }

        // Check if wand doesn't already have this upgrade and is upgradeable
        WandOptions options = new WandOptions(wand);
        return !options.hasUpgrade(upgrade) && isWandUpgradeable(wand);
    }

    @NotNull
    @Override
    public ItemStack assemble(@NotNull CraftingInput input, @NotNull HolderLookup.Provider registries) {
        ItemStack wand = null;
        IWandUpgrade upgrade = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof ItemWand) {
                    wand = stack;
                } else if (stack.getItem() instanceof IWandUpgrade) {
                    upgrade = (IWandUpgrade) stack.getItem();
                }
            }
        }

        if (wand == null || upgrade == null) {
            return ItemStack.EMPTY;
        }

        // Create a copy of the wand with the upgrade added
        ItemStack newWand = wand.copy();
        WandOptions options = new WandOptions(newWand);
        options.addUpgrade(upgrade);
        options.save();
        return newWand;
    }

    @NotNull
    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return ModRecipes.WAND_UPGRADE_SERIALIZER;
    }

    /**
     * Determines if a wand can be upgraded based on its type.
     */
    private boolean isWandUpgradeable(ItemStack wand) {
        if (wand.getItem() instanceof ItemWandBasic basicWand) {
            ToolMaterial tier = basicWand.getTier();
            String wandType = getWandTypeFromTier(tier);
            return Services.CONFIG.isWandUpgradeable(wandType);
        } else if (wand.getItem() instanceof ItemWand itemWand) {
            // Infinity wand - always upgradeable
            return itemWand.isInfinityWand() || Services.CONFIG.isWandUpgradeable("infinity");
        }
        return false;
    }

    private String getWandTypeFromTier(ToolMaterial tier) {
        if (tier == ToolMaterial.STONE)
            return "stone";
        if (tier == ToolMaterial.IRON)
            return "iron";
        if (tier == ToolMaterial.DIAMOND)
            return "diamond";
        if (tier == ToolMaterial.NETHERITE)
            return "netherite";
        return "stone";
    }
}

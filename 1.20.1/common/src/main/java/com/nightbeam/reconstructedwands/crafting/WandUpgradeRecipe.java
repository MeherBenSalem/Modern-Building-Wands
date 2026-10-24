package com.nightbeam.reconstructedwands.crafting;

import com.nightbeam.reconstructedwands.content.ModContent;
import com.nightbeam.reconstructedwands.item.CoreItem;
import com.nightbeam.reconstructedwands.item.WandItem;
import com.nightbeam.reconstructedwands.wand.WandSettings;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** A wand and a core in the crafting grid: the wand comes out with the core installed (settings and damage kept). */
public final class WandUpgradeRecipe extends CustomRecipe {
    public WandUpgradeRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    public static RecipeSerializer<WandUpgradeRecipe> serializer() {
        return new SimpleCraftingRecipeSerializer<>(WandUpgradeRecipe::new);
    }

    private record Parts(ItemStack wand, CoreItem core) {
    }

    private static @Nullable Parts parts(CraftingContainer input) {
        ItemStack wand = null;
        CoreItem core = null;
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (wand == null && stack.getItem() instanceof WandItem) {
                wand = stack;
            } else if (core == null && stack.getItem() instanceof CoreItem item) {
                core = item;
            } else {
                return null;
            }
        }
        return wand != null && core != null ? new Parts(wand, core) : null;
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        Parts parts = parts(input);
        return parts != null && ((WandItem) parts.wand().getItem()).acceptsCores() && !WandSettings.of(parts.wand()).hasCore(parts.core().mode());
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        Parts parts = parts(input);
        if (parts == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = parts.wand().copyWithCount(1);
        WandSettings settings = WandSettings.of(result);
        if (!settings.install(parts.core().mode())) {
            return ItemStack.EMPTY;
        }
        settings.save();
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<WandUpgradeRecipe> getSerializer() {
        return ModContent.WAND_UPGRADE.get();
    }
}

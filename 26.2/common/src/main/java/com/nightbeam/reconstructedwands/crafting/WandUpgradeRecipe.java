package com.nightbeam.reconstructedwands.crafting;

import com.mojang.serialization.MapCodec;
import com.nightbeam.reconstructedwands.content.ModContent;
import com.nightbeam.reconstructedwands.item.CoreItem;
import com.nightbeam.reconstructedwands.item.WandItem;
import com.nightbeam.reconstructedwands.wand.WandSettings;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** A wand and a core in the crafting grid: the wand comes out with the core installed (settings and damage kept). */
public final class WandUpgradeRecipe extends CustomRecipe {
    public static final WandUpgradeRecipe INSTANCE = new WandUpgradeRecipe();
    public static final MapCodec<WandUpgradeRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, WandUpgradeRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private WandUpgradeRecipe() {
    }

    public static RecipeSerializer<WandUpgradeRecipe> serializer() {
        return new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
    }

    private record Parts(ItemStack wand, CoreItem core) {
    }

    private static @Nullable Parts parts(CraftingInput input) {
        ItemStack wand = null;
        CoreItem core = null;
        for (int i = 0; i < input.size(); i++) {
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
    public boolean matches(CraftingInput input, Level level) {
        Parts parts = parts(input);
        return parts != null && ((WandItem) parts.wand().getItem()).acceptsCores() && !WandSettings.of(parts.wand()).hasCore(parts.core().mode());
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
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
    public RecipeSerializer<WandUpgradeRecipe> getSerializer() {
        return ModContent.WAND_UPGRADE.get();
    }
}

package com.nightbeam.reconstructedwands.content;

import com.nightbeam.reconstructedwands.crafting.WandUpgradeRecipe;
import com.nightbeam.reconstructedwands.item.CoreItem;
import com.nightbeam.reconstructedwands.item.WandItem;
import com.nightbeam.reconstructedwands.item.WandTier;
import com.nightbeam.reconstructedwands.platform.Registrar;
import com.nightbeam.reconstructedwands.wand.WandMode;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** Everything the mod registers. Registry ids of 1.x content are unchanged. */
public final class ModContent {
    public static final Map<WandTier, Supplier<WandItem>> WANDS = new EnumMap<>(WandTier.class);
    public static final Map<WandMode, Supplier<CoreItem>> CORES = new EnumMap<>(WandMode.class);
    public static Supplier<RecipeSerializer<WandUpgradeRecipe>> WAND_UPGRADE;

    private ModContent() {
    }

    public static void register(Registrar r) {
        for (WandTier tier : WandTier.values()) {
            WANDS.put(tier, r.register(Registries.ITEM, tier.itemId(), id -> new WandItem(tier, wandProperties(tier, id))));
        }
        for (WandMode mode : WandMode.values()) {
            if (mode.core() != null) {
                CORES.put(mode, r.register(Registries.ITEM, mode.core(),
                        id -> new CoreItem(mode, properties(id).stacksTo(16).rarity(Rarity.UNCOMMON))));
            }
        }
        WAND_UPGRADE = r.register(Registries.RECIPE_SERIALIZER, "wand_upgrade", id -> WandUpgradeRecipe.serializer());
    }

    private static Item.Properties properties(Identifier id) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id));
    }

    private static Item.Properties wandProperties(WandTier tier, Identifier id) {
        Item.Properties properties = properties(id).stacksTo(1);
        int durability = tier.stats().durability();
        TagKey<Item> repair = switch (tier) {
            case STONE -> ItemTags.STONE_TOOL_MATERIALS;
            case IRON -> ItemTags.IRON_TOOL_MATERIALS;
            case DIAMOND -> ItemTags.DIAMOND_TOOL_MATERIALS;
            case NETHERITE -> ItemTags.NETHERITE_TOOL_MATERIALS;
            case INFINITY -> null;
        };
        if (durability > 0 && tier != WandTier.INFINITY) {
            properties.durability(durability).enchantable(tier == WandTier.STONE ? 5 : tier == WandTier.IRON ? 14 : 10);
            properties.repairable(repair);
        } else {
            properties.component(DataComponents.UNBREAKABLE, Unit.INSTANCE);
        }
        if (tier == WandTier.NETHERITE || tier == WandTier.INFINITY) {
            properties.fireResistant();
        }
        properties.rarity(tier == WandTier.INFINITY ? Rarity.EPIC : tier == WandTier.NETHERITE ? Rarity.RARE : Rarity.COMMON);
        return properties;
    }

    public static WandItem wand(WandTier tier) {
        return WANDS.get(tier).get();
    }

    public static CoreItem core(WandMode mode) {
        return CORES.get(mode).get();
    }

    /** The creative tab's items, in order. Each loader builds the tab itself (its builder API differs). */
    public static void forEachTabItem(Consumer<ItemStack> output) {
        for (WandTier tier : WandTier.values()) {
            output.accept(new ItemStack(wand(tier)));
        }
        for (Supplier<CoreItem> core : CORES.values()) {
            output.accept(new ItemStack(core.get()));
        }
    }

    public static ItemStack tabIcon() {
        return new ItemStack(wand(WandTier.DIAMOND));
    }

    public static Component tabTitle() {
        return Component.translatable("itemGroup.reconstructedwands.wands");
    }

    public static Identifier tabId() {
        return com.nightbeam.reconstructedwands.ReconstructedWands.id("wands_tab");
    }
}

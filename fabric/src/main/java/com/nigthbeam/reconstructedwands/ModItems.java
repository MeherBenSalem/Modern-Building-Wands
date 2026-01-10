package com.nigthbeam.reconstructedwands;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import com.nigthbeam.reconstructedwands.items.core.ItemCoreAngel;
import com.nigthbeam.reconstructedwands.items.core.ItemCoreDestruction;
import com.nigthbeam.reconstructedwands.items.wand.ItemWandBasic;
import com.nigthbeam.reconstructedwands.items.wand.ItemWandInfinity;

/**
 * Fabric item registration.
 */
public class ModItems {
    // Wands
    public static final Item WAND_STONE = registerItem("stone_wand",
            new ItemWandBasic(new Item.Properties().stacksTo(1), Tiers.STONE));
    public static final Item WAND_IRON = registerItem("iron_wand",
            new ItemWandBasic(new Item.Properties().stacksTo(1), Tiers.IRON));
    public static final Item WAND_DIAMOND = registerItem("diamond_wand",
            new ItemWandBasic(new Item.Properties().stacksTo(1), Tiers.DIAMOND));
    public static final Item WAND_NETHERITE = registerItem("netherite_wand",
            new ItemWandBasic(new Item.Properties().stacksTo(1).fireResistant(), Tiers.NETHERITE));
    public static final Item WAND_INFINITY = registerItem("infinity_wand",
            new ItemWandInfinity(new Item.Properties().stacksTo(1)));

    // Cores
    public static final Item CORE_ANGEL = registerItem("core_angel",
            new ItemCoreAngel(new Item.Properties().stacksTo(1)));
    public static final Item CORE_DESTRUCTION = registerItem("core_destruction",
            new ItemCoreDestruction(new Item.Properties().stacksTo(1)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), item);
    }

    public static void register() {
        // Add items to creative tabs
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(WAND_STONE);
            entries.accept(WAND_IRON);
            entries.accept(WAND_DIAMOND);
            entries.accept(WAND_NETHERITE);
            entries.accept(WAND_INFINITY);
            entries.accept(CORE_ANGEL);
            entries.accept(CORE_DESTRUCTION);
        });

        Constants.LOG.info("Registered {} items", 7);
    }
}

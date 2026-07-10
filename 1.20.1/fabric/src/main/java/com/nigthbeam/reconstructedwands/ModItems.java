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

import java.util.function.Function;

/**
 * Fabric item registration.
 * In 1.21.2+, Item.Properties must have setId() called before Item
 * construction.
 */
public class ModItems {
        // Wands - initialized during register()
        public static Item WAND_STONE;
        public static Item WAND_IRON;
        public static Item WAND_DIAMOND;
        public static Item WAND_NETHERITE;
        public static Item WAND_INFINITY;

        // Cores
        public static Item CORE_ANGEL;
        public static Item CORE_DESTRUCTION;

        private static Item registerItem(String name, Function<Item.Properties, Item> factory) {
                ResourceLocation id = new ResourceLocation(Constants.MOD_ID, name);
                Item item = factory.apply(new Item.Properties());
                return Registry.register(BuiltInRegistries.ITEM, id, item);
        }

        public static void register() {
                // Register items - must use setId() on properties in 1.21.2+
                WAND_STONE = registerItem("stone_wand",
                                props -> new ItemWandBasic(props.stacksTo(1), Tiers.STONE));
                WAND_IRON = registerItem("iron_wand", props -> new ItemWandBasic(props.stacksTo(1), Tiers.IRON));
                WAND_DIAMOND = registerItem("diamond_wand",
                                props -> new ItemWandBasic(props.stacksTo(1), Tiers.DIAMOND));
                WAND_NETHERITE = registerItem("netherite_wand",
                                props -> new ItemWandBasic(props.stacksTo(1).fireResistant(), Tiers.NETHERITE));
                WAND_INFINITY = registerItem("infinity_wand", props -> new ItemWandInfinity(props.stacksTo(1)));

                CORE_ANGEL = registerItem("core_angel", props -> new ItemCoreAngel(props.stacksTo(1)));
                CORE_DESTRUCTION = registerItem("core_destruction",
                                props -> new ItemCoreDestruction(props.stacksTo(1)));

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

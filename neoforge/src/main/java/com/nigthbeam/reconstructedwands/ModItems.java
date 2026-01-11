package com.nigthbeam.reconstructedwands;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.neoforge.registries.DeferredItem;
import com.nigthbeam.reconstructedwands.items.core.ItemCoreAngel;
import com.nigthbeam.reconstructedwands.items.core.ItemCoreDestruction;
import com.nigthbeam.reconstructedwands.items.wand.ItemWandBasic;
import com.nigthbeam.reconstructedwands.items.wand.ItemWandInfinity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * NeoForge item registration using DeferredRegister.Items.
 * Uses DeferredItem which auto-handles item ID setting in 1.21.2+.
 */
public class ModItems {

        private static final List<DeferredItem<Item>> ALL_ITEMS = new ArrayList<>();

        // Wands - use registerItem which provides Item.Properties with ID already set
        public static final DeferredItem<Item> STONE_WAND = registerItem("stone_wand",
                        props -> new ItemWandBasic(props, ToolMaterial.STONE));
        public static final DeferredItem<Item> IRON_WAND = registerItem("iron_wand",
                        props -> new ItemWandBasic(props, ToolMaterial.IRON));
        public static final DeferredItem<Item> DIAMOND_WAND = registerItem("diamond_wand",
                        props -> new ItemWandBasic(props, ToolMaterial.DIAMOND));
        public static final DeferredItem<Item> NETHERITE_WAND = registerItem("netherite_wand",
                        props -> new ItemWandBasic(props.fireResistant(), ToolMaterial.NETHERITE));
        public static final DeferredItem<Item> INFINITY_WAND = registerItem("infinity_wand",
                        props -> new ItemWandInfinity(props.stacksTo(1)));

        // Cores
        public static final DeferredItem<Item> CORE_ANGEL = registerItem("core_angel",
                        props -> new ItemCoreAngel(props.stacksTo(1)));
        public static final DeferredItem<Item> CORE_DESTRUCTION = registerItem("core_destruction",
                        props -> new ItemCoreDestruction(props.stacksTo(1)));

        private static DeferredItem<Item> registerItem(String name, Function<Item.Properties, Item> factory) {
                // DeferredRegister.Items.registerItem provides Props with ID already set
                DeferredItem<Item> item = com.nigthbeam.reconstructedwands.REConstructedWandsNeoForge.ITEMS
                                .registerItem(name, factory);
                ALL_ITEMS.add(item);
                return item;
        }

        public static void register() {
                // Called to trigger static initialization
        }

        public static List<DeferredItem<Item>> getAllItems() {
                return ALL_ITEMS;
        }
}

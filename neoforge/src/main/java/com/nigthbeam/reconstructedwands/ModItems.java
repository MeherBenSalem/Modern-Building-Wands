package com.nigthbeam.reconstructedwands;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import com.nigthbeam.reconstructedwands.items.core.ItemCoreAngel;
import com.nigthbeam.reconstructedwands.items.core.ItemCoreDestruction;
import com.nigthbeam.reconstructedwands.items.wand.ItemWandBasic;
import com.nigthbeam.reconstructedwands.items.wand.ItemWandInfinity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * NeoForge item registration using DeferredRegister.
 */
public class ModItems {

        private static final List<Supplier<Item>> ALL_ITEMS = new ArrayList<>();

        // Wands
        public static final Supplier<Item> STONE_WAND = registerItem("stone_wand",
                        () -> new ItemWandBasic(new Item.Properties(), Tiers.STONE));
        public static final Supplier<Item> IRON_WAND = registerItem("iron_wand",
                        () -> new ItemWandBasic(new Item.Properties(), Tiers.IRON));
        public static final Supplier<Item> DIAMOND_WAND = registerItem("diamond_wand",
                        () -> new ItemWandBasic(new Item.Properties(), Tiers.DIAMOND));
        public static final Supplier<Item> NETHERITE_WAND = registerItem("netherite_wand",
                        () -> new ItemWandBasic(new Item.Properties().fireResistant(), Tiers.NETHERITE));
        public static final Supplier<Item> INFINITY_WAND = registerItem("infinity_wand",
                        () -> new ItemWandInfinity(new Item.Properties().stacksTo(1)));

        // Cores
        public static final Supplier<Item> CORE_ANGEL = registerItem("core_angel",
                        () -> new ItemCoreAngel(new Item.Properties().stacksTo(1)));
        public static final Supplier<Item> CORE_DESTRUCTION = registerItem("core_destruction",
                        () -> new ItemCoreDestruction(new Item.Properties().stacksTo(1)));

        private static Supplier<Item> registerItem(String name, Supplier<Item> item) {
                Supplier<Item> registered = com.nigthbeam.reconstructedwands.REConstructedWandsNeoForge.ITEMS
                                .register(name, item);
                ALL_ITEMS.add(registered);
                return registered;
        }

        public static void register() {
                // Called to trigger static initialization
        }

        public static List<Supplier<Item>> getAllItems() {
                return ALL_ITEMS;
        }
}

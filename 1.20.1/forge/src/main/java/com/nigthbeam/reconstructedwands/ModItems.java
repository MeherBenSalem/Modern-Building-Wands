package com.nigthbeam.reconstructedwands;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.RegistryObject;
import com.nigthbeam.reconstructedwands.items.core.ItemCoreAngel;
import com.nigthbeam.reconstructedwands.items.core.ItemCoreDestruction;
import com.nigthbeam.reconstructedwands.items.wand.ItemWandBasic;
import com.nigthbeam.reconstructedwands.items.wand.ItemWandInfinity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Forge item registration using DeferredRegister and RegistryObject.
 */
public class ModItems {

        private static final List<RegistryObject<Item>> ALL_ITEMS = new ArrayList<>();

        // Wands - use registerItem which provides Item.Properties with ID already set
        public static final RegistryObject<Item> STONE_WAND = registerItem("stone_wand",
                        props -> new ItemWandBasic(props, Tiers.STONE));
        public static final RegistryObject<Item> IRON_WAND = registerItem("iron_wand",
                        props -> new ItemWandBasic(props, Tiers.IRON));
        public static final RegistryObject<Item> DIAMOND_WAND = registerItem("diamond_wand",
                        props -> new ItemWandBasic(props, Tiers.DIAMOND));
        public static final RegistryObject<Item> NETHERITE_WAND = registerItem("netherite_wand",
                        props -> new ItemWandBasic(props.fireResistant(), Tiers.NETHERITE));
        public static final RegistryObject<Item> INFINITY_WAND = registerItem("infinity_wand",
                        props -> new ItemWandInfinity(props.stacksTo(1)));

        // Cores
        public static final RegistryObject<Item> CORE_ANGEL = registerItem("core_angel",
                        props -> new ItemCoreAngel(props.stacksTo(1)));
        public static final RegistryObject<Item> CORE_DESTRUCTION = registerItem("core_destruction",
                        props -> new ItemCoreDestruction(props.stacksTo(1)));

        private static RegistryObject<Item> registerItem(String name, Function<Item.Properties, Item> factory) {
                RegistryObject<Item> item = REConstructedWandsForge.ITEMS.register(name,
                                () -> factory.apply(new Item.Properties()));
                ALL_ITEMS.add(item);
                return item;
        }

        public static void register() {
                // Called to trigger static initialization
        }

        public static List<RegistryObject<Item>> getAllItems() {
                return ALL_ITEMS;
        }
}

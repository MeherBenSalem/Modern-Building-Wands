package com.nigthbeam.reconstructedwands;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.nigthbeam.reconstructedwands.crafting.ModRecipes;
import com.nigthbeam.reconstructedwands.crafting.RecipeWandUpgrade;

import java.util.function.Supplier;

/**
 * NeoForge mod entrypoint for reconstructedwands.
 */
@Mod(Constants.MOD_ID)
public class REConstructedWandsNeoForge {

        public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);
        public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
                        .create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);
        public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
                        .create(Registries.RECIPE_SERIALIZER, Constants.MOD_ID);

        public static REConstructedWandsNeoForge instance;
        public com.nigthbeam.reconstructedwands.client.RenderBlockPreview renderBlockPreview;

        // Recipe serializers
        public static final Supplier<RecipeSerializer<RecipeWandUpgrade>> WAND_UPGRADE_SERIALIZER = RECIPE_SERIALIZERS
                        .register("wand_upgrade", ModRecipes::createWandUpgradeSerializer);

        public static final Supplier<CreativeModeTab> WAND_TAB = CREATIVE_MODE_TABS.register("wands_tab",
                        () -> CreativeModeTab.builder()
                                        .title(net.minecraft.network.chat.Component
                                                        .translatable("creativetab.reconstructedwands.wands_tab"))
                                        .icon(() -> ModItems.INFINITY_WAND.get().getDefaultInstance())
                                        .displayItems((parameters, output) -> {
                                                ModItems.getAllItems().forEach(holder -> output.accept(holder.get()));
                                        }).build());

        public REConstructedWandsNeoForge(IEventBus modEventBus) {
                instance = this;
                // Initialize common code
                REConstructedWands.init();

                // Register items
                ModItems.register();

                // Register DeferredRegister
                ITEMS.register(modEventBus);
                CREATIVE_MODE_TABS.register(modEventBus);
                RECIPE_SERIALIZERS.register(modEventBus);

                // Register event listeners
                modEventBus.addListener(this::commonSetup);
                modEventBus.addListener(this::clientSetup);
                modEventBus.addListener(this::registerKeyMappings);

                modEventBus.addListener(com.nigthbeam.reconstructedwands.network.ModMessages::register);

                net.neoforged.fml.ModLoadingContext.get().getActiveContainer().registerConfig(
                                net.neoforged.fml.config.ModConfig.Type.CLIENT,
                                com.nigthbeam.reconstructedwands.basics.ConfigClient.SPEC);
        }

        private void commonSetup(final FMLCommonSetupEvent event) {
                // Set the serializer reference for cross-platform access
                ModRecipes.setWandUpgradeSerializer(WAND_UPGRADE_SERIALIZER.get());
                Constants.LOG.info("REConstructedWands NeoForge common setup");
        }

        private void clientSetup(final net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
                renderBlockPreview = new com.nigthbeam.reconstructedwands.client.RenderBlockPreview();
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(renderBlockPreview);
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS
                                .register(new com.nigthbeam.reconstructedwands.client.ClientEvents());
        }

        private void registerKeyMappings(final RegisterKeyMappingsEvent event) {
                event.register(com.nigthbeam.reconstructedwands.client.ClientEvents.WAND_GUI_KEY);
        }
}

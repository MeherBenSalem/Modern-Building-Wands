package com.nigthbeam.reconstructedwands;

import com.nigthbeam.reconstructedwands.basics.ConfigClient;
import com.nigthbeam.reconstructedwands.client.ClientEvents;
import com.nigthbeam.reconstructedwands.client.RenderBlockPreview;
import com.nigthbeam.reconstructedwands.crafting.ModRecipes;
import com.nigthbeam.reconstructedwands.crafting.RecipeWandUpgrade;
import com.nigthbeam.reconstructedwands.network.ModMessages;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(Constants.MOD_ID)
public class REConstructedWandsForge {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Constants.MOD_ID);

    public static final RegistryObject<RecipeSerializer<RecipeWandUpgrade>> WAND_UPGRADE_SERIALIZER =
            RECIPE_SERIALIZERS.register("wand_upgrade", ModRecipes::createWandUpgradeSerializer);
    public static final RegistryObject<CreativeModeTab> WAND_TAB = CREATIVE_MODE_TABS.register("wands_tab",
            () -> CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("creativetab.reconstructedwands.wands_tab"))
                    .icon(() -> ModItems.INFINITY_WAND.get().getDefaultInstance())
                    .displayItems((parameters, output) -> ModItems.getAllItems().forEach(item -> output.accept(item.get())))
                    .build());

    public static REConstructedWandsForge instance;
    public RenderBlockPreview renderBlockPreview;

    public REConstructedWandsForge() {
        instance = this;
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        REConstructedWands.init();
        ModItems.register();
        ITEMS.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::registerKeyMappings);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigClient.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModRecipes.setWandUpgradeSerializer(WAND_UPGRADE_SERIALIZER.get());
            ModMessages.register();
        });
    }

    private void clientSetup(FMLClientSetupEvent event) {
        renderBlockPreview = new RenderBlockPreview();
        MinecraftForge.EVENT_BUS.register(renderBlockPreview);
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ClientEvents.WAND_GUI_KEY);
    }
}

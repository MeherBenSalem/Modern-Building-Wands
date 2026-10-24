package com.nightbeam.reconstructedwands.item;

import com.nightbeam.reconstructedwands.client.ClientBridge;
import com.nightbeam.reconstructedwands.config.WandsConfig;
import com.nightbeam.reconstructedwands.network.Network;
import com.nightbeam.reconstructedwands.wand.Executor;
import com.nightbeam.reconstructedwands.wand.Planner;
import com.nightbeam.reconstructedwands.wand.WandMode;
import com.nightbeam.reconstructedwands.wand.WandSettings;
import com.nightbeam.reconstructedwands.wand.supply.Supply;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public final class WandItem extends Item {
    private final WandTier tier;

    public WandItem(WandTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public WandTier tier() {
        return tier;
    }

    public boolean acceptsCores() {
        return tier.stats().cores();
    }

    @Override
    public int getEnchantmentValue() {
        return tier == WandTier.STONE ? 5 : tier == WandTier.IRON ? 14 : 10;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        TagKey<Item> material = switch (tier) {
            case STONE -> ItemTags.STONE_TOOL_MATERIALS;
            case IRON -> common("ingots/iron");
            case DIAMOND -> common("gems/diamond");
            case NETHERITE -> common("ingots/netherite");
            case INFINITY -> null;
        };
        return material != null && repair.is(material);
    }

    private static TagKey<Item> common(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ItemStack wand = context.getItemInHand();
        if (player.isShiftKeyDown() && Network.optionDown(serverPlayer)) {
            return Executor.undo(serverPlayer) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        if (player.isShiftKeyDown() && toggleBinding(serverPlayer, wand, context.getClickedPos())) {
            return InteractionResult.SUCCESS;
        }
        BlockHitResult hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
        return Executor.use(serverPlayer, (ServerLevel) level, wand, context.getHand(), hit) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack wand = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (level.isClientSide()) {
                if (!ClientBridge.optionKeyDown() && ClientBridge.sneakOpensScreen()) {
                    ClientBridge.openScreen(wand);
                }
                return InteractionResultHolder.success(wand);
            }
            if (Network.optionDown((ServerPlayer) player)) {
                Executor.undo((ServerPlayer) player);
            }
            return InteractionResultHolder.success(wand);
        }
        WandMode mode = WandSettings.of(wand).mode();
        if (mode != WandMode.ANGEL && mode != WandMode.ARCHITECT) {
            return InteractionResultHolder.pass(wand);
        }
        if (level.isClientSide()) {
            return InteractionResultHolder.success(wand);
        }
        return Executor.use((ServerPlayer) player, (ServerLevel) level, wand, hand, null)
                ? InteractionResultHolder.success(wand) : InteractionResultHolder.fail(wand);
    }

    /** Sneak + right-click a container: bind the wand to it as a block source, or unbind it. */
    private static boolean toggleBinding(ServerPlayer player, ItemStack wand, BlockPos pos) {
        if (!WandsConfig.server().boundContainers || Supply.containerAt(player.level(), pos) == null) {
            return false;
        }
        WandSettings settings = WandSettings.of(wand);
        WandSettings.Spot bound = settings.bound();
        String dimension = Supply.dimensionId(player.level());
        boolean unbind = bound != null && bound.pos().equals(pos) && bound.dimension().equals(dimension);
        settings.setBound(unbind ? null : pos, dimension);
        settings.save();
        player.level().playSound(null, pos, unbind ? SoundEvents.CHAIN_BREAK : SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 0.8F, 1.2F);
        player.sendSystemMessage(Component.translatable(unbind ? "reconstructedwands.message.unbound" : "reconstructedwands.message.bound",
                pos.getX(), pos.getY(), pos.getZ()), true);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        WandSettings settings = WandSettings.of(stack);
        WandMode mode = settings.mode();
        lines.add(Component.translatable("reconstructedwands.tooltip.mode", Component.translatable(mode.translationKey()))
                .withStyle(ChatFormatting.GRAY));
        if (mode == WandMode.ANGEL) {
            lines.add(Component.translatable("reconstructedwands.tooltip.reach", tier.stats().angel()).withStyle(ChatFormatting.GRAY));
        } else {
            int max = switch (mode) {
                case DESTRUCTION -> tier.stats().destruction();
                case EXCHANGE -> tier.stats().exchange();
                default -> tier.stats().construction();
            };
            int size = settings.size();
            lines.add(Component.translatable("reconstructedwands.tooltip.size", size > 0 ? Math.min(size, max) : max, max)
                    .withStyle(ChatFormatting.GRAY));
        }
        if (settings.modes().size() > 1) {
            MutableComponent cores = Component.empty();
            boolean first = true;
            for (WandMode core : settings.modes()) {
                if (core == WandMode.CONSTRUCTION) {
                    continue;
                }
                if (!first) {
                    cores.append(", ");
                }
                cores.append(Component.translatable(core.translationKey()));
                first = false;
            }
            lines.add(Component.translatable("reconstructedwands.tooltip.cores", cores).withStyle(ChatFormatting.GRAY));
        } else if (!acceptsCores()) {
            lines.add(Component.translatable("reconstructedwands.tooltip.no_cores").withStyle(ChatFormatting.DARK_GRAY));
        }
        if (!ClientBridge.shiftDown()) {
            lines.add(Component.translatable("reconstructedwands.tooltip.shift").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        for (WandSettings.Setting setting : new WandSettings.Setting[] {WandSettings.Setting.LOCK, WandSettings.Setting.DIRECTION,
                WandSettings.Setting.REPLACE, WandSettings.Setting.MATCH, WandSettings.Setting.RANDOM}) {
            lines.add(Component.translatable(setting.translationKey()).withStyle(ChatFormatting.AQUA).append(": ")
                    .append(Component.translatable(valueKey(setting, settings.value(setting))).withStyle(ChatFormatting.WHITE)));
        }
        if (settings.hasCore(WandMode.ARCHITECT)) {
            lines.add(Component.translatable(WandSettings.Setting.SHAPE.translationKey()).withStyle(ChatFormatting.AQUA).append(": ")
                    .append(Component.translatable(settings.shape().translationKey()).withStyle(ChatFormatting.WHITE)));
        }
        WandSettings.Spot bound = settings.bound();
        if (bound != null) {
            lines.add(Component.translatable("reconstructedwands.tooltip.bound", bound.pos().getX(), bound.pos().getY(), bound.pos().getZ())
                    .withStyle(ChatFormatting.GOLD));
        }
        lines.add(Component.translatable("reconstructedwands.tooltip.controls").withStyle(ChatFormatting.DARK_GRAY));
    }

    /** The translation key of a setting's value ("reconstructedwands.option.lock.nolock"). */
    public static String valueKey(WandSettings.Setting setting, int value) {
        String name = switch (setting) {
            case MODE -> WandMode.values()[value].key();
            case LOCK -> WandSettings.Lock.values()[value].name();
            case DIRECTION -> WandSettings.Facing.values()[value].name();
            case MATCH -> WandSettings.Match.values()[value].name();
            case REPLACE, RANDOM -> value == 1 ? "on" : "off";
            case SHAPE -> com.nightbeam.reconstructedwands.wand.Shape.values()[value].key();
            case SIZE, UNBIND -> String.valueOf(value);
        };
        return setting.translationKey() + "." + name.toLowerCase(java.util.Locale.ROOT);
    }

    /** The limit one click has right now (for the HUD and the screen). */
    public int limitFor(Player player, ItemStack wand) {
        WandSettings settings = WandSettings.of(wand);
        return Planner.limit(player, tier, settings.mode(), settings.size(), wand);
    }
}

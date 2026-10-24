package com.nightbeam.reconstructedwands.client;

import com.nightbeam.reconstructedwands.config.WandsConfig;
import com.nightbeam.reconstructedwands.item.WandItem;
import com.nightbeam.reconstructedwands.wand.Plan;
import com.nightbeam.reconstructedwands.wand.WandMode;
import com.nightbeam.reconstructedwands.wand.WandSettings;
import com.nightbeam.reconstructedwands.wand.supply.Supply;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/** A small panel right of the hotbar while a wand is held: mode, size, the block and how many you have. */
public final class WandHud {
    static final int[] MODE_COLORS = {0xFF7FE8FF, 0xFFFFE082, 0xFFFF8A65, 0xFFB388FF, 0xFF80CBC4};

    private WandHud() {
    }

    public static int modeColor(WandMode mode) {
        return MODE_COLORS[mode.ordinal()];
    }

    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!WandsConfig.client().hud || minecraft.player == null || minecraft.options.hideGui || minecraft.screen != null) {
            return;
        }
        ItemStack wand = Preview.heldWand(minecraft.player);
        if (wand == null) {
            return;
        }
        WandItem item = (WandItem) wand.getItem();
        WandSettings settings = WandSettings.of(wand);
        WandMode mode = settings.mode();
        Plan plan = Preview.plan();
        Font font = minecraft.font;

        int x = graphics.guiWidth() / 2 + 91 + 8;
        int y = graphics.guiHeight() - 22;
        int width = 132;
        graphics.fill(x - 2, y - 2, x + width, y + 21, 0x90101418);
        graphics.fill(x - 2, y - 2, x, y + 21, modeColor(mode));

        Component title = Component.translatable(mode.translationKey());
        if (mode == WandMode.ARCHITECT) {
            title = Component.translatable("reconstructedwands.hud.architect", title, Component.translatable(settings.shape().translationKey()));
        }
        graphics.drawString(font, title, x + 3, y, modeColor(mode), true);
        int limit = item.limitFor(minecraft.player, wand);
        int count = plan == null ? 0 : plan.targets.size();
        String counter = count + "/" + limit;
        graphics.drawString(font, counter, x + width - 3 - font.width(counter), y, 0xFFE0E0E0, true);

        if (plan != null && plan.problem != null) {
            graphics.drawString(font, font.substrByWidth(plan.problem, width - 6).getString(), x + 3, y + 11, 0xFFFF6B6B, true);
            return;
        }
        if (mode == WandMode.ARCHITECT && plan != null && plan.anchor == null) {
            graphics.drawString(font, Component.translatable("reconstructedwands.hud.first_corner"), x + 3, y + 11, 0xFFB0B0B0, true);
            return;
        }
        BlockItem block = plan == null ? null : plan.mainItem();
        if (block == null || mode == WandMode.DESTRUCTION) {
            if (mode == WandMode.DESTRUCTION && plan != null && !plan.targets.isEmpty()) {
                graphics.drawString(font, Component.translatable("reconstructedwands.hud.remove", plan.targets.size()), x + 3, y + 11, 0xFFFFB27A, true);
            }
            return;
        }
        ItemStack icon = new ItemStack(block);
        graphics.pose().pushPose();
        graphics.pose().translate(x + 1, y + 9, 0);
        graphics.pose().scale(0.75F, 0.75F, 1);
        graphics.renderFakeItem(icon, 0, 0);
        graphics.pose().popPose();
        Supply supply = Supply.of(minecraft.player, settings);
        int have = supply.count(block);
        String haveText = supply.infinite() ? "∞" : String.valueOf(have);
        Component line = Component.translatable("reconstructedwands.hud.blocks", haveText);
        int color = plan.missing.isEmpty() ? 0xFFB0B0B0 : 0xFFFF6B6B;
        graphics.drawString(font, line, x + 16, y + 11, color, true);
    }
}

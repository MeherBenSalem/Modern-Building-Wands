package com.nightbeam.reconstructedwands.client;

import com.nightbeam.reconstructedwands.config.WandsConfig;
import com.nightbeam.reconstructedwands.content.ModContent;
import com.nightbeam.reconstructedwands.network.SettingPayload;
import com.nightbeam.reconstructedwands.wand.WandMode;
import com.nightbeam.reconstructedwands.wand.WandSettings;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Hold the wheel key, point at a mode, let go. */
public final class ModeWheelScreen extends Screen {
    private static final int RADIUS = 64;
    private static final int TILE = 44;

    private final ItemStack wand;
    private final List<WandMode> modes;
    private @Nullable WandMode hovered;

    public ModeWheelScreen(ItemStack wand) {
        super(Component.translatable("reconstructedwands.screen.wheel"));
        this.wand = wand;
        this.modes = WandSettings.of(wand).modes();
    }

    private int centerX() {
        return width / 2;
    }

    private int centerY() {
        return height / 2;
    }

    private int tileX(int index) {
        double angle = -Math.PI / 2 + index * 2 * Math.PI / modes.size();
        return centerX() + (int) Math.round(Math.cos(angle) * RADIUS);
    }

    private int tileY(int index) {
        double angle = -Math.PI / 2 + index * 2 * Math.PI / modes.size();
        return centerY() + (int) Math.round(Math.sin(angle) * RADIUS);
    }

    private @Nullable WandMode modeAt(double mouseX, double mouseY) {
        double dx = mouseX - centerX();
        double dy = mouseY - centerY();
        if (dx * dx + dy * dy < 14 * 14) {
            return null;
        }
        double angle = Math.atan2(dy, dx) + Math.PI / 2;
        double step = 2 * Math.PI / modes.size();
        int index = (int) Math.floor((angle + step / 2) / step);
        return modes.get(Math.floorMod(index, modes.size()));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        hovered = modeAt(mouseX, mouseY);
        WandMode selected = WandSettings.of(wand).selectedMode();
        for (int i = 0; i < modes.size(); i++) {
            WandMode mode = modes.get(i);
            int x = tileX(i) - TILE / 2;
            int y = tileY(i) - TILE / 2;
            int color = WandHud.modeColor(mode);
            boolean lit = mode == hovered;
            boolean enabled = WandsConfig.server().enabled(mode);
            graphics.fill(x, y, x + TILE, y + TILE, lit ? 0x60000000 | (color & 0xFFFFFF) : 0xB0101418);
            graphics.outline(x, y, TILE, TILE, mode == selected ? color : lit ? 0xFFFFFFFF : 0xFF3A4450);
            ItemStack icon = mode.core() == null ? new ItemStack(wand.getItem()) : new ItemStack(ModContent.core(mode));
            graphics.fakeItem(icon, x + TILE / 2 - 8, y + 6);
            Component name = Component.translatable(mode.translationKey());
            graphics.pose().pushMatrix();
            graphics.pose().translate(x + TILE / 2.0F, y + TILE - 9);
            float scale = Math.min(0.75F, (TILE - 4) / (float) Math.max(1, font.width(name)));
            graphics.pose().scale(scale, scale);
            graphics.centeredText(font, name, 0, 0, enabled ? (lit ? 0xFFFFFFFF : 0xFFC8C8C8) : 0xFF707070);
            graphics.pose().popMatrix();
        }
        WandMode shown = hovered != null ? hovered : selected;
        graphics.centeredText(font, Component.translatable(shown.translationKey()), centerX(), centerY() - 8, WandHud.modeColor(shown));
        graphics.centeredText(font, Component.translatable("reconstructedwands.screen.wheel_hint"), centerX(), centerY() + RADIUS + TILE / 2 + 8,
                0xFFA0A0A0);
    }

    private void choose() {
        if (hovered != null && WandsConfig.server().enabled(hovered) && hovered != WandSettings.of(wand).selectedMode()) {
            WandSettings settings = WandSettings.of(wand);
            if (settings.apply(WandSettings.Setting.MODE, hovered.ordinal())) {
                settings.save();
            }
            WandsClient.send(new SettingPayload(WandSettings.Setting.MODE.ordinal(), hovered.ordinal()));
        }
        onClose();
    }

    @Override
    public void tick() {
        if (!WandsClient.wheelKeyDown()) {
            choose();
        }
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (WandsClient.WHEEL_KEY.matches(event)) {
            choose();
            return true;
        }
        return super.keyReleased(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        hovered = modeAt(event.x(), event.y());
        choose();
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

package com.nightbeam.reconstructedwands.client;

import com.nightbeam.reconstructedwands.config.WandsConfig;
import com.nightbeam.reconstructedwands.content.ModContent;
import com.nightbeam.reconstructedwands.item.WandItem;
import com.nightbeam.reconstructedwands.network.SettingPayload;
import com.nightbeam.reconstructedwands.wand.Planner;
import com.nightbeam.reconstructedwands.wand.WandMode;
import com.nightbeam.reconstructedwands.wand.WandSettings;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/** The wand's options: modes, settings, size and the bound container. Every change is sent to the server at once. */
public final class WandScreen extends Screen {
    private static final int WIDTH = 316;
    private static final int HEIGHT = 222;

    private final ItemStack wand;
    private int left;
    private int top;

    public WandScreen(ItemStack wand) {
        super(wand.getHoverName());
        this.wand = wand;
    }

    private WandSettings settings() {
        return WandSettings.of(wand);
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;
        WandSettings settings = settings();

        List<WandMode> modes = settings.modes();
        int tileWidth = 58;
        int gap = 4;
        int rowWidth = modes.size() * tileWidth + (modes.size() - 1) * gap;
        int x = left + (WIDTH - rowWidth) / 2;
        for (WandMode mode : modes) {
            addRenderableWidget(new ModeTile(x, top + 26, tileWidth, 40, mode));
            x += tileWidth + gap;
        }

        int col = (WIDTH - 24 - 8) / 2;
        int rowY = top + 76;
        addSetting(WandSettings.Setting.LOCK, left + 12, rowY, col);
        addSetting(WandSettings.Setting.DIRECTION, left + 20 + col, rowY, col);
        addSetting(WandSettings.Setting.MATCH, left + 12, rowY + 24, col);
        addSetting(WandSettings.Setting.REPLACE, left + 20 + col, rowY + 24, col);
        addSetting(WandSettings.Setting.RANDOM, left + 12, rowY + 48, col);
        if (settings.hasCore(WandMode.ARCHITECT)) {
            addSetting(WandSettings.Setting.SHAPE, left + 20 + col, rowY + 48, col);
        }
        addRenderableWidget(new SizeSlider(left + 12, rowY + 74, WIDTH - 24, 20));
        if (settings.bound() != null) {
            addRenderableWidget(Button.builder(Component.translatable("reconstructedwands.screen.unbind"), button -> {
                send(WandSettings.Setting.UNBIND, 0);
                rebuild();
            }).bounds(left + WIDTH - 12 - 70, top + 175, 70, 18).build());
        }
    }

    private void rebuild() {
        clearWidgets();
        init();
    }

    private void addSetting(WandSettings.Setting setting, int x, int y, int width) {
        addRenderableWidget(Button.builder(label(setting), button -> {
            WandSettings settings = settings();
            send(setting, settings.cycled(setting, hasShiftDown()));
            button.setMessage(label(setting));
        }).bounds(x, y, width, 20).tooltip(Tooltip.create(Component.translatable(setting.translationKey() + ".desc"))).build());
    }

    private Component label(WandSettings.Setting setting) {
        WandSettings settings = settings();
        return Component.translatable(setting.translationKey()).append(": ")
                .append(Component.translatable(WandItem.valueKey(setting, settings.value(setting))).withStyle(ChatFormatting.YELLOW));
    }

    /** Applies the change to the client's copy (so the screen updates at once) and asks the server to do the same. */
    private void send(WandSettings.Setting setting, int value) {
        WandSettings settings = settings();
        if (settings.apply(setting, value)) {
            settings.save();
        }
        WandsClient.send(new SettingPayload(setting.ordinal(), value));
    }

    private static boolean hasShiftDown() {
        return Minecraft.getInstance().hasShiftDown();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.fill(left, top, left + WIDTH, top + HEIGHT, 0xE0101418);
        graphics.outline(left, top, WIDTH, HEIGHT, 0xFF3A4450);
        WandMode mode = settings().mode();
        graphics.fill(left, top, left + WIDTH, top + 2, WandHud.modeColor(mode));
        graphics.fakeItem(wand, left + 8, top + 6);
        graphics.text(font, title, left + 28, top + 10, 0xFFFFFFFF, true);
        WandSettings.Spot bound = settings().bound();
        if (bound != null) {
            graphics.text(font, Component.translatable("reconstructedwands.screen.bound", bound.pos().getX(), bound.pos().getY(), bound.pos().getZ()),
                    left + 12, top + 180, 0xFFFFC66D, true);
        } else {
            graphics.text(font, Component.translatable("reconstructedwands.screen.bind_hint"), left + 12, top + 180, 0xFF8A949E, false);
        }
        graphics.text(font, Component.translatable("reconstructedwands.screen.controls"), left + 12, top + 197, 0xFF8A949E, false);
        graphics.text(font, Component.translatable("reconstructedwands.screen.controls2"), left + 12, top + 208, 0xFF8A949E, false);
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (minecraft != null && (minecraft.options.keyInventory.matches(event) || WandsClient.SCREEN_KEY.matches(event))) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** One mode: the core's icon and name; the selected one is lit in the mode's colour. */
    private final class ModeTile extends AbstractButton {
        private final WandMode mode;

        ModeTile(int x, int y, int width, int height, WandMode mode) {
            super(x, y, width, height, Component.translatable(mode.translationKey()));
            this.mode = mode;
            if (!WandsConfig.server().enabled(mode)) {
                active = false;
                setTooltip(Tooltip.create(Component.translatable("reconstructedwands.screen.mode_disabled")));
            } else {
                setTooltip(Tooltip.create(Component.translatable(mode.translationKey() + ".desc")));
            }
        }

        @Override
        public void onPress(InputWithModifiers input) {
            send(WandSettings.Setting.MODE, mode.ordinal());
            rebuild();
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            boolean selected = settings().selectedMode() == mode;
            int color = WandHud.modeColor(mode);
            graphics.fill(getX(), getY(), getX() + width, getY() + height, selected ? 0x40000000 | (color & 0xFFFFFF) : 0x30FFFFFF);
            graphics.outline(getX(), getY(), width, height, selected ? color : isHoveredOrFocused() ? 0xFFB0B0B0 : 0xFF404850);
            ItemStack icon = mode.core() == null ? new ItemStack(wand.getItem()) : new ItemStack(ModContent.core(mode));
            graphics.fakeItem(icon, getX() + width / 2 - 8, getY() + 5);
            int textColor = active ? (selected ? color : 0xFFE0E0E0) : 0xFF707070;
            // Long names ("Construction") are drawn smaller so they stay inside the tile.
            float scale = Math.min(1.0F, (width - 6) / (float) Math.max(1, font.width(getMessage())));
            graphics.pose().pushMatrix();
            graphics.pose().translate(getX() + width / 2.0F, getY() + height - 13);
            graphics.pose().scale(scale, scale);
            graphics.centeredText(font, getMessage(), 0, 0, textColor);
            graphics.pose().popMatrix();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    /** The size: from 1 to the wand's limit for the current mode; the right end means "as many as the wand allows". */
    private final class SizeSlider extends AbstractSliderButton {
        SizeSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), 0.0);
            int max = max();
            int size = settings().size();
            value = size <= 0 || max <= 1 ? 1.0 : (double) (Math.min(size, max) - 1) / (max - 1);
            updateMessage();
        }

        private int max() {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return 1;
            }
            return Math.max(1, Planner.maxSize(minecraft.player, ((WandItem) wand.getItem()).tier(), settings().mode()));
        }

        private int size() {
            int max = max();
            return value >= 1.0 ? 0 : 1 + (int) Math.round(value * (max - 1));
        }

        @Override
        protected void updateMessage() {
            int size = size();
            Component shown = size == 0 ? Component.translatable("reconstructedwands.screen.size_max", max())
                    : Component.literal(String.valueOf(size));
            setMessage(Component.translatable("reconstructedwands.option.size").append(": ").append(shown.copy().withStyle(ChatFormatting.YELLOW)));
        }

        @Override
        protected void applyValue() {
            value = Mth.clamp(value, 0.0, 1.0);
            send(WandSettings.Setting.SIZE, size());
        }
    }
}

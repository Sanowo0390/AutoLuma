package io.github.mintymc.autoluma;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/** Vanilla-only settings UI; no Cloth Config or YACL dependency. */
public final class AutoLumaScreen extends Screen {
    private final Screen parent;
    private final AutoLumaConfig cfg = AutoLumaClient.config();
    private Button enabledButton;
    private Button forceButton;
    private int startY;

    public AutoLumaScreen(Screen parent) {
        super(Component.translatable("autoluma.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = Math.min(310, Math.max(180, this.width - 24));
        int x = (this.width - w) / 2;
        startY = Math.max(49, (this.height - 245) / 2 + 50);

        enabledButton = addRenderableWidget(Button.builder(enabledText(), button -> {
            cfg.enabled = !cfg.enabled;
            enabledButton.setMessage(enabledText());
        }).bounds(x, startY, w, 20).build());

        forceButton = addRenderableWidget(Button.builder(forceText(), button -> {
            cfg.forceBoost = !cfg.forceBoost;
            forceButton.setMessage(forceText());
        }).bounds(x, startY + 25, w, 20).build());

        addRenderableWidget(new RangeSlider(x, startY + 54, w, cfg.boostPercent,
            0, 1500, 25, val -> cfg.boostPercent = val,
            val -> Component.translatable("autoluma.boost", val)));

        addRenderableWidget(new RangeSlider(x, startY + 81, w, cfg.darkThreshold,
            0, 14, 1, val -> cfg.darkThreshold = val,
            val -> Component.translatable("autoluma.threshold", val)));

        addRenderableWidget(new RangeSlider(x, startY + 108, w, cfg.transitionTicks,
            1, 60, 1, val -> cfg.transitionTicks = val,
            val -> Component.translatable("autoluma.speed", val)));

        addRenderableWidget(Button.builder(Component.translatable("autoluma.done"), button -> onClose())
            .bounds(x, Math.min(startY + 176, height - 24), w, 20).build());
    }

    private Component enabledText() {
        return Component.translatable("autoluma.enabled",
            Component.translatable(cfg.enabled ? "autoluma.on" : "autoluma.off"));
    }

    private Component forceText() {
        return Component.translatable("autoluma.force",
            Component.translatable(cfg.forceBoost ? "autoluma.on" : "autoluma.off"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 22, 0xFFFFFFFF);
        graphics.centeredText(font, Component.translatable("autoluma.description"), width / 2, 35, 0xFFB0B0B0);

        graphics.centeredText(font, Component.translatable("autoluma.diagnostic",
            AutoLumaClient.sampledLight(), AutoLumaClient.sampledBlock(),
            AutoLumaClient.sampledSky(), AutoLumaClient.sampledSkyDarken()),
            width / 2, startY + 142, 0xFFB0B0B0);
        graphics.centeredText(font, Component.translatable("autoluma.diagnostic_boost",
            Math.round(AutoLumaClient.bonusGamma() * 100.0f),
            Component.translatable(AutoLumaClient.isDark() ? "autoluma.on" : "autoluma.off")),
            width / 2, startY + 154, 0xFFB0B0B0);
    }

    @Override
    public void onClose() {
        cfg.save();
        this.minecraft.gui.setScreen(parent);
    }

    private static final class RangeSlider extends AbstractSliderButton {
        private final int min, max, step;
        private final IntConsumer set;
        private final IntFunction<Component> label;

        RangeSlider(int x, int y, int width, int initial, int min, int max, int step,
                    IntConsumer set, IntFunction<Component> label) {
            super(x, y, width, 20, label.apply(initial), (double) (initial - min) / (max - min));
            this.min = min;
            this.max = max;
            this.step = step;
            this.set = set;
            this.label = label;
            updateMessage();
        }

        private int selected() {
            int steps = Math.round((float) ((max - min) * this.value / step));
            return Math.min(max, min + steps * step);
        }

        @Override
        protected void updateMessage() {
            this.setMessage(label.apply(selected()));
        }

        @Override
        protected void applyValue() {
            this.set.accept(selected());
        }
    }
}

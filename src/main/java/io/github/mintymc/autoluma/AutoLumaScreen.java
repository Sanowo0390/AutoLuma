package io.github.mintymc.autoluma;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/** Uses vanilla widgets, so Cloth Config and YACL are not required. */
public final class AutoLumaScreen extends Screen {
    private final Screen parent;
    private final AutoLumaConfig cfg = AutoLumaClient.config();
    private Button enabledButton;

    public AutoLumaScreen(Screen parent) {
        super(Component.translatable("autoluma.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = Math.min(290, Math.max(180, this.width - 24));
        int x = (this.width - w) / 2;
        int y = Math.max(45, (this.height - 195) / 2 + 45);

        enabledButton = addRenderableWidget(Button.builder(enabledText(), button -> {
            cfg.enabled = !cfg.enabled;
            enabledButton.setMessage(enabledText());
        }).bounds(x, y, w, 20).build());

        addRenderableWidget(new RangeSlider(x, y + 27, w, cfg.boostPercent,
            0, 1500, 25, val -> cfg.boostPercent = val,
            val -> Component.translatable("autoluma.boost", val)));

        addRenderableWidget(new RangeSlider(x, y + 54, w, cfg.darkThreshold,
            0, 13, 1, val -> cfg.darkThreshold = val,
            val -> Component.translatable("autoluma.threshold", val)));

        addRenderableWidget(new RangeSlider(x, y + 81, w, cfg.transitionTicks,
            1, 60, 1, val -> cfg.transitionTicks = val,
            val -> Component.translatable("autoluma.speed", val)));

        addRenderableWidget(Button.builder(Component.translatable("autoluma.done"), button -> onClose())
            .bounds(x, Math.min(y + 122, height - 27), w, 20).build());
    }

    private Component enabledText() {
        return Component.translatable("autoluma.enabled",
            Component.translatable(cfg.enabled ? "autoluma.on" : "autoluma.off"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 22, 0xFFFFFFFF);
        graphics.centeredText(font, Component.translatable("autoluma.description"), width / 2, 35, 0xFFB0B0B0);
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

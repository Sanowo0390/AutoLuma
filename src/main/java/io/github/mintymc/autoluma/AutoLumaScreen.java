package io.github.mintymc.autoluma;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/** Mod Menu settings + diagnostic counters; no additional config GUI library. */
public final class AutoLumaScreen extends Screen {
    private final Screen parent;
    private final AutoLumaConfig config = AutoLumaClient.config();
    private Button enabledButton, forceButton, modeButton;
    private int controlsY;

    public AutoLumaScreen(Screen parent) {
        super(Component.translatable("autoluma.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int width = Math.min(310, Math.max(160, this.width - 28));
        int x = (this.width - width) / 2;
        controlsY = Math.max(45, (this.height - 254) / 2 + 48);

        enabledButton = addRenderableWidget(Button.builder(enabledText(), button -> {
            config.enabled = !config.enabled;
            enabledButton.setMessage(enabledText());
            preview();
        }).bounds(x, controlsY, width, 20).build());

        forceButton = addRenderableWidget(Button.builder(forceText(), button -> {
            config.forceBoost = !config.forceBoost;
            forceButton.setMessage(forceText());
            preview();
        }).bounds(x, controlsY + 23, width, 20).build());

        modeButton = addRenderableWidget(Button.builder(modeText(), button -> {
            AutoLumaConfig.RenderMode[] modes = AutoLumaConfig.RenderMode.values();
            config.renderMode = modes[(config.renderMode.ordinal() + 1) % modes.length];
            modeButton.setMessage(modeText());
            preview();
        }).bounds(x, controlsY + 46, width, 20).build());

        addRenderableWidget(new RangeSlider(x, controlsY + 73, width, config.strengthPercent,
                0, 100, 1, value -> config.strengthPercent = value,
                value -> Component.translatable("autoluma.strength", value)));

        addRenderableWidget(new RangeSlider(x, controlsY + 100, width, config.darkThreshold,
                0, 14, 1, value -> config.darkThreshold = value,
                value -> Component.translatable("autoluma.threshold", value)));

        addRenderableWidget(new RangeSlider(x, controlsY + 127, width, config.transitionTicks,
                1, 60, 1, value -> config.transitionTicks = value,
                value -> Component.translatable("autoluma.speed", value)));

        addRenderableWidget(Button.builder(Component.translatable("autoluma.done"),
                button -> onClose()).bounds(x, Math.min(controlsY + 207, height - 24), width, 20).build());
    }

    private Component enabledText() {
        return Component.translatable("autoluma.enabled",
            Component.translatable(config.enabled ? "autoluma.on" : "autoluma.off"));
    }

    private Component forceText() {
        return Component.translatable("autoluma.force",
            Component.translatable(config.forceBoost ? "autoluma.on" : "autoluma.off"));
    }

    private Component modeText() {
        return Component.translatable("autoluma.render_mode",
            Component.translatable("autoluma.mode." + config.renderMode.name().toLowerCase(java.util.Locale.ROOT)));
    }

    private void preview() {
        AutoLumaClient.previewSettings();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 18, 0xFFFFFFFF);
        graphics.centeredText(font, Component.translatable("autoluma.description"), width / 2, 31, 0xFFB0B0B0);

        graphics.centeredText(font, Component.translatable("autoluma.diagnostic",
            AutoLumaClient.sampledLight(), AutoLumaClient.sampledBlock(),
            AutoLumaClient.sampledSky(), AutoLumaClient.sampledSkyDarken()),
            width / 2, controlsY + 158, 0xFFC0C0C0);

        graphics.centeredText(font, Component.translatable("autoluma.diagnostic_strength",
            Math.round(AutoLumaClient.intensity() * 100.0f), AutoLumaClient.postLevel(),
            Component.translatable(AutoLumaClient.shadersActive() ? "autoluma.on" : "autoluma.off")),
            width / 2, controlsY + 170, 0xFFC0C0C0);

        if (height >= 290) {
            graphics.centeredText(font, Component.translatable("autoluma.diagnostic_render",
                AutoLumaClient.gammaHookCalls(), AutoLumaClient.nightVisionHookCalls(),
                AutoLumaClient.postRequestCalls()), width / 2, controlsY + 182, 0xFF999999);
        }
    }

    @Override
    public void onClose() {
        config.save();
        this.minecraft.gui.setScreen(parent);
    }

    private static final class RangeSlider extends AbstractSliderButton {
        private final int min, max, step;
        private final IntConsumer setter;
        private final IntFunction<Component> label;

        RangeSlider(int x, int y, int width, int initial, int min, int max, int step,
                    IntConsumer setter, IntFunction<Component> label) {
            super(x, y, width, 20, label.apply(initial), (double) (initial - min) / (max - min));
            this.min = min;
            this.max = max;
            this.step = step;
            this.setter = setter;
            this.label = label;
            updateMessage();
        }

        private int selected() {
            return Math.min(max, min + Math.round((max - min) * (float) value / step) * step);
        }

        @Override
        protected void updateMessage() {
            setMessage(label.apply(selected()));
        }

        @Override
        protected void applyValue() {
            setter.accept(selected());
            AutoLumaClient.previewSettings();
        }
    }
}

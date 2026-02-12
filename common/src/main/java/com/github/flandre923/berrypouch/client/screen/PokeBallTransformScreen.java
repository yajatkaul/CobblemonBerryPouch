package com.github.flandre923.berrypouch.client.screen;

import com.github.flandre923.berrypouch.client.DevEnvironment;
import com.github.flandre923.berrypouch.client.config.PokeBallGunTransformSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PokeBallTransformScreen extends Screen {
    private static final int FIELD_WIDTH = 80;
    private static final int FIELD_HEIGHT = 18;
    private final Screen lastScreen;
    private final List<FieldBinding> bindings = new ArrayList<>();

    public PokeBallTransformScreen(Screen lastScreen) {
        super(Component.translatable("screen.berrypouch.pokeball_transform.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        if (!DevEnvironment.IS_DEV) {
            Minecraft.getInstance().setScreen(lastScreen);
            return;
        }
        bindings.clear();
        int centerX = this.width / 2;
        int leftColumnX = centerX - 150;
        int rightColumnX = centerX + 10;
        int headerY = 28;
        int fieldsStartY = 46;

        addSectionLabel(leftColumnX, headerY,
                Component.translatable("screen.berrypouch.pokeball_transform.first_person"));
        addTransformFields(leftColumnX, fieldsStartY, PokeBallGunTransformSettings.getFirstPerson());

        addSectionLabel(rightColumnX, headerY,
                Component.translatable("screen.berrypouch.pokeball_transform.other"));
        addTransformFields(rightColumnX, fieldsStartY, PokeBallGunTransformSettings.getOther());

        int globalY = Math.min(fieldsStartY + 200, this.height - 70);
        addSectionLabel(centerX - 60, globalY,
                Component.translatable("screen.berrypouch.pokeball_transform.global"));
        addField(centerX - 40, globalY + 16,
                Component.translatable("screen.berrypouch.pokeball_transform.base_rotate_y"),
                PokeBallGunTransformSettings::getBaseRotationY,
                PokeBallGunTransformSettings::setBaseRotationY);

        int buttonY = this.height - 28;
        addRenderableWidget(Button.builder(Component.translatable("screen.berrypouch.pokeball_transform.reset"),
                button -> {
                    PokeBallGunTransformSettings.resetToDefaults();
                    PokeBallGunTransformSettings.save();
                    refreshBindings();
                }).bounds(centerX - 110, buttonY, 100, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"),
                button -> Minecraft.getInstance().setScreen(lastScreen))
                .bounds(centerX + 10, buttonY, 100, 20)
                .build());

        refreshBindings();
    }

    private void addSectionLabel(int x, int y, Component label) {
        int width = this.font.width(label);
        addRenderableWidget(new LabelWidget(x, y, width, label));
    }

    private void addTransformFields(int columnX, int startY, PokeBallGunTransformSettings.Transform transform) {
        int y = startY + 6;
        addField(columnX, y,
                Component.translatable("screen.berrypouch.pokeball_transform.translate_x"),
                () -> transform.translateX,
                value -> transform.translateX = value);
        y += 20;
        addField(columnX, y,
                Component.translatable("screen.berrypouch.pokeball_transform.translate_y"),
                () -> transform.translateY,
                value -> transform.translateY = value);
        y += 20;
        addField(columnX, y,
                Component.translatable("screen.berrypouch.pokeball_transform.translate_z"),
                () -> transform.translateZ,
                value -> transform.translateZ = value);
        y += 20;
        addField(columnX, y,
                Component.translatable("screen.berrypouch.pokeball_transform.rotate_x"),
                () -> transform.rotateX,
                value -> transform.rotateX = value);
        y += 20;
        addField(columnX, y,
                Component.translatable("screen.berrypouch.pokeball_transform.rotate_y"),
                () -> transform.rotateY,
                value -> transform.rotateY = value);
        y += 20;
        addField(columnX, y,
                Component.translatable("screen.berrypouch.pokeball_transform.rotate_z"),
                () -> transform.rotateZ,
                value -> transform.rotateZ = value);
        y += 20;
        addField(columnX, y,
                Component.translatable("screen.berrypouch.pokeball_transform.scale_x"),
                () -> transform.scaleX,
                value -> transform.scaleX = value);
        y += 20;
        addField(columnX, y,
                Component.translatable("screen.berrypouch.pokeball_transform.scale_y"),
                () -> transform.scaleY,
                value -> transform.scaleY = value);
        y += 20;
        addField(columnX, y,
                Component.translatable("screen.berrypouch.pokeball_transform.scale_z"),
                () -> transform.scaleZ,
                value -> transform.scaleZ = value);
    }

    private void addField(int x, int y, Component label, FloatGetter getter, FloatSetter setter) {
        int labelWidth = this.font.width(label);
        int labelX = x;
        int fieldX = x + labelWidth + 6;
        addRenderableWidget(new LabelWidget(labelX, y + 4, labelWidth, label));

        EditBox editBox = new EditBox(this.font, fieldX, y, FIELD_WIDTH, FIELD_HEIGHT, label);
        editBox.setMaxLength(20);
        editBox.setResponder(value -> {
            Float parsed = tryParse(value);
            if (parsed != null) {
                setter.set(parsed);
                PokeBallGunTransformSettings.save();
            }
        });
        addRenderableWidget(editBox);
        bindings.add(new FieldBinding(editBox, getter));
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui, mouseX, mouseY, partialTick);
        gui.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        super.render(gui, mouseX, mouseY, partialTick);
    }

    private void refreshBindings() {
        for (FieldBinding binding : bindings) {
            if (!binding.editBox.isFocused()) {
                binding.editBox.setValue(format(binding.getter.get()));
            }
        }
    }

    private static Float tryParse(String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String format(float value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    private record FieldBinding(EditBox editBox, FloatGetter getter) {
    }

    @FunctionalInterface
    private interface FloatGetter {
        float get();
    }

    @FunctionalInterface
    private interface FloatSetter {
        void set(float value);
    }

    private static final class LabelWidget extends net.minecraft.client.gui.components.AbstractWidget {
        private final Component label;

        private LabelWidget(int x, int y, int width, Component label) {
            super(x, y, width, 9, label);
            this.label = label;
        }

        @Override
        public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            gui.drawString(Minecraft.getInstance().font, label, getX(), getY(), 0xE0E0E0, false);
        }

        @Override
        protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narration) {
        }
    }
}

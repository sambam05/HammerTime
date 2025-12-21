package com.sheath.hammermining.client;

import com.sheath.hammermining.config.GeneralConfig;
import com.sheath.hammermining.init.ConfigInit;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class HammerConfigScreen extends Screen {
    private final Screen parent;
    private final GeneralConfig config;

    private TextFieldWidget durabilityField;
    private boolean splitXp;

    public HammerConfigScreen(Screen parent) {
        super(Text.literal("Hammer Mining Config"));
        this.parent = parent;
        this.config = ConfigInit.LOOT_CONFIG;
        this.splitXp = this.config.splitXpPerBlock;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 4;

        // Extra durability per extra block
        this.durabilityField = new TextFieldWidget(this.textRenderer, centerX - 100, y, 200, 20, Text.literal("Extra durability"));
        this.durabilityField.setTextPredicate(s -> s.matches("-?\\d*"));
        this.durabilityField.setText(String.valueOf(config.extraDurabilityPerExtraBlock));
        this.addDrawableChild(this.durabilityField);

        // XP split toggle
        y += 30;
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder()
                .initially(this.splitXp)
                .build(centerX - 100, y, 200, 20, Text.literal("XP per block"), (button, value) -> this.splitXp = value));

        // Done / Cancel buttons
        y += 40;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> {
            saveConfig();
            this.close();
        }).dimensions(centerX - 100, y, 95, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button -> this.close())
                .dimensions(centerX + 5, y, 95, 20).build());

        super.init();
    }

    private void saveConfig() {
        int durability = config.extraDurabilityPerExtraBlock;
        try {
            String text = durabilityField.getText().trim();
            if (!text.isEmpty()) {
                durability = Integer.parseInt(text);
            }
        } catch (NumberFormatException ignored) {
        }

        config.extraDurabilityPerExtraBlock = durability;
        config.splitXpPerBlock = this.splitXp;
        config.save();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}

package dev.fixpot47.entityesp;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class EntityEspScreen extends Screen {
    private Button playerButton;
    private Button mobButton;
    private Button chestButton;

    public EntityEspScreen() {
        super(Component.literal("EntityESP"));
    }

    @Override
    protected void init() {
        int width = 180;
        int x = (this.width - width) / 2;
        int y = this.height / 2 - 38;

        playerButton = this.addRenderableWidget(Button.builder(playerLabel(), button -> {
            EntityEspConfig.togglePlayerEsp();
            button.setMessage(playerLabel());
        }).bounds(x, y, width, 20).build());

        mobButton = this.addRenderableWidget(Button.builder(mobLabel(), button -> {
            EntityEspConfig.toggleMobEsp();
            button.setMessage(mobLabel());
        }).bounds(x, y + 26, width, 20).build());

        chestButton = this.addRenderableWidget(Button.builder(chestLabel(), button -> {
            EntityEspConfig.toggleChestEsp();
            button.setMessage(chestLabel());
        }).bounds(x, y + 52, width, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.onClose())
                .bounds(x, y + 86, width, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, this.height / 2 - 72, 0xFFFFFFFF);
        graphics.centeredText(this.font, Component.literal("F8"), this.width / 2,
                this.height / 2 + 78, 0xFFAAAAAA);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static Component playerLabel() {
        return toggleLabel("PlayerESP", EntityEspConfig.playerEsp());
    }

    private static Component mobLabel() {
        return toggleLabel("MobESP", EntityEspConfig.mobEsp());
    }

    private static Component chestLabel() {
        return toggleLabel("ChestESP", EntityEspConfig.chestEsp());
    }

    private static Component toggleLabel(String name, boolean enabled) {
        return Component.literal(name + ": ")
                .append(Component.literal(enabled ? "ON" : "OFF")
                        .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
    }
}

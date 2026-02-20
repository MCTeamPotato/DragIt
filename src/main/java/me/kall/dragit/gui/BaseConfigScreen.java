package me.kall.dragit.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public abstract class BaseConfigScreen extends Screen {

    protected final String filePath;
    protected boolean syncToServer = true;

    protected Component statusMessage = Component.empty();
    protected int statusColor = 0xFFFFFF;

    protected BaseConfigScreen(Component title, String filePath) {
        super(title);
        this.filePath = filePath;
    }

    protected abstract void initExtraWidgets(int centerX, int startY);

    protected abstract int getStartY();
    protected abstract int getConfirmButtonYOffset();

    protected abstract void onDone();

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int startY = getStartY();

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.dragit.sync." + (syncToServer ? "on" : "off")),
                button -> {
                    this.syncToServer = !this.syncToServer;
                    button.setMessage(Component.translatable("gui.dragit.sync." + (syncToServer ? "on" : "off")));
                }).bounds(centerX - 100, startY, 200, 20).build());

        initExtraWidgets(centerX, startY);

        int confirmY = startY + getConfirmButtonYOffset();
        this.addRenderableWidget(Button.builder(Component.translatable("gui.yes"), button -> this.onDone()).bounds(centerX - 105, confirmY, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.no"), button -> this.onClose()).bounds(centerX + 5, confirmY, 100, 20).build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.dragit.file", this.filePath), this.width / 2, 40, 0xAAAAAA);
        renderExtra(guiGraphics, mouseX, mouseY, partialTicks);
        if (!this.statusMessage.getString().isEmpty()) guiGraphics.drawCenteredString(this.font, this.statusMessage, this.width / 2, getStatusMessageY(), this.statusColor);
    }

    protected void renderExtra(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {}

    protected int getStatusMessageY() {
        return this.height / 2 + 20;
    }

    protected void setStatus(Component message, int color) {
        this.statusMessage = message;
        this.statusColor = color;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
package me.kall.dragit.gui;

import me.kall.dragit.DragIt;
import me.kall.dragit.data.skin.ClientSkins;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.skin.SkinSavePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class SkinConfigScreen extends Screen {
    private final String filePath;

    private boolean syncToServer = true;

    private Component statusMessage = Component.empty();
    private int statusColor = 0xFFFFFF;

    public SkinConfigScreen(String filePath) {
        super(Component.translatable("gui.dragit.skin_config.title"));
        this.filePath = filePath;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int startY = this.height / 2 - 40;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.dragit.sync." + (this.syncToServer ? "on" : "off")),
                        button -> {
                            this.syncToServer = !this.syncToServer;
                            button.setMessage(Component.translatable("gui.dragit.sync." + (this.syncToServer ? "on" : "off")));
                        }).bounds(centerX - 100, startY, 200, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.yes"), button -> this.onDone()).bounds(centerX - 102, startY + 30, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.no"), button -> this.onClose()).bounds(centerX + 2, startY + 30, 100, 20).build());
    }

    private void onDone() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return;
        UUID uuid = player.getUUID();
        ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "skin_" + System.currentTimeMillis());

        try {
            byte[] textureBytes = Files.readAllBytes(new File(this.filePath).toPath());

            ClientSkins.registerSkin(uuid, textureLocation, textureBytes);

            if (this.syncToServer) {
                DragNetworker.INSTANCE.sendToServer(new SkinSavePacket(uuid, textureLocation, textureBytes));
                DragIt.LOGGER.info("Delivering skin {} to server. Texture Location: {}. Size: {} bytes.", this.filePath, textureLocation, textureBytes.length);
            } else {
                DragIt.LOGGER.info("Skipping server sync for image {}. Texture Location: {}. Size: {} bytes.", this.filePath, textureLocation, textureBytes.length);
            }

            this.onClose();
        } catch (IOException e) {
            DragIt.LOGGER.error("Failed to load skin: {}", this.filePath, e);
            this.statusMessage = Component.translatable("gui.dragit.error", e.getMessage());
            this.statusColor = 0xFF0000;
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.dragit.file", new File(this.filePath).getName()), this.width / 2, this.height / 2 - 70, 0xAAAAAA);
        if (!this.statusMessage.getString().isEmpty()) guiGraphics.drawCenteredString(this.font, this.statusMessage, this.width / 2, this.height / 2 + 20, this.statusColor);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
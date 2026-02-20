package me.kall.dragit.gui;

import me.kall.dragit.DragIt;
import me.kall.dragit.data.skin.ClientSkins;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.skin.SkinSavePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class SkinConfigScreen extends BaseConfigScreen {
    public SkinConfigScreen(String filePath) {
        super(Component.translatable("gui.dragit.skin_config.title"), filePath);
    }

    @Override
    protected int getStartY() {
        return this.height / 2 - 40;
    }

    @Override
    protected int getConfirmButtonYOffset() {
        return 30;
    }

    @Override
    protected void initExtraWidgets(int centerX, int startY) {}

    @Override
    protected void onDone() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return;
        UUID uuid = player.getUUID();
        ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "skin_" + System.currentTimeMillis());

        try {
            byte[] textureBytes = Files.readAllBytes(new File(this.filePath).toPath());

            ClientSkins.registerSkin(uuid, textureLocation, textureBytes);

            if (this.checkSync()) this.syncToServer = false;

            if (this.syncToServer) {
                DragNetworker.INSTANCE.sendToServer(new SkinSavePacket(uuid, textureLocation, textureBytes));
                DragIt.LOGGER.info("Delivering skin {} to server. Texture Location: {}. Size: {} bytes.", this.filePath, textureLocation, textureBytes.length);
            } else {
                DragIt.LOGGER.info("Skipping server sync for image {}. Texture Location: {}. Size: {} bytes.", this.filePath, textureLocation, textureBytes.length);
            }

            this.onClose();
        } catch (IOException e) {
            DragIt.LOGGER.error("Failed to load skin: {}", this.filePath, e);
            this.setStatus(Component.translatable("gui.dragit.error", e.getMessage()), 0xFF0000);
        }
    }
}
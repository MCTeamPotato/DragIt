package me.kall.dragit.gui;

import me.kall.dragit.DragIt;
import me.kall.dragit.config.DragCommonConfig;
import me.kall.dragit.data.painting.ClientPaintings;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.painting.PaintingSavePacket;
import me.kall.dragit.util.ImageCompressor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class PaintingConfigScreen extends BaseConfigScreen {
    private final ResourceLocation dimension;
    private final long pos;

    private EditBox maxPixelsBox;
    private int maxPixels = 32 * 32;

    public PaintingConfigScreen(String filePath, ResourceLocation dimension, long pos) {
        super(Component.translatable("gui.dragit.painting_config.title"), filePath);
        this.dimension = dimension;
        this.pos = pos;
    }

    @Override
    protected int getStartY() {
        return this.height / 2 - 80;
    }

    @Override
    protected int getConfirmButtonYOffset() {
        return 135;
    }

    @Override
    protected int getStatusMessageY() {
        return this.height / 2 + 80;
    }

    @Override
    protected void initExtraWidgets(int centerX, int startY) {
        this.maxPixelsBox = new EditBox(this.font, centerX - 100, startY + 85, 200, 20, Component.translatable("gui.dragit.painting_config.max_pixels"));
        this.maxPixelsBox.setValue(String.valueOf(this.maxPixels));
        this.maxPixelsBox.setMaxLength(10);
        this.maxPixelsBox.setResponder(text -> {
            try {
                int value = Integer.parseInt(text);
                if (value > 0 && value <= DragCommonConfig.INSTANCE.getMaxPixels()) {
                    this.maxPixels = value;
                    double sizeKB = (value * 4.0) / 1024.0;
                    this.setStatus(Component.translatable("gui.dragit.painting_config.valid", value, String.format("%.2f", sizeKB)), 0x00FF00);
                } else {
                    this.setStatus(Component.translatable("gui.dragit.painting_config.invalid_range", String.valueOf(DragCommonConfig.INSTANCE.getMaxPixels())), 0xFF0000);
                }
            } catch (NumberFormatException e) {
                if (!text.isEmpty()) {
                    this.setStatus(Component.translatable("gui.dragit.painting_config.invalid_format"), 0xFF0000);
                } else {
                    this.statusMessage = Component.empty();
                }
            }
        });
        this.addRenderableWidget(this.maxPixelsBox);
        this.setInitialFocus(this.maxPixelsBox);

        int presetY = startY + 110;
        int[] presetValues = {8192, 4096, 2048, 512};
        int buttonWidth = 47, spacing = 3;
        for (int i = 0; i < presetValues.length; i++) {
            final int value = presetValues[i];
            this.addRenderableWidget(Button.builder(Component.literal(String.valueOf(value)), button -> this.maxPixelsBox.setValue(String.valueOf(value))).bounds(centerX - 100 + i * (buttonWidth + spacing), presetY, buttonWidth, 20).build());
        }
    }

    @Override
    protected void renderExtra(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.dragit.painting_config.max_pixels"), this.width / 2, this.height / 2 - 60 + 50, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.dragit.painting_config.tip"), this.width / 2, this.height - 30, 0x888888);
    }

    @Override
    protected void onDone() {
        try {
            byte[] textureBytes = ImageCompressor.compress(this.filePath, this.maxPixels);
            ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "painting_" + System.currentTimeMillis());

            if (this.checkSync()) this.syncToServer = false;

            if (this.syncToServer) {
                DragNetworker.INSTANCE.sendToServer(new PaintingSavePacket(this.dimension, this.pos, textureLocation, textureBytes));
                DragIt.LOGGER.info("Delivering image {} to server. Texture Location: {}. Size: {} bytes.", this.filePath, textureLocation, textureBytes.length + 16);
            } else {
                DragIt.LOGGER.info("Skipping server sync for image {}. Texture Location: {}. Size: {} bytes.", this.filePath, textureLocation, textureBytes.length + 16);
            }

            ClientPaintings.registerPainting(this.dimension, this.pos, textureBytes, textureLocation);
            this.onClose();
        } catch (IOException e) {
            DragIt.LOGGER.error("Failed to load image: {}", this.filePath, e);
            this.setStatus(Component.translatable("gui.dragit.error", e.getMessage()), 0xFF0000);
        }
    }
}
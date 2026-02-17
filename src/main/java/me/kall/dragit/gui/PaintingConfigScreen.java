package me.kall.dragit.gui;

import me.kall.dragit.DragIt;
import me.kall.dragit.data.painting.ClientPaintings;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.painting.PaintingSavePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class PaintingConfigScreen extends BaseConfigScreen {
    private static final int MAX_PIXELS_DEFAULT = 32 * 32;

    private final ResourceLocation dimension;
    private final long pos;

    private EditBox maxPixelsBox;
    private boolean enableCompression = true;
    private int maxPixels = MAX_PIXELS_DEFAULT;

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
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.dragit.painting_config.compression." + (this.enableCompression ? "on" : "off")),
                button -> {
                    this.enableCompression = !this.enableCompression;
                    button.setMessage(Component.translatable("gui.dragit.painting_config.compression." + (this.enableCompression ? "on" : "off")));
                    this.maxPixelsBox.setEditable(this.enableCompression);
                }).bounds(centerX - 100, startY + 30, 200, 20).build());

        this.maxPixelsBox = new EditBox(this.font, centerX - 100, startY + 85, 200, 20, Component.translatable("gui.dragit.painting_config.max_pixels"));
        this.maxPixelsBox.setValue(String.valueOf(MAX_PIXELS_DEFAULT));
        this.maxPixelsBox.setMaxLength(10);
        this.maxPixelsBox.setResponder(text -> {
            try {
                int value = Integer.parseInt(text);
                if (value > 0 && value <= 100000000) {
                    this.maxPixels = value;
                    double sizeKB = (value * 4.0) / 1024.0;
                    this.setStatus(Component.translatable("gui.dragit.painting_config.valid", value, String.format("%.2f", sizeKB)), 0x00FF00);
                } else {
                    this.setStatus(Component.translatable("gui.dragit.painting_config.invalid_range"), 0xFF0000);
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
            byte[] textureBytes = processImage(this.filePath, this.enableCompression, this.maxPixels);
            ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "painting_" + System.currentTimeMillis());

            if (DragIt.OP_REQUIRED.get() && this.minecraft != null && this.minecraft.player != null
                    && !this.minecraft.player.hasPermissions(Commands.LEVEL_GAMEMASTERS)) {
                this.syncToServer = false;
            }

            if (this.syncToServer) {
                DragNetworker.INSTANCE.sendToServer(new PaintingSavePacket(dimension, pos, textureLocation, textureBytes));
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

    private static byte @NotNull [] processImage(String filePath, boolean doCompression, int maxPixels) throws IOException {
        BufferedImage originalImage = ImageIO.read(new File(filePath));
        if (originalImage == null) throw new IOException("Unable to read image file: " + filePath);

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int totalPixels = width * height;
        BufferedImage processedImage = originalImage;

        if (doCompression && totalPixels > maxPixels) {
            double scale = Math.sqrt((double) maxPixels / totalPixels);
            int newWidth = (int) (width * scale);
            int newHeight = (int) (height * scale);
            DragIt.LOGGER.info("Compressing image from {}x{} ({} pixels) to {}x{} ({} pixels)", width, height, totalPixels, newWidth, newHeight, newWidth * newHeight);

            processedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = processedImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            graphics.dispose();
        } else if (!doCompression && totalPixels > maxPixels) {
            DragIt.LOGGER.info("Skipping compression. Image size: {}x{} ({} pixels)", width, height, totalPixels);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(processedImage, "PNG", outputStream);
        return outputStream.toByteArray();
    }
}
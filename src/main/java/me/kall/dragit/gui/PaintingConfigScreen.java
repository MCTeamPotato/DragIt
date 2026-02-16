package me.kall.dragit.gui;

import me.kall.dragit.DragIt;
import me.kall.dragit.data.painting.ClientPaintings;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.painting.PaintingSavePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class PaintingConfigScreen extends Screen {
    private static final int MAX_PIXELS_DEFAULT = 32 * 32;

    private final String filePath;
    private final ResourceLocation dimension;
    private final long pos;

    private EditBox maxPixelsBox;

    private boolean enableCompression = true;
    private int maxPixels = MAX_PIXELS_DEFAULT;

    private Component statusMessage = Component.empty();
    private int statusColor = 0xFFFFFF;

    public PaintingConfigScreen(String filePath, ResourceLocation dimension, long pos) {
        super(Component.translatable("gui.dragit.painting_config.title"));
        this.filePath = filePath;
        this.dimension = dimension;
        this.pos = pos;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = this.height / 2 - 60;

        Button enableCompressionButton = Button.builder(
                Component.translatable("gui.dragit.painting_config.compression." + (this.enableCompression ? "on" : "off")),
                button -> {
                    enableCompression = !enableCompression;
                    button.setMessage(Component.translatable("gui.dragit.painting_config.compression." + (this.enableCompression ? "on" : "off")));
                    maxPixelsBox.setEditable(enableCompression);
                }).bounds(centerX - 100, startY, 200, 20).build();
        this.addRenderableWidget(enableCompressionButton);

        this.maxPixelsBox = new EditBox(this.font, centerX - 100, startY + 60, 200, 20, Component.translatable("gui.dragit.painting_config.max_pixels"));
        this.maxPixelsBox.setValue(String.valueOf(MAX_PIXELS_DEFAULT));
        this.maxPixelsBox.setMaxLength(10);
        this.maxPixelsBox.setResponder(text -> {
            try {
                int value = Integer.parseInt(text);
                if (value > 0 && value <= 100000000) {
                    this.maxPixels = value;
                    double sizeKB = (value * 4.0) / 1024.0;
                    this.statusMessage = Component.translatable("gui.dragit.painting_config.valid", value, String.format("%.2f", sizeKB));
                    this.statusColor = 0x00FF00;
                } else {
                    this.statusMessage = Component.translatable("gui.dragit.painting_config.invalid_range");
                    this.statusColor = 0xFF0000;
                }
            } catch (NumberFormatException e) {
                if (!text.isEmpty()) {
                    this.statusMessage = Component.translatable("gui.dragit.painting_config.invalid_format");
                    this.statusColor = 0xFF0000;
                } else {
                    this.statusMessage = Component.empty();
                }
            }
        });
        this.addRenderableWidget(this.maxPixelsBox);
        this.setInitialFocus(this.maxPixelsBox);

        int presetY = startY + 85;
        int presetStartX = centerX - 100;
        int buttonWidth = 47;
        int spacing = 3;
        int[] presetValues = {8192, 4096, 2048, 512};

        for (int i = 0; i < presetValues.length; i++) {
            final int value = presetValues[i];
            this.addRenderableWidget(Button.builder(Component.literal(String.valueOf(value)), button -> this.maxPixelsBox.setValue(String.valueOf(value))).bounds(presetStartX + i * (buttonWidth + spacing), presetY, buttonWidth, 20).build());
        }

        this.addRenderableWidget(Button.builder(Component.translatable("gui.dragit.painting_config.confirm"), button -> this.processAndUpload()).bounds(centerX - 105, startY + 110, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.dragit.painting_config.cancel"), button -> this.onClose()).bounds(centerX + 5, startY + 110, 100, 20).build());
    }

    @Override
    public void render(@NotNull GuiGraphics poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        poseStack.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        poseStack.drawCenteredString(this.font, Component.translatable("gui.dragit.painting_config.file", new File(this.filePath).getName()), this.width / 2, 40, 0xAAAAAA);
        poseStack.drawCenteredString(this.font, Component.translatable("gui.dragit.painting_config.max_pixels"), this.width / 2, this.height / 2 - 60 + 45, 0xFFFFFF);
        if (!this.statusMessage.getString().isEmpty()) poseStack.drawCenteredString(this.font, this.statusMessage, this.width / 2, this.height / 2 + 80, this.statusColor);
        poseStack.drawCenteredString(this.font, Component.translatable("gui.dragit.painting_config.tip"), this.width / 2, this.height - 30, 0x888888);
    }

    private void processAndUpload() {
        try {
            byte[] textureBytes = processImage(this.filePath, this.enableCompression, this.maxPixels);
            ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "painting_" + System.currentTimeMillis());
            DragNetworker.INSTANCE.sendToServer(new PaintingSavePacket(dimension, pos, textureLocation, textureBytes));
            DragIt.LOGGER.info("Delivering image {} to server. Size: {} bytes.", textureLocation.toString(), textureBytes.length + 16);
            ClientPaintings.registerPainting(this.dimension, this.pos, textureBytes, textureLocation);
            this.onClose();
        } catch (IOException exception) {
            DragIt.LOGGER.error("Failed to load image: {}", this.filePath);
            DragIt.LOGGER.error(exception.getMessage(), exception);
            this.statusMessage = Component.translatable("gui.dragit.painting_config.error", exception.getMessage());
            this.statusColor = 0xFF0000;
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
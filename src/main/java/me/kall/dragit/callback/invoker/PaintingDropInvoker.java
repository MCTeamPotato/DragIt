package me.kall.dragit.callback.invoker;

import me.kall.dragit.DragIt;
import me.kall.dragit.data.painting.ClientPaintings;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.painting.PaintingSavePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFWDropCallback;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class PaintingDropInvoker implements Invoker {
    private static final int MAX_PIXELS = 32 * 32;

    public static final PaintingDropInvoker INSTANCE = new PaintingDropInvoker();

    @Override
    public boolean invoke(int count, long names) {
        String filePath = GLFWDropCallback.getName(names, 0);
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        HitResult target = minecraft.hitResult;

        if (player != null && level != null && target != null && target.getType().equals(HitResult.Type.ENTITY) && ((EntityHitResult) target).getEntity() instanceof Painting painting) {
            try {
                byte[] textureBytes = processImage(filePath, !player.isShiftKeyDown());
                ResourceLocation dimension = level.dimension().location();
                long pos = painting.blockPosition().asLong();
                ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "painting_" + Math.abs(filePath.hashCode()));

                DragNetworker.INSTANCE.sendToServer(new PaintingSavePacket(dimension, pos, textureLocation, textureBytes));
                DragIt.LOGGER.info("Delivering image {} to server. Size: {} bytes.", textureLocation.toString(), textureBytes.length + 16);
                ClientPaintings.registerPainting(dimension, pos, textureBytes, textureLocation);
                return true;
            } catch (IOException exception) {
                DragIt.LOGGER.error("Failed to load image: {}", filePath);
                DragIt.LOGGER.error(exception.getMessage(), exception);
                return false;
            }
        }
        return false;
    }

    private static byte @NotNull [] processImage(String filePath, boolean doCompression) throws IOException {
        BufferedImage originalImage = ImageIO.read(new File(filePath));
        if (originalImage == null) {
            throw new IOException("Unable to read image file: " + filePath);
        }

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int totalPixels = width * height;

        BufferedImage processedImage = originalImage;

        if (doCompression && totalPixels > MAX_PIXELS) {
            double scale = Math.sqrt((double) MAX_PIXELS / totalPixels);
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
        } else if (!doCompression && totalPixels > MAX_PIXELS) {
            DragIt.LOGGER.info("Skipping compression (Shift held). Image size: {}x{} ({} pixels)", width, height, totalPixels);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(processedImage, "PNG", outputStream);
        return outputStream.toByteArray();
    }
}
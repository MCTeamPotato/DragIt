package me.kall.dragit.util;

import me.kall.dragit.DragIt;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ImageCompressor {
    public static byte @NotNull [] compress(String filePath, int maxPixels) throws IOException {
        BufferedImage original = ImageIO.read(new File(filePath));
        if (original == null) throw new IOException("Unable to read image file: " + filePath);
        return compress(original, maxPixels, filePath);
    }

    private static byte @NotNull [] compress(@NotNull BufferedImage original, int maxPixels, String source) throws IOException {
        int width = original.getWidth();
        int height = original.getHeight();
        int totalPixels = width * height;

        if (totalPixels <= maxPixels) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(original, "PNG", out);
            return out.toByteArray();
        }

        double scale = Math.sqrt((double) maxPixels / totalPixels);
        int newWidth  = Math.max(1, (int) (width  * scale));
        int newHeight = Math.max(1, (int) (height * scale));
        DragIt.LOGGER.info("Compressing image [{}] from {}x{} ({} px) to {}x{} ({} px)", source, width, height, totalPixels, newWidth, newHeight, newWidth * newHeight);

        BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(original, 0, 0, newWidth, newHeight, null);
        graphics.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(scaled, "PNG", outputStream);
        return outputStream.toByteArray();
    }
}
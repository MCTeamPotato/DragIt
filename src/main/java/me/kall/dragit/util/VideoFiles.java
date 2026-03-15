package me.kall.dragit.util;

import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class VideoFiles {
    private static final String[] VIDEO_EXTENSIONS = {".mp4", ".gif", ".mkv", ".webm", ".avi", ".mov", ".flv", ".m4v"};

    public static boolean isVideoFile(@NotNull String path) {
        String lower = path.toLowerCase();
        for (String videoExtension : VIDEO_EXTENSIONS) {
            if (lower.endsWith(videoExtension)) return true;
        }
        return false;
    }

    public static @NotNull String copyFileToConfig(String filePath) {
        Path source = Path.of(filePath);
        String hash = String.valueOf(filePath.hashCode());
        Path targetDir = FMLLoader.getGamePath().resolve("config").resolve("narutoloading-sources").resolve(hash);

        String fileName = source.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String ext = dot == -1 ? "" : fileName.substring(dot);

        Path target = targetDir.resolve("video" + ext);

        try {
            Files.createDirectories(targetDir);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString();
        } catch (Exception exception) {
            return target.toString();
        }
    }
}

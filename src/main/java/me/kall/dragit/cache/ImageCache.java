package me.kall.dragit.cache;

import me.kall.dragit.DragIt;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageCache {
    private static Path cacheFolder;

    public static void init() {
        cacheFolder = FMLLoader.getGamePath().resolve("imageCache");
        try {
            Files.createDirectories(cacheFolder);
            DragIt.LOGGER.info("[DragIt] ImageCache initialized: {}", cacheFolder);
        } catch (IOException e) {
            DragIt.LOGGER.error("[DragIt] Failed to create imageCache directory", e);
        }
    }

    public static int hash(byte[] bytes) {
        return Arrays.hashCode(bytes);
    }

    public static void save(byte[] bytes) {
        if (cacheFolder == null) return;
        int hash = hash(bytes);
        List<byte[]> existing = find(hash);
        for (byte[] e : existing) {
            if (Arrays.equals(e, bytes)) return;
        }
        Path path = buildPath(hash, existing.size());
        try {
            Files.write(path, bytes);
        } catch (IOException e) {
            DragIt.LOGGER.error("[DragIt] Failed to write imageCache file: {}", path, e);
        }
    }

    public static @NotNull List<byte[]> find(int hash) {
        List<byte[]> result = new ArrayList<>();
        if (cacheFolder == null) return result;
        tryRead(result, cacheFolder.resolve(hash + ".dat"));
        for (int i = 1; ; i++) {
            Path p = cacheFolder.resolve(hash + "_" + i + ".dat");
            if (!Files.exists(p)) break;
            tryRead(result, p);
        }
        return result;
    }

    private static void tryRead(List<byte[]> list, Path path) {
        if (!Files.exists(path)) return;
        try {
            list.add(Files.readAllBytes(path));
        } catch (IOException e) {
            DragIt.LOGGER.error("[DragIt] Failed to read cache file: {}", path, e);
        }
    }

    private static @NotNull Path buildPath(int hash, int index) {
        return index == 0 ? cacheFolder.resolve(hash + ".dat") : cacheFolder.resolve(hash + "_" + index + ".dat");
    }
}
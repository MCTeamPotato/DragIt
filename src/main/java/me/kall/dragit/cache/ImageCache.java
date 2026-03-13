package me.kall.dragit.cache;

import me.kall.dragit.DragIt;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.cache.CacheRequestPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class ImageCache {
    private static final Path CACHE = FMLLoader.getGamePath().resolve("imageCache");

    public static @NotNull ResourceLocation getOrCreate(byte @NotNull [] bytes) throws IOException {
        String hash = String.format("%08x", Arrays.hashCode(bytes));
        Files.createDirectories(CACHE);

        String base = "image_" + hash;
        for (int i = 0; ; i++) {
            String path = (i == 0) ? base : base + "_" + i;
            Path file = CACHE.resolve(path);
            if (!Files.exists(file)) {
                Files.write(file, bytes);
                DragIt.LOGGER.info("ImageCache: stored new entry [{}]", path);
                return ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, path);
            }
            byte[] existing = Files.readAllBytes(file);
            if (Arrays.equals(existing, bytes)) return ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, path);
        }
    }

    public static byte @Nullable [] load(@NotNull ResourceLocation location) {
        Path file = CACHE.resolve(location.getPath());
        if (!Files.exists(file)) return null;
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            DragIt.LOGGER.error("ImageCache: failed to read [{}]", location.getPath(), e);
            return null;
        }
    }

    public static void store(@NotNull ResourceLocation location, byte @NotNull [] bytes) {
        try {
            Files.createDirectories(CACHE);
            Path file = CACHE.resolve(location.getPath());
            if (!Files.exists(file)) Files.write(file, bytes);
        } catch (IOException e) {
            DragIt.LOGGER.error("ImageCache: failed to store [{}]", location.getPath(), e);
        }
    }

    public static byte @Nullable [] resolveBytes(ResourceLocation location, byte @Nullable [] bytes, PendingRegistrations.Registration onResolved) {
        if (bytes != null) {
            store(location, bytes);
            return bytes;
        }
        byte[] cached = load(location);
        if (cached != null) return cached;
        PendingRegistrations.add(location, onResolved);
        DragNetworker.sendToServer(new CacheRequestPacket(location));
        DragIt.LOGGER.info("Cache miss for [{}], requesting from server.", location);
        return null;
    }
}
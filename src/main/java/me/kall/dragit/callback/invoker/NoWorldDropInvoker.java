package me.kall.dragit.callback.invoker;

import me.kall.dragit.DragIt;
import me.kall.dragit.callback.DragCallback;
import me.kall.narutoloading.NarutoLoading;
import me.kall.narutoloading.common.env.BaseEnv;
import me.kall.narutoloading.common.env.config.NarutoConfig;
import me.kall.narutoloading.noworld.core.NarutoRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFWDropCallback;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class NoWorldDropInvoker implements DragCallback.Invoker {
    @Override
    public boolean invoke(long window, int count, long names) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null || minecraft.level != null) return false;
        String filePath = GLFWDropCallback.getName(names, 0);
        try {
            String newFilePath = NarutoConfig.relative(copyFileToConfig(filePath));
            BaseEnv.narutoConfig.config.put("videoFileName", newFilePath).put("audioFileName", NarutoLoading.BLANK).saveToFile();
            BaseEnv.setupEnv(false);
            NarutoRenderer.INSTANCE.shutdown();
            NarutoRenderer.INSTANCE.setup();
        } catch (Exception exception) {
            DragIt.LOGGER.error("Error copying file to config directory.", exception);
        }
        return false;
    }

    public static @NotNull String copyFileToConfig(String filePath) throws IOException {
        Path source = Paths.get(filePath);

        String hash = String.valueOf(filePath.hashCode());

        Path targetDir = FMLLoader.getGamePath().resolve("config").resolve("narutoloading-sources").resolve(hash);

        Files.createDirectories(targetDir);

        String fileName = source.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String ext = dot == -1 ? "" : fileName.substring(dot);

        Path target = targetDir.resolve("video" + ext);

        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

        return target.toString();
    }
}

package me.kall.dragit.callback.invoker;

import me.kall.dragit.callback.DragCallback;
import me.kall.dragit.util.VideoFiles;
import me.kall.narutoloading.NarutoLoading;
import me.kall.narutoloading.common.env.BaseEnv;
import me.kall.narutoloading.common.env.config.NarutoConfig;
import me.kall.narutoloading.noworld.core.NarutoRenderer;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFWDropCallback;

public class NoWorldDropInvoker implements DragCallback.Invoker {
    @Override
    public boolean invoke(long window, int count, long names) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null || minecraft.level != null) return false;
        String filePath = GLFWDropCallback.getName(names, 0);
        String newFilePath = NarutoConfig.relative(VideoFiles.copyFileToConfig(filePath));
        BaseEnv.narutoConfig.config.put("videoFileName", newFilePath).put("audioFileName", NarutoLoading.BLANK).saveToFile();
        BaseEnv.setupEnv(false);
        NarutoRenderer.INSTANCE.shutdown();
        NarutoRenderer.INSTANCE.setup();
        return false;
    }
}

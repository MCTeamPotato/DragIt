package me.kall.dragit.callback;

import me.kall.dragit.DragIt;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = DragIt.MOD_ID, value = Dist.CLIENT)
public class DragCallback extends GLFWDropCallback {
    private static GLFWDropCallback lastCallback;

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        lastCallback = GLFW.glfwSetDropCallback(Minecraft.getInstance().getWindow().getWindow(), new DragCallback());
    }

    @Override
    public void invoke(long window, int count, long names) {
        PaintingDropCallback.invoke(count, names);
        SkinDropCallback.invoke(count, names);
        if (lastCallback != null) lastCallback.invoke(window, count, names);
    }
}

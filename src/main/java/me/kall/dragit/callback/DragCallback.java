package me.kall.dragit.callback;

import me.kall.dragit.DragIt;
import me.kall.dragit.callback.invoker.ChatDropInvoker;
import me.kall.dragit.callback.invoker.ItemFrameDropInvoker;
import me.kall.dragit.callback.invoker.PaintingDropInvoker;
import me.kall.dragit.callback.invoker.SkinDropInvoker;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = DragIt.MOD_ID, value = Dist.CLIENT)
public class DragCallback extends GLFWDropCallback {

    private static @Nullable GLFWDropCallback lastCallback;

    private final Invoker[] invokers = new Invoker[]{new SkinDropInvoker(), new PaintingDropInvoker(), new ItemFrameDropInvoker(), new ChatDropInvoker()};

    @Override
    public void invoke(long window, int count, long names) {
        for (Invoker invoker : this.invokers) {
            if (invoker.invoke(window, count, names)) break;
        }

        if (lastCallback == null) return;
        lastCallback.invoke(window, count, names);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        lastCallback = GLFW.glfwSetDropCallback(Minecraft.getInstance().getWindow().getWindow(), new DragCallback());
    }

    public interface Invoker {
        boolean invoke(long window, int count, long names);
    }
}

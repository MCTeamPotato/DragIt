package me.kall.dragit.callback.invoker;

import me.kall.dragit.callback.DragCallback;
import me.kall.dragit.gui.SkinConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFWDropCallback;

public class SkinDropInvoker implements DragCallback.Invoker {
    @Override
    public boolean invoke(long names) {
        String filePath = GLFWDropCallback.getName(names, 0);
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (minecraft.screen instanceof EffectRenderingInventoryScreen && player != null) {
            Minecraft.getInstance().setScreen(new SkinConfigScreen(filePath));
            return true;
        }
        return false;
    }
}

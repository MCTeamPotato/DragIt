package me.kall.dragit.callback.invoker;

import me.kall.dragit.callback.DragCallback;
import me.kall.dragit.ext.ImageContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFWDropCallback;

public class ChatImageDropInvoker implements DragCallback.Invoker {
    @Override
    public boolean invoke(long names) {
        String filePath = GLFWDropCallback.getName(names, 0);
        if (Minecraft.getInstance().screen instanceof ChatScreen chatScreen) {
            ((ImageContainer)chatScreen).dropIt$addImage(filePath);
            return true;
        }
        return false;
    }
}

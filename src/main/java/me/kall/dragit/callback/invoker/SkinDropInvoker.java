package me.kall.dragit.callback.invoker;

import me.kall.dragit.DragIt;
import me.kall.dragit.DragItClient;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.callback.DragCallback;
import me.kall.dragit.data.skin.ClientSkins;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.skin.SkinSavePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFWDropCallback;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class SkinDropInvoker implements DragCallback.Invoker {
    @Override
    public boolean invoke(long window, int count, long names) {
        String filePath = GLFWDropCallback.getName(names, 0);
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (!(minecraft.screen instanceof EffectRenderingInventoryScreen) || player == null) return false;

        UUID uuid = player.getUUID();
        try {
            byte[] textureBytes = Files.readAllBytes(new File(filePath).toPath());
            if (textureBytes.length > 10000) {
                player.displayClientMessage(Component.translatable("note.dragit.skin"), false);
                return false;
            }
            ResourceLocation textureLocation = ImageCache.getOrCreate(textureBytes);
            if (DragItClient.canSync()) DragNetworker.sendToServer(new SkinSavePacket(uuid, textureLocation, textureBytes));
            ClientSkins.registerSkin(uuid, textureLocation, textureBytes);
            return true;
        } catch (IOException exception) {
            DragIt.LOGGER.error("Error registering skin", exception);
        }
        return false;
    }
}

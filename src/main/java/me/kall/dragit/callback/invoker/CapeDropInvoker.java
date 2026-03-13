package me.kall.dragit.callback.invoker;

import me.kall.dragit.DragIt;
import me.kall.dragit.DragItClient;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.callback.DragCallback;
import me.kall.dragit.config.DragClientConfig;
import me.kall.dragit.data.cape.ClientCapes;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.cape.CapeSavePacket;
import me.kall.dragit.util.ImageCompressor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFWDropCallback;

import java.io.IOException;
import java.util.UUID;

public class CapeDropInvoker implements DragCallback.Invoker {
    @Override
    public boolean invoke(long window, int count, long names) {
        String filePath = GLFWDropCallback.getName(names, 0);
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.options.getCameraType().isFirstPerson()) return false;

        if (!(minecraft.screen instanceof EffectRenderingInventoryScreen)) return false;

        UUID uuid = player.getUUID();
        try {
            byte[] textureBytes = ImageCompressor.compress(filePath, DragClientConfig.INSTANCE.getCapeMaxPixels());
            Identifier textureLocation = ImageCache.getOrCreate(textureBytes);
            if (DragItClient.canSync()) DragNetworker.sendToServer(new CapeSavePacket(uuid, textureLocation, textureBytes));
            ClientCapes.registerCape(uuid, textureLocation, textureBytes);
            return true;
        } catch (IOException exception) {
            DragIt.LOGGER.error("Error registering cape", exception);
        }
        return false;
    }
}

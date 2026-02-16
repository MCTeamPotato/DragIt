package me.kall.dragit.callback.invoker;

import me.kall.dragit.DragIt;
import me.kall.dragit.data.skin.ClientSkins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFWDropCallback;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

public class SkinDropInvoker implements Invoker {
    public static final SkinDropInvoker INSTANCE = new SkinDropInvoker();

    @Override
    public boolean invoke(int count, long names) {
        String filePath = GLFWDropCallback.getName(names, 0);
        try (FileInputStream skinFile = new FileInputStream(filePath)) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (minecraft.screen instanceof EffectRenderingInventoryScreen && player != null) {
                UUID uuid = player.getUUID();
                ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "skin_" + Math.abs(filePath.hashCode()));
                byte[] textureBytes = skinFile.readAllBytes();
                ClientSkins.registerSkin(uuid, textureLocation, textureBytes);
                return true;
            }
        } catch (IOException ioException) {
            DragIt.LOGGER.error("Failed to load skin: {}", filePath);
            DragIt.LOGGER.error(ioException.getMessage(), ioException);
        }
        return false;
    }
}

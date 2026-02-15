package me.kall.dragit.callback;

import me.kall.dragit.DragIt;
import me.kall.dragit.data.skin.ClientSkins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFWDropCallback;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

public class SkinDropCallback {
    public static void invoke(int count, long names) {
        for (int i = 0; i < count; i++) {
            String filePath = GLFWDropCallback.getName(names, i);
            try (FileInputStream skinFile = new FileInputStream(filePath)) {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player == null) return;

                UUID uuid = player.getUUID();
                ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "skin_" + Math.abs(filePath.hashCode()));
                byte[] textureBytes = skinFile.readAllBytes();
                ClientSkins.registerSkin(uuid, textureLocation, textureBytes);
            } catch (IOException ioException) {
                DragIt.LOGGER.error("Failed to load skin: {}", filePath);
                DragIt.LOGGER.error(ioException.getMessage(), ioException);
            }
        }
    }
}

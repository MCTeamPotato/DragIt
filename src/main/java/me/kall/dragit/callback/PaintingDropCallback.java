package me.kall.dragit.callback;

import me.kall.dragit.DragIt;
import me.kall.dragit.data.painting.ClientPaintings;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.painting.PaintingSavePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFWDropCallback;

import java.io.FileInputStream;
import java.io.IOException;

public class PaintingDropCallback {
    public static void invoke(int count, long names) {
        for (int i = 0; i < count; i++) {
            String filePath = GLFWDropCallback.getName(names, i);
            try (FileInputStream imageFile = new FileInputStream(filePath)) {

                Minecraft minecraft = Minecraft.getInstance();
                LocalPlayer player = minecraft.player;
                ClientLevel level = minecraft.level;

                if (player == null || level == null) return;

                HitResult target = minecraft.hitResult;
                if (target == null || !target.getType().equals(HitResult.Type.ENTITY)) return;

                Entity entity = ((EntityHitResult)target).getEntity();
                if (entity instanceof Painting painting) {

                    ResourceLocation dimension = level.dimension().location();
                    long pos = painting.blockPosition().asLong();
                    ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "painting_" + Math.abs(filePath.hashCode()));
                    byte[] textureBytes = imageFile.readAllBytes();

                    DragNetworker.INSTANCE.sendToServer(new PaintingSavePacket(dimension, pos, textureLocation, textureBytes));
                    DragIt.LOGGER.info("Delivering image {} to server. Size: {} bytes.", textureLocation.toString(), textureBytes.length + 16);
                    ClientPaintings.registerPainting(dimension, pos, textureBytes, textureLocation);
                }
            } catch (IOException exception) {
                DragIt.LOGGER.error("Failed to load image: {}", filePath);
                DragIt.LOGGER.error(exception.getMessage(), exception);
            }
        }
    }
}

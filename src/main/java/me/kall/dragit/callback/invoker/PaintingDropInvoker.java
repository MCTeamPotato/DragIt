package me.kall.dragit.callback.invoker;

import me.kall.dragit.DragIt;
import me.kall.dragit.DragItClient;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.callback.DragCallback;
import me.kall.dragit.config.DragClientConfig;
import me.kall.dragit.data.painting.ClientPaintings;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.painting.PaintingSavePacket;
import me.kall.dragit.util.ImageCompressor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFWDropCallback;

import java.io.IOException;

public class PaintingDropInvoker implements DragCallback.Invoker {
    @Override
    public boolean invoke(long window, int count, long names) {
        String filePath = GLFWDropCallback.getName(names, 0);
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        HitResult target = minecraft.hitResult;

        if (level == null || target == null || !target.getType().equals(HitResult.Type.ENTITY)) return false;
        if (!(((EntityHitResult) target).getEntity() instanceof Painting painting)) return false;

        try {
            ResourceLocation dimension = level.dimension().location();
            long pos = painting.blockPosition().asLong();
            byte[] textureBytes = ImageCompressor.compress(filePath, DragClientConfig.INSTANCE.getPaintingMaxPixels());
            ResourceLocation textureLocation = ImageCache.getOrCreate(textureBytes);

            ClientPaintings.registerPainting(dimension, pos, textureBytes, textureLocation);
            if (DragItClient.canSync()) DragNetworker.INSTANCE.sendToServer(new PaintingSavePacket(dimension, pos, textureLocation, textureBytes));
            return true;
        } catch (IOException exception) {
            DragIt.LOGGER.error("Error registering painting", exception);
        }
        return false;
    }
}
package me.kall.dragit.data;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import me.kall.dragit.network.ImageSavePacket;
import me.kall.dragit.network.ImageSyncManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

@Mod.EventBusSubscriber(modid = DragIt.MOD_ID, value = Dist.CLIENT)
public class ClientImages {
    public static final Object2ObjectMap<ResourceLocation, Long2ObjectMap<Image>> IMAGES = new Object2ObjectOpenHashMap<>();

    public static void registerImage(ResourceLocation dimension, long pos, @NotNull NativeImage image, ResourceLocation textureLocation) {
        try {
            DynamicTexture dynamicTexture = new DynamicTexture(image);
            Minecraft.getInstance().getTextureManager().register(textureLocation, dynamicTexture);
            Image old = IMAGES.computeIfAbsent(dimension, key -> new Long2ObjectOpenHashMap<>()).put(pos, new Image(textureLocation, dynamicTexture, image.asByteArray()));
            if (old != null) {
                Minecraft.getInstance().getTextureManager().release(old.textureLocation());
                old.dynamicTexture().close();
            }
        } catch (IOException ioException) {
            DragIt.LOGGER.error("Error saving image", ioException);
        }
    }

    @SubscribeEvent
    public static void saveImages(ClientPlayerNetworkEvent.LoggingOut event) {
        for (Map.Entry<ResourceLocation, Long2ObjectMap<Image>> dimensionEntry : IMAGES.entrySet()) {
            ResourceLocation dimension = dimensionEntry.getKey();
            for (Long2ObjectMap.Entry<Image> imageEntry : dimensionEntry.getValue().long2ObjectEntrySet()) {
                long pos = imageEntry.getLongKey();
                Image image = imageEntry.getValue();
                ResourceLocation textureLocation = image.textureLocation();
                byte[] textureBytes = image.textureBytes();
                ImageSyncManager.INSTANCE.sendToServer(new ImageSavePacket(dimension, pos, textureLocation, textureBytes));
                DragIt.LOGGER.info("Delivering image {} to server. Size: {} bytes.", textureLocation.toString(), textureBytes.length + 16);
                Minecraft.getInstance().getTextureManager().release(textureLocation);
                image.dynamicTexture().close();
            }
        }
        IMAGES.clear();
    }

    public record Image(ResourceLocation textureLocation, DynamicTexture dynamicTexture, byte[] textureBytes) {}
}

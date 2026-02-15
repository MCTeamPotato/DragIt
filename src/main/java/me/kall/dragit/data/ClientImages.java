package me.kall.dragit.data;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = DragIt.MOD_ID, value = Dist.CLIENT)
public class ClientImages {
    public static final Object2ObjectMap<ResourceLocation, Long2ObjectMap<Image>> IMAGES = new Object2ObjectOpenHashMap<>();

    public static void registerImage(ResourceLocation dimension, long pos, byte[] imageBytes, ResourceLocation textureLocation) {
        try {
            DynamicTexture dynamicTexture = new DynamicTexture(NativeImage.read(imageBytes));
            Minecraft.getInstance().getTextureManager().register(textureLocation, dynamicTexture);
            Image old = IMAGES.computeIfAbsent(dimension, key -> new Long2ObjectOpenHashMap<>()).put(pos, new Image(textureLocation, dynamicTexture, imageBytes));
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
        for (Long2ObjectMap<Image> imageMap : IMAGES.values()) {
            for (Long2ObjectMap.Entry<Image> imageEntry : imageMap.long2ObjectEntrySet()) {
                Image image = imageEntry.getValue();
                ResourceLocation textureLocation = image.textureLocation();
                Minecraft.getInstance().getTextureManager().release(textureLocation);
                image.dynamicTexture().close();
            }
        }
        IMAGES.clear();
    }

    @SubscribeEvent
    public static void removeImage(@NotNull EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Painting painting && painting.level() instanceof ClientLevel level) {
            ResourceLocation dimension = level.dimension().location();
            long pos = painting.blockPosition().asLong();

            Long2ObjectMap<ClientImages.Image> imageMap = IMAGES.get(dimension);
            if (imageMap == null) return;
            if (imageMap.containsKey(pos)) {
                Image image = imageMap.get(pos);
                image.dynamicTexture.close();
                Minecraft.getInstance().getTextureManager().release(image.textureLocation);
                imageMap.remove(pos);
            }
            if (imageMap.isEmpty()) IMAGES.remove(dimension);
        }
    }

    public record Image(ResourceLocation textureLocation, DynamicTexture dynamicTexture, byte[] textureBytes) {}
}

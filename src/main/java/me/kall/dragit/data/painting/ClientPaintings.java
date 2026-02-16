package me.kall.dragit.data.painting;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = DragIt.MOD_ID, value = Dist.CLIENT)
public class ClientPaintings {
    public static final Object2ObjectMap<ResourceLocation, Long2ObjectMap<Painting>> PAINTINGS = new Object2ObjectOpenHashMap<>();

    public static void registerPainting(ResourceLocation dimension, long pos, byte[] textureBytes, ResourceLocation textureLocation) {
        try {
            if (PAINTINGS.containsKey(dimension)) {
                Long2ObjectMap<Painting> positionMap = PAINTINGS.get(dimension);
                if (positionMap.containsKey(pos)) {
                    Painting removed = positionMap.remove(pos);
                    Minecraft.getInstance().getTextureManager().release(removed.textureLocation());
                    removed.dynamicTexture().close();
                    if (positionMap.isEmpty()) PAINTINGS.remove(dimension);
                }
            }
            DynamicTexture dynamicTexture = new DynamicTexture(NativeImage.read(textureBytes));
            Minecraft.getInstance().getTextureManager().register(textureLocation, dynamicTexture);
            PAINTINGS.computeIfAbsent(dimension, key -> new Long2ObjectOpenHashMap<>()).put(pos, new Painting(textureLocation, dynamicTexture));
        } catch (IOException ioException) {
            DragIt.LOGGER.error("Error saving image", ioException);
        }
    }

    @SubscribeEvent
    public static void saveImages(ClientPlayerNetworkEvent.LoggingOut event) {
        for (Long2ObjectMap<Painting> imageMap : PAINTINGS.values()) {
            for (Long2ObjectMap.Entry<Painting> imageEntry : imageMap.long2ObjectEntrySet()) {
                Painting painting = imageEntry.getValue();
                ResourceLocation textureLocation = painting.textureLocation();
                Minecraft.getInstance().getTextureManager().release(textureLocation);
                painting.dynamicTexture().close();
            }
        }
        PAINTINGS.clear();
    }

    @SubscribeEvent
    public static void removeImage(@NotNull EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.decoration.Painting painting && painting.level() instanceof ClientLevel level) {
            ResourceLocation dimension = level.dimension().location();
            long pos = painting.blockPosition().asLong();

            Long2ObjectMap<Painting> imageMap = PAINTINGS.get(dimension);
            if (imageMap == null) return;
            if (imageMap.containsKey(pos)) {
                Painting image = imageMap.get(pos);
                image.dynamicTexture().close();
                Minecraft.getInstance().getTextureManager().release(image.textureLocation());
                imageMap.remove(pos);
            }
            if (imageMap.isEmpty()) PAINTINGS.remove(dimension);
        }
    }

    public record Painting(ResourceLocation textureLocation, DynamicTexture dynamicTexture) {}
}

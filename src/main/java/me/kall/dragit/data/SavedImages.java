package me.kall.dragit.data;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import me.kall.dragit.network.ImageLoadPacket;
import me.kall.dragit.network.ImageSyncManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Mod.EventBusSubscriber(modid = DragIt.MOD_ID)
public class SavedImages extends SavedData {
    private static final String DATA_NAME = "DragIt";

    public final Object2ObjectMap<ResourceLocation, Long2ObjectMap<Image>> images = new Object2ObjectOpenHashMap<>();

    public static @NotNull SavedImages load(@NotNull CompoundTag tag) {
        SavedImages data = new SavedImages();

        CompoundTag imagesTag = tag.getCompound("Images");
        for (String key : imagesTag.getAllKeys()) {
            ResourceLocation resourceLocation = ResourceLocation.parse(key);
            CompoundTag dimensionTag = imagesTag.getCompound(key);

            Long2ObjectMap<Image> dimensionImages = new Long2ObjectOpenHashMap<>();
            ListTag imagesList = dimensionTag.getList("ImagesList", Tag.TAG_COMPOUND);

            for (int i = 0; i < imagesList.size(); i++) {
                CompoundTag imageTag = imagesList.getCompound(i);
                dimensionImages.put(imageTag.getLong("Pos"), new Image(ResourceLocation.parse(imageTag.getString("TextureLocation")), imageTag.getByteArray("DynamicTexture")));
            }

            data.images.put(resourceLocation, dimensionImages);
        }

        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        CompoundTag imagesTag = new CompoundTag();

        for (Object2ObjectMap.Entry<ResourceLocation, Long2ObjectMap<Image>> entry : this.images.object2ObjectEntrySet()) {
            CompoundTag dimensionTag = new CompoundTag();
            ListTag imagesList = new ListTag();

            for (Long2ObjectMap.Entry<Image> imageEntry : entry.getValue().long2ObjectEntrySet()) {
                CompoundTag imageTag = new CompoundTag();
                imageTag.putLong("Pos", imageEntry.getLongKey());
                imageTag.putString("TextureLocation", imageEntry.getValue().textureLocation().toString());
                imageTag.putByteArray("DynamicTexture", imageEntry.getValue().textureBytes());

                imagesList.add(imageTag);
            }

            dimensionTag.put("ImagesList", imagesList);
            imagesTag.put(entry.getKey().toString(), dimensionTag);
        }

        tag.put("Images", imagesTag);
        return tag;
    }

    public static @NotNull SavedImages get(@NotNull ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(SavedImages::load, SavedImages::new, DATA_NAME);
    }

    @SubscribeEvent
    public static void sendImages(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.level() instanceof ServerLevel level) {
            SavedImages savedImages = get(level);
            for (Map.Entry<ResourceLocation, Long2ObjectMap<Image>> dimensionEntry : savedImages.images.entrySet()) {
                ResourceLocation dimension = dimensionEntry.getKey();
                for (Long2ObjectMap.Entry<Image> imageEntry : dimensionEntry.getValue().long2ObjectEntrySet()) {
                    Image image = imageEntry.getValue();
                    ResourceLocation textureLocation = image.textureLocation();
                    byte[] textureBytes = image.textureBytes();
                    ImageSyncManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ImageLoadPacket(dimension, imageEntry.getLongKey(), textureLocation, textureBytes));
                    DragIt.LOGGER.info("Delivering image {} to client. Size: {} bytes.", textureLocation.toString(), textureBytes.length + 16);
                }
            }
            savedImages.images.clear();
            savedImages.setDirty();
        }
    }

    public record Image(ResourceLocation textureLocation, byte[] textureBytes) {}
}
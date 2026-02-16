package me.kall.dragit.data.painting;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import me.kall.dragit.network.painting.PaintingLoadPacket;
import me.kall.dragit.network.DragNetworker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Mod.EventBusSubscriber(modid = DragIt.MOD_ID)
public class SavedPaintings extends SavedData {
    private static final String DATA_NAME = "DragItSavedPaintings";

    public final Object2ObjectMap<ResourceLocation, Long2ObjectMap<Painting>> paintings = new Object2ObjectOpenHashMap<>();

    public void removeImage(ResourceLocation dimension, long pos) {
        Long2ObjectMap<Painting> imageMap = this.paintings.get(dimension);
        if (imageMap == null) return;
        imageMap.remove(pos);
        if (imageMap.isEmpty()) this.paintings.remove(dimension);
    }

    public static @NotNull SavedPaintings load(@NotNull CompoundTag tag) {
        SavedPaintings data = new SavedPaintings();

        CompoundTag imagesTag = tag.getCompound("Paintings");
        for (String key : imagesTag.getAllKeys()) {
            ResourceLocation resourceLocation = ResourceLocation.parse(key);
            CompoundTag dimensionTag = imagesTag.getCompound(key);

            Long2ObjectMap<Painting> dimensionImages = new Long2ObjectOpenHashMap<>();
            ListTag imagesList = dimensionTag.getList("PaintingList", Tag.TAG_COMPOUND);

            for (int i = 0; i < imagesList.size(); i++) {
                CompoundTag imageTag = imagesList.getCompound(i);
                dimensionImages.put(imageTag.getLong("Pos"), new Painting(ResourceLocation.parse(imageTag.getString("TextureLocation")), imageTag.getByteArray("TextureBytes")));
            }

            data.paintings.put(resourceLocation, dimensionImages);
        }

        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        CompoundTag imagesTag = new CompoundTag();

        for (Object2ObjectMap.Entry<ResourceLocation, Long2ObjectMap<Painting>> entry : this.paintings.object2ObjectEntrySet()) {
            CompoundTag dimensionTag = new CompoundTag();
            ListTag imagesList = new ListTag();

            for (Long2ObjectMap.Entry<Painting> imageEntry : entry.getValue().long2ObjectEntrySet()) {
                CompoundTag imageTag = new CompoundTag();
                imageTag.putLong("Pos", imageEntry.getLongKey());
                imageTag.putString("TextureLocation", imageEntry.getValue().textureLocation().toString());
                imageTag.putByteArray("TextureBytes", imageEntry.getValue().textureBytes());

                imagesList.add(imageTag);
            }

            dimensionTag.put("PaintingList", imagesList);
            imagesTag.put(entry.getKey().toString(), dimensionTag);
        }

        tag.put("Paintings", imagesTag);
        return tag;
    }

    public static @NotNull SavedPaintings get(@NotNull ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(SavedPaintings::load, SavedPaintings::new, DATA_NAME);
    }

    @SubscribeEvent
    public static void sendImages(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.level() instanceof ServerLevel level) {
            SavedPaintings savedPaintings = get(level);
            for (Map.Entry<ResourceLocation, Long2ObjectMap<Painting>> dimensionEntry : savedPaintings.paintings.entrySet()) {
                ResourceLocation dimension = dimensionEntry.getKey();
                for (Long2ObjectMap.Entry<Painting> imageEntry : dimensionEntry.getValue().long2ObjectEntrySet()) {
                    Painting painting = imageEntry.getValue();
                    ResourceLocation textureLocation = painting.textureLocation();
                    byte[] textureBytes = painting.textureBytes();
                    DragNetworker.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PaintingLoadPacket(dimension, imageEntry.getLongKey(), textureLocation, textureBytes));
                    DragIt.LOGGER.info("Delivering image {} to client. Size: {} bytes.", textureLocation.toString(), textureBytes.length + 16);
                }
            }
        }
    }

    @SubscribeEvent
    public static void removeImage(@NotNull EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.decoration.Painting painting && painting.level() instanceof ServerLevel level) {
            ResourceLocation dimension = level.dimension().location();
            long pos = painting.blockPosition().asLong();
            SavedPaintings.get(level).removeImage(dimension, pos);
        }
    }

    public record Painting(ResourceLocation textureLocation, byte[] textureBytes) {}
}
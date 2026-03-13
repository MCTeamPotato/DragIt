package me.kall.dragit.data.painting;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import me.kall.dragit.data.SavedTextureData;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.painting.PaintingLoadPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@EventBusSubscriber(modid = DragIt.MOD_ID)
public class SavedPaintings extends SavedData {
    private static final String DATA_NAME = "DragItSavedPaintings";

    public final Object2ObjectMap<Identifier, Long2ObjectMap<SavedTextureData>> paintings = new Object2ObjectOpenHashMap<>();

    public void removeImage(Identifier dimension, long pos) {
        Long2ObjectMap<SavedTextureData> imageMap = this.paintings.get(dimension);
        if (imageMap == null) return;
        imageMap.remove(pos);
        if (imageMap.isEmpty()) this.paintings.remove(dimension);
        this.setDirty();
    }

    public static @NotNull SavedPaintings load(@NotNull CompoundTag tag) {
        SavedPaintings data = new SavedPaintings();

        CompoundTag imagesTag = tag.getCompound("Paintings").orElseThrow();
        for (String key : imagesTag.keySet()) {
            Identifier resourceLocation = Identifier.parse(key);
            CompoundTag dimensionTag = imagesTag.getCompound(key).orElseThrow();

            Long2ObjectMap<SavedTextureData> dimensionImages = new Long2ObjectOpenHashMap<>();
            ListTag imagesList = dimensionTag.getList("PaintingList").orElseThrow();

            for (int i = 0; i < imagesList.size(); i++) {
                CompoundTag imageTag = imagesList.getCompound(i).orElseThrow();
                dimensionImages.put(imageTag.getLong("Pos").orElseThrow().longValue(), new SavedTextureData(Identifier.parse(imageTag.getString("TextureLocation").orElseThrow()), imageTag.getByteArray("TextureBytes").orElseThrow()));
            }

            data.paintings.put(resourceLocation, dimensionImages);
        }

        return data;
    }

    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        CompoundTag imagesTag = new CompoundTag();

        for (Object2ObjectMap.Entry<Identifier, Long2ObjectMap<SavedTextureData>> entry : this.paintings.object2ObjectEntrySet()) {
            CompoundTag dimensionTag = new CompoundTag();
            ListTag imagesList = new ListTag();

            for (Long2ObjectMap.Entry<SavedTextureData> imageEntry : entry.getValue().long2ObjectEntrySet()) {
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
        return level.getDataStorage().computeIfAbsent(new SavedDataType<>(DATA_NAME, SavedPaintings::new, new Codec<>() {
            @Override
            public <T> DataResult<Pair<SavedPaintings, T>> decode(DynamicOps<T> ops, T input) {
                return DataResult.success(Pair.of(load((CompoundTag) ops.convertTo(NbtOps.INSTANCE, input)), input));
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> DataResult<T> encode(SavedPaintings input, DynamicOps<T> ops, T prefix) {
                return DataResult.success((T) input.save(new CompoundTag()));
            }
        }));
    }

    @SubscribeEvent
    public static void sendImages(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = player.level();
        for (Map.Entry<Identifier, Long2ObjectMap<SavedTextureData>> dimEntry : get(level).paintings.entrySet()) {
            Identifier dimension = dimEntry.getKey();
            for (Long2ObjectMap.Entry<SavedTextureData> imgEntry : dimEntry.getValue().long2ObjectEntrySet()) {
                Identifier textureLocation = imgEntry.getValue().textureLocation();
                DragNetworker.send(player, new PaintingLoadPacket(dimension, imgEntry.getLongKey(), textureLocation, null));
                DragIt.LOGGER.info("Delivering painting [{}] to {} (null bytes).", textureLocation, player.getName().getString());
            }
        }
    }

    @SubscribeEvent
    public static void removeImage(@NotNull EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Painting painting && painting.level() instanceof ServerLevel level) {
            Entity.RemovalReason removalReason = painting.getRemovalReason();
            if (removalReason != Entity.RemovalReason.KILLED && removalReason != Entity.RemovalReason.DISCARDED) return;

            Identifier dimension = level.dimension().identifier();
            long pos = painting.blockPosition().asLong();
            SavedPaintings.get(level).removeImage(dimension, pos);
        }
    }
}
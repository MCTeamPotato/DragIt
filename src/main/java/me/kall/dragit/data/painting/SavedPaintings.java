package me.kall.dragit.data.painting;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import me.kall.dragit.data.SavedTextureData;
import me.kall.dragit.ext.Leaving;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.painting.PaintingLoadPacket;
import me.kall.dragit.network.remove.HangingRemovePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Mod.EventBusSubscriber(modid = DragIt.MOD_ID)
public class SavedPaintings extends SavedData {
    private static final String DATA_NAME = "DragItSavedPaintings";

    public final Object2ObjectMap<ResourceLocation, Long2ObjectMap<SavedTextureData>> paintings = new Object2ObjectOpenHashMap<>();

    public SavedPaintings() {
        super(DATA_NAME);
    }

    public void removeImage(ResourceLocation dimension, long pos) {
        Long2ObjectMap<SavedTextureData> imageMap = this.paintings.get(dimension);
        if (imageMap == null) return;
        imageMap.remove(pos);
        if (imageMap.isEmpty()) this.paintings.remove(dimension);
        this.setDirty();
    }

    public void load(@NotNull CompoundTag tag) {
        CompoundTag imagesTag = tag.getCompound("Paintings");
        for (String key : imagesTag.getAllKeys()) {
            ResourceLocation resourceLocation = new ResourceLocation(key);
            CompoundTag dimensionTag = imagesTag.getCompound(key);

            Long2ObjectMap<SavedTextureData> dimensionImages = new Long2ObjectOpenHashMap<>();
            ListTag imagesList = dimensionTag.getList("PaintingList", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < imagesList.size(); i++) {
                CompoundTag imageTag = imagesList.getCompound(i);
                dimensionImages.put(imageTag.getLong("Pos"), new SavedTextureData(new ResourceLocation(imageTag.getString("TextureLocation")), imageTag.getByteArray("TextureBytes")));
            }

            this.paintings.put(resourceLocation, dimensionImages);
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        CompoundTag imagesTag = new CompoundTag();

        for (Object2ObjectMap.Entry<ResourceLocation, Long2ObjectMap<SavedTextureData>> entry : this.paintings.object2ObjectEntrySet()) {
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
        return level.getDataStorage().computeIfAbsent(SavedPaintings::new, DATA_NAME);
    }

    @SubscribeEvent
    public static void sendImages(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer)) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();
        for (Map.Entry<ResourceLocation, Long2ObjectMap<SavedTextureData>> dimEntry : get(player.getLevel()).paintings.entrySet()) {
            ResourceLocation dimension = dimEntry.getKey();
            for (Long2ObjectMap.Entry<SavedTextureData> imgEntry : dimEntry.getValue().long2ObjectEntrySet()) {
                ResourceLocation textureLocation = imgEntry.getValue().textureLocation();
                DragNetworker.send(player, new PaintingLoadPacket(dimension, imgEntry.getLongKey(), textureLocation, null));
                DragIt.LOGGER.info("Delivering painting [{}] to {} (null bytes).", textureLocation, player.getName().getString());
            }
        }
    }

    @SubscribeEvent
    public static void removeImage(@NotNull EntityLeaveWorldEvent event) {
        if (event.getEntity() instanceof Painting && event.getWorld() instanceof ServerLevel) {
            Painting painting = (Painting) event.getEntity();
            ServerLevel level = (ServerLevel) event.getWorld();
            if (((Leaving)painting).dragIt$keepData()) return;

            ResourceLocation dimension = level.dimension().location();
            long pos = painting.blockPosition().asLong();
            SavedPaintings.get(level).removeImage(dimension, pos);
            DragNetworker.send(new HangingRemovePacket(dimension, pos, HangingRemovePacket.PAINTING));
        }
    }
}
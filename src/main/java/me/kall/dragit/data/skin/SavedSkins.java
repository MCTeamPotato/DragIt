package me.kall.dragit.data.skin;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SavedSkins extends SavedData {
    private static final String DATA_NAME = "DragItSavedSkins";

    public final Object2ObjectMap<UUID, Skin> skins = new Object2ObjectOpenHashMap<>();

    public static @NotNull SavedSkins load(@NotNull CompoundTag tag) {
        SavedSkins data = new SavedSkins();

        ListTag skinsList = tag.getList("Skins", Tag.TAG_COMPOUND);
        for (int i = 0; i < skinsList.size(); i++) {
            CompoundTag skinTag = skinsList.getCompound(i);
            data.skins.put(skinTag.getUUID("UUID"), new Skin(ResourceLocation.parse(skinTag.getString("TextureLocation")), skinTag.getByteArray("TextureBytes")));
        }

        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag skinsList = new ListTag();

        for (var entry : this.skins.object2ObjectEntrySet()) {
            CompoundTag skinTag = new CompoundTag();
            skinTag.putUUID("UUID", entry.getKey());
            skinTag.putString("TextureLocation", entry.getValue().textureLocation().toString());
            skinTag.putByteArray("TextureBytes", entry.getValue().textureBytes());
            skinsList.add(skinTag);
        }

        tag.put("Skins", skinsList);
        return tag;
    }

    public static @NotNull SavedSkins get(@NotNull ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(SavedSkins::load, SavedSkins::new, DATA_NAME);
    }

    public record Skin(ResourceLocation textureLocation, byte[] textureBytes) {}
}
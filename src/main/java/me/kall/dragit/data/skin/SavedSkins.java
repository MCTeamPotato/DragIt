package me.kall.dragit.data.skin;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import me.kall.dragit.data.SavedTextureData;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.skin.SkinLoadPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = DragIt.MOD_ID)
public class SavedSkins extends SavedData {
    private static final String DATA_NAME = "DragItSavedSkins";

    public final Object2ObjectMap<UUID, SavedTextureData> skins = new Object2ObjectOpenHashMap<>();

    public static @NotNull SavedSkins load(@NotNull CompoundTag tag) {
        SavedSkins data = new SavedSkins();

        ListTag skinsList = tag.getList("Skins").orElseThrow();
        for (int i = 0; i < skinsList.size(); i++) {
            CompoundTag skinTag = skinsList.getCompound(i).orElseThrow();
            data.skins.put(UUID.fromString(skinTag.getString("UUID").orElseThrow()), new SavedTextureData(Identifier.parse(skinTag.getString("TextureLocation").orElseThrow()), skinTag.getByteArray("TextureBytes").orElseThrow()));
        }

        return data;
    }

    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag skinsList = new ListTag();

        for (var entry : this.skins.object2ObjectEntrySet()) {
            CompoundTag skinTag = new CompoundTag();
            skinTag.putString("UUID", entry.getKey().toString());
            skinTag.putString("TextureLocation", entry.getValue().textureLocation().toString());
            skinTag.putByteArray("TextureBytes", entry.getValue().textureBytes());
            skinsList.add(skinTag);
        }

        tag.put("Skins", skinsList);
        return tag;
    }

    public static @NotNull SavedSkins get(@NotNull ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedDataType<>(DATA_NAME, SavedSkins::new, new Codec<>() {
            @Override
            public <T> DataResult<Pair<SavedSkins, T>> decode(DynamicOps<T> ops, T input) {
                return DataResult.success(Pair.of(load((CompoundTag) ops.convertTo(NbtOps.INSTANCE, input)), input));
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> DataResult<T> encode(SavedSkins input, DynamicOps<T> ops, T prefix) {
                return DataResult.success((T) input.save(new CompoundTag()));
            }
        }));
    }

    @SubscribeEvent
    public static void sendSkins(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = player.level();
        for (Map.Entry<UUID, SavedTextureData> entry : get(level).skins.entrySet()) {
            UUID uuid = entry.getKey();
            Identifier textureLocation = entry.getValue().textureLocation();
            DragNetworker.send(player, new SkinLoadPacket(uuid, textureLocation, null));
            DragIt.LOGGER.info("Delivering skin [{}] to {} (null bytes, client will use cache).", textureLocation, player.getName().getString());
        }
    }
}
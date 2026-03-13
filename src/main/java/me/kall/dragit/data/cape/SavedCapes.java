package me.kall.dragit.data.cape;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import me.kall.dragit.data.SavedTextureData;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.cape.CapeLoadPacket;
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
public class SavedCapes extends SavedData {
    private static final String DATA_NAME = "DragItSavedCapes";

    public final Object2ObjectMap<UUID, SavedTextureData> capes = new Object2ObjectOpenHashMap<>();

    public static @NotNull SavedCapes load(@NotNull CompoundTag tag) {
        SavedCapes data = new SavedCapes();

        ListTag capesList = tag.getList("Capes").orElseThrow();
        for (int i = 0; i < capesList.size(); i++) {
            CompoundTag capeTag = capesList.getCompound(i).orElseThrow();
            data.capes.put(UUID.fromString(capeTag.getString("UUID").orElseThrow()), new SavedTextureData(Identifier.parse(capeTag.getString("TextureLocation").orElseThrow()), capeTag.getByteArray("TextureBytes").orElseThrow()));
        }

        return data;
    }

    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag capesList = new ListTag();

        for (var entry : this.capes.object2ObjectEntrySet()) {
            CompoundTag capeTag = new CompoundTag();
            capeTag.putString("UUID", entry.getKey().toString());
            capeTag.putString("TextureLocation", entry.getValue().textureLocation().toString());
            capeTag.putByteArray("TextureBytes", entry.getValue().textureBytes());
            capesList.add(capeTag);
        }

        tag.put("Capes", capesList);
        return tag;
    }

    public static @NotNull SavedCapes get(@NotNull ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedDataType<>(DATA_NAME, SavedCapes::new, new Codec<>() {
            @Override
            public <T> DataResult<Pair<SavedCapes, T>> decode(DynamicOps<T> ops, T input) {
                return DataResult.success(Pair.of(load((CompoundTag) ops.convertTo(NbtOps.INSTANCE, input)), input));
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> DataResult<T> encode(SavedCapes input, DynamicOps<T> ops, T prefix) {
                return DataResult.success((T) input.save(new CompoundTag()));
            }
        }));
    }

    @SubscribeEvent
    public static void sendSkins(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = player.level();
        for (Map.Entry<UUID, SavedTextureData> entry : get(level).capes.entrySet()) {
            UUID uuid = entry.getKey();
            Identifier textureLocation = entry.getValue().textureLocation();
            DragNetworker.send(player, new CapeLoadPacket(uuid, textureLocation, null));
            DragIt.LOGGER.info("Delivering cape [{}] to {} (null bytes, client will use cache).", textureLocation, player.getName().getString());
        }
    }
}

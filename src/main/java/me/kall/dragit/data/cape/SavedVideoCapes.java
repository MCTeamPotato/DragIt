package me.kall.dragit.data.cape;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.cape.VideoCapeLoadPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
public class SavedVideoCapes extends SavedData {
    private static final String DATA_NAME = "DragItSavedVideoCapes";
    private static final String TAG_LIST = "VideoCapes";

    public final Map<UUID, String> videoCapes = new Object2ObjectOpenHashMap<>();

    public static @NotNull SavedVideoCapes load(@NotNull CompoundTag tag) {
        SavedVideoCapes data = new SavedVideoCapes();
        CompoundTag list = tag.getCompound(TAG_LIST).orElseThrow();
        for (String key : list.keySet()) {
            try {
                data.videoCapes.put(UUID.fromString(key), list.getString(key).orElseThrow());
            } catch (IllegalArgumentException e) {
                DragIt.LOGGER.warn("SavedVideoCapes: skipping invalid UUID key '{}'", key);
            }
        }
        return data;
    }

    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        CompoundTag list = new CompoundTag();
        for (Map.Entry<UUID, String> e : videoCapes.entrySet()) {
            list.putString(e.getKey().toString(), e.getValue());
        }
        tag.put(TAG_LIST, list);
        return tag;
    }

    public static @NotNull SavedVideoCapes get(@NotNull ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedDataType<>(DATA_NAME, SavedVideoCapes::new, new Codec<>() {
            @Override
            public <T> DataResult<Pair<SavedVideoCapes, T>> decode(DynamicOps<T> ops, T input) {
                return DataResult.success(Pair.of(load((CompoundTag) ops.convertTo(NbtOps.INSTANCE, input)), input));
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> DataResult<T> encode(SavedVideoCapes input, DynamicOps<T> ops, T prefix) {
                return DataResult.success((T) input.save(new CompoundTag()));
            }
        }));
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer joiningPlayer)) return;
        ServerLevel level = joiningPlayer.level();

        SavedVideoCapes data = get(level);
        UUID  joiningUUID = joiningPlayer.getUUID();

        for (Map.Entry<UUID, String> entry : data.videoCapes.entrySet()) {
            DragNetworker.send(joiningPlayer, new VideoCapeLoadPacket(entry.getKey(), entry.getValue()));
            DragIt.LOGGER.info("SavedVideoCapes: syncing cape [uuid={}] to {}", entry.getKey(), joiningPlayer.getName().getString());
        }

        String selfPath = data.videoCapes.get(joiningUUID);
        if (selfPath != null) {
            for (ServerPlayer other : joiningPlayer.level().getServer().getPlayerList().getPlayers()) {
                if (!other.getUUID().equals(joiningUUID)) {
                    DragNetworker.send(other, new VideoCapeLoadPacket(joiningUUID, selfPath));
                }
            }
            DragIt.LOGGER.info("SavedVideoCapes: broadcasted {}'s cape to all online players", joiningPlayer.getName().getString());
        }
    }
}
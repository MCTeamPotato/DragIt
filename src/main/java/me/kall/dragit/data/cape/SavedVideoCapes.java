package me.kall.dragit.data.cape;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.cape.VideoCapeLoadPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = DragIt.MOD_ID)
public class SavedVideoCapes extends SavedData {
    private static final String DATA_NAME = "DragItSavedVideoCapes";
    private static final String TAG_LIST = "VideoCapes";

    public final Map<UUID, String> videoCapes = new Object2ObjectOpenHashMap<>();

    public SavedVideoCapes() {
        super(DATA_NAME);
    }

    public void load(@NotNull CompoundTag tag) {
        CompoundTag list = tag.getCompound(TAG_LIST);
        for (String key : list.getAllKeys()) {
            try {
                this.videoCapes.put(UUID.fromString(key), list.getString(key));
            } catch (IllegalArgumentException e) {
                DragIt.LOGGER.warn("SavedVideoCapes: skipping invalid UUID key '{}'", key);
            }
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        CompoundTag list = new CompoundTag();
        for (Map.Entry<UUID, String> e : videoCapes.entrySet()) {
            list.putString(e.getKey().toString(), e.getValue());
        }
        tag.put(TAG_LIST, list);
        return tag;
    }

    public static @NotNull SavedVideoCapes get(@NotNull ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(SavedVideoCapes::new, DATA_NAME);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer)) return;
        ServerPlayer joiningPlayer = (ServerPlayer) event.getEntity();
        if (!(joiningPlayer.level instanceof ServerLevel))       return;

        SavedVideoCapes data = get(joiningPlayer.getLevel());
        UUID  joiningUUID = joiningPlayer.getUUID();

        for (Map.Entry<UUID, String> entry : data.videoCapes.entrySet()) {
            DragNetworker.send(joiningPlayer, new VideoCapeLoadPacket(entry.getKey(), entry.getValue()));
            DragIt.LOGGER.info("SavedVideoCapes: syncing cape [uuid={}] to {}", entry.getKey(), joiningPlayer.getName().getString());
        }

        String selfPath = data.videoCapes.get(joiningUUID);
        if (selfPath != null) {
            for (ServerPlayer other : joiningPlayer.server.getPlayerList().getPlayers()) {
                if (!other.getUUID().equals(joiningUUID)) {
                    DragNetworker.send(other, new VideoCapeLoadPacket(joiningUUID, selfPath));
                }
            }
            DragIt.LOGGER.info("SavedVideoCapes: broadcasted {}'s cape to all online players", joiningPlayer.getName().getString());
        }
    }
}
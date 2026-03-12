package me.kall.dragit.data.cape;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import me.kall.dragit.data.SavedTextureData;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.cape.CapeLoadPacket;
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
import java.util.UUID;

@Mod.EventBusSubscriber(modid = DragIt.MOD_ID)
public class SavedCapes extends SavedData {
    private static final String DATA_NAME = "DragItSavedCapes";

    public final Object2ObjectMap<UUID, SavedTextureData> capes = new Object2ObjectOpenHashMap<>();

    public static @NotNull SavedCapes load(@NotNull CompoundTag tag) {
        SavedCapes data = new SavedCapes();

        ListTag capesList = tag.getList("Capes", Tag.TAG_COMPOUND);
        for (int i = 0; i < capesList.size(); i++) {
            CompoundTag capeTag = capesList.getCompound(i);
            data.capes.put(capeTag.getUUID("UUID"), new SavedTextureData(ResourceLocation.parse(capeTag.getString("TextureLocation")), capeTag.getByteArray("TextureBytes")));
        }

        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag capesList = new ListTag();

        for (var entry : this.capes.object2ObjectEntrySet()) {
            CompoundTag capeTag = new CompoundTag();
            capeTag.putUUID("UUID", entry.getKey());
            capeTag.putString("TextureLocation", entry.getValue().textureLocation().toString());
            capeTag.putByteArray("TextureBytes", entry.getValue().textureBytes());
            capesList.add(capeTag);
        }

        tag.put("Capes", capesList);
        return tag;
    }

    public static @NotNull SavedCapes get(@NotNull ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(SavedCapes::load, SavedCapes::new, DATA_NAME);
    }

    @SubscribeEvent
    public static void sendSkins(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        for (Map.Entry<UUID, SavedTextureData> entry : get(level).capes.entrySet()) {
            UUID uuid = entry.getKey();
            ResourceLocation textureLocation = entry.getValue().textureLocation();
            DragNetworker.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new CapeLoadPacket(uuid, textureLocation, null));
            DragIt.LOGGER.info("Delivering cape [{}] to {} (null bytes, client will use cache).", textureLocation, player.getName().getString());
        }
    }
}

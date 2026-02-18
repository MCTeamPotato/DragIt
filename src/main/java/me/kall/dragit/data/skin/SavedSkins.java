package me.kall.dragit.data.skin;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.kall.dragit.DragIt;
import me.kall.dragit.data.SavedTextureData;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.skin.SkinLoadPacket;
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
public class SavedSkins extends SavedData {
    private static final String DATA_NAME = "DragItSavedSkins";

    public final Object2ObjectMap<UUID, SavedTextureData> skins = new Object2ObjectOpenHashMap<>();

    public static @NotNull SavedSkins load(@NotNull CompoundTag tag) {
        SavedSkins data = new SavedSkins();

        ListTag skinsList = tag.getList("Skins", Tag.TAG_COMPOUND);
        for (int i = 0; i < skinsList.size(); i++) {
            CompoundTag skinTag = skinsList.getCompound(i);
            data.skins.put(skinTag.getUUID("UUID"), new SavedTextureData(ResourceLocation.parse(skinTag.getString("TextureLocation")), skinTag.getByteArray("TextureBytes")));
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

    @SubscribeEvent
    public static void sendSkins(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.level() instanceof ServerLevel level) {
            SavedSkins savedSkins = get(level);
            for (Map.Entry<UUID, SavedTextureData> entry : savedSkins.skins.entrySet()) {
                UUID uuid = entry.getKey();
                SavedTextureData savedTextureData = entry.getValue();
                ResourceLocation textureLocation = savedTextureData.textureLocation();
                byte[] textureBytes = savedTextureData.textureBytes();
                DragNetworker.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SkinLoadPacket(uuid, textureLocation, textureBytes));
                DragIt.LOGGER.info("Delivering skin {} to client. Size: {} bytes.", textureLocation.toString(), textureBytes.length + 16);
            }
        }
    }
}
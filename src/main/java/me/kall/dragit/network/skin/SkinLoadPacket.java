package me.kall.dragit.network.skin;

import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.data.skin.ClientSkins;
import me.kall.dragit.network.base.SkinPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public class SkinLoadPacket extends SkinPacket {
    public SkinLoadPacket(UUID uuid, ResourceLocation textureLocation, byte @Nullable [] textureBytes) {
        super(uuid, textureLocation, textureBytes);
    }

    public SkinLoadPacket(@NotNull FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                byte[] bytes = ImageCache.resolveBytes(this.textureLocation, this.textureBytes, resolved -> ClientSkins.registerSkin(this.uuid, this.textureLocation, resolved));
                if (bytes != null) ClientSkins.registerSkin(this.uuid, this.textureLocation, bytes);
                DragIt.LOGGER.info("SkinLoadPacket handled [{}] uuid={}", this.textureLocation, this.uuid);
            } catch (Throwable t) {
                DragIt.LOGGER.error("Error handling SkinLoadPacket", t);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

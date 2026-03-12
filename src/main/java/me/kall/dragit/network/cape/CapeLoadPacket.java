package me.kall.dragit.network.cape;

import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.data.cape.ClientCapes;
import me.kall.dragit.network.base.SkinPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public class CapeLoadPacket extends SkinPacket {
    public CapeLoadPacket(UUID uuid, ResourceLocation textureLocation, byte @Nullable [] textureBytes) {
        super(uuid, textureLocation, textureBytes);
    }

    public CapeLoadPacket(@NotNull FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                byte[] bytes = ImageCache.resolveBytes(this.textureLocation, this.textureBytes, resolved -> ClientCapes.registerCape(this.uuid, this.textureLocation, resolved));
                if (bytes != null) ClientCapes.registerCape(this.uuid, this.textureLocation, bytes);
                DragIt.LOGGER.info("CapeLoadPacket handled [{}] uuid={}", this.textureLocation, this.uuid);
            } catch (Throwable t) {
                DragIt.LOGGER.error("Error handling CapeLoadPacket", t);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

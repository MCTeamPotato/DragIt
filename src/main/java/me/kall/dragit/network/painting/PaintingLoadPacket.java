package me.kall.dragit.network.painting;

import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.data.painting.ClientPaintings;
import me.kall.dragit.network.base.PaintingPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class PaintingLoadPacket extends PaintingPacket {
    public PaintingLoadPacket(ResourceLocation dimension, long pos, ResourceLocation textureLocation, byte @Nullable [] textureBytes) {
        super(dimension, pos, textureLocation, textureBytes);
    }

    public PaintingLoadPacket(@NotNull FriendlyByteBuf buf) { super(buf); }

    @Override
    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                byte[] bytes = ImageCache.resolveBytes(this.textureLocation, this.textureBytes, resolved -> ClientPaintings.registerPainting(this.dimension, this.pos, resolved, this.textureLocation));
                if (bytes != null) ClientPaintings.registerPainting(this.dimension, this.pos, bytes, this.textureLocation);
                DragIt.LOGGER.info("PaintingLoadPacket handled [{}] dim={} pos=[{}]",
                        this.textureLocation, this.dimension,
                        BlockPos.getX(this.pos) + "," + BlockPos.getY(this.pos) + "," + BlockPos.getZ(this.pos));
            } catch (Throwable t) {
                DragIt.LOGGER.error("Error handling PaintingLoadPacket", t);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

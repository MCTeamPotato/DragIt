package me.kall.dragit.network.painting;

import me.kall.dragit.DragIt;
import me.kall.dragit.data.painting.ClientPaintings;
import me.kall.dragit.network.base.PaintingPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class PaintingLoadPacket extends PaintingPacket {
    public PaintingLoadPacket(ResourceLocation dimension, long pos, ResourceLocation textureLocation, byte[] textureBytes) {
        super(dimension, pos, textureLocation, textureBytes);
    }

    public PaintingLoadPacket(@NotNull FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                ClientPaintings.registerPainting(this.dimension, this.pos, this.textureBytes, this.textureLocation);
                DragIt.LOGGER.info("PaintingLoadPacket handled. TextureLocation: {}.  Dimension: {}. Position: [{}].", this.textureLocation.toString(), this.dimension.toString(), BlockPos.getX(this.pos) + ", " + BlockPos.getY(this.pos) + ", " + BlockPos.getZ(this.pos));
            } catch (Throwable throwable) {
                DragIt.LOGGER.error("Error handling PaintingLoadPacket", throwable);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

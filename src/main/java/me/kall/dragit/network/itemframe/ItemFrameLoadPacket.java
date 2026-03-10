package me.kall.dragit.network.itemframe;

import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.data.itemframe.ClientItemFrames;
import me.kall.dragit.network.base.ItemFramePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ItemFrameLoadPacket extends ItemFramePacket {
    public ItemFrameLoadPacket(ResourceLocation dimension, ResourceLocation textureLocation, byte @Nullable [] textureBytes, long[] positions, int[] columns, int[] rows, int totalColumns, int totalRows) {
        super(dimension, textureLocation, textureBytes, positions, columns, rows, totalColumns, totalRows);
    }

    public ItemFrameLoadPacket(@NotNull FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void handle(@NotNull Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            try {
                byte[] bytes = ImageCache.resolveBytes(this.textureLocation, this.textureBytes, resolved -> ClientItemFrames.registerGroup(this.dimension, this.positions, this.columns, this.rows, this.totalColumns, this.totalRows, this.textureLocation, resolved));
                if (bytes != null) ClientItemFrames.registerGroup(this.dimension, this.positions, this.columns, this.rows, this.totalColumns, this.totalRows, this.textureLocation, bytes);
                DragIt.LOGGER.info("ItemFrameLoadPacket handled [{}] dim={}", this.textureLocation, this.dimension);
            } catch (Throwable t) {
                DragIt.LOGGER.error("Error handling ItemFrameLoadPacket", t);
            }
        });
        context.get().setPacketHandled(true);
    }
}

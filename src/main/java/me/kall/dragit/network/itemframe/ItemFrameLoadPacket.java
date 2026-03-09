package me.kall.dragit.network.itemframe;

import me.kall.dragit.DragIt;
import me.kall.dragit.data.itemframe.ClientItemFrames;
import me.kall.dragit.network.base.ItemFramePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ItemFrameLoadPacket extends ItemFramePacket {
    public ItemFrameLoadPacket(ResourceLocation dimension, ResourceLocation textureLocation, byte[] textureBytes, long[] positions, int[] columns, int[] rows, int totalColumns, int totalRows) {
        super(dimension, textureLocation, textureBytes, positions, columns, rows, totalColumns, totalRows);
    }

    public ItemFrameLoadPacket(@NotNull FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void handle(@NotNull Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            try {
                ClientItemFrames.registerGroup(this.dimension, this.positions, this.columns, this.rows, this.totalColumns, this.totalRows, this.textureLocation, this.textureBytes);
                DragIt.LOGGER.info("ItemFrameLoadPacket handled. TextureLocation: {}. Dimension: {}. ", this.textureLocation.toString(), this.dimension.toString());
            } catch (Throwable throwable) {
                DragIt.LOGGER.error("Error handling ItemFrameLoadPacket", throwable);
            }
        });
        context.get().setPacketHandled(true);
    }
}
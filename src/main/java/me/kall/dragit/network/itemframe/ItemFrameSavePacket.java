package me.kall.dragit.network.itemframe;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.kall.dragit.DragIt;
import me.kall.dragit.cache.ImageCache;
import me.kall.dragit.data.itemframe.SavedItemFrames;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.base.ItemFramePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;

public class ItemFrameSavePacket extends ItemFramePacket {
    public ItemFrameSavePacket(ResourceLocation dimension, ResourceLocation textureLocation, byte[] textureBytes, long[] positions, int[] columns, int[] rows, int totalColumns, int totalRows) {
        super(dimension, textureLocation, textureBytes, positions, columns, rows, totalColumns, totalRows);
    }

    public ItemFrameSavePacket(@NotNull FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void handle(@NotNull Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            try {
                ServerPlayer sender = context.get().getSender();
                if (sender == null || this.textureBytes == null) return;

                ImageCache.store(this.textureLocation, this.textureBytes);

                ObjectList<SavedItemFrames.FrameRecord> frameRecords = new ObjectArrayList<>();
                for (int i = 0; i < this.positions.length; i++) frameRecords.add(new SavedItemFrames.FrameRecord(this.positions[i], this.columns[i], this.rows[i]));

                SavedItemFrames.get(sender.serverLevel()).addGroup(new SavedItemFrames.Group(this.dimension, this.textureLocation, this.textureBytes, this.totalColumns, this.totalRows, frameRecords));

                UUID senderUUID = sender.getUUID();
                for (ServerPlayer target : sender.server.getPlayerList().getPlayers()) {
                    if (!target.getUUID().equals(senderUUID)) {
                        DragNetworker.INSTANCE.send(PacketDistributor.PLAYER.with(() -> target), new ItemFrameLoadPacket(this.dimension, this.textureLocation, this.textureBytes, this.positions, this.columns, this.rows, this.totalColumns, this.totalRows));
                    }
                }
                DragIt.LOGGER.info("ItemFrameSavePacket handled [{}] dim={} positions={}", this.textureLocation, this.dimension, Arrays.stream(this.positions).mapToObj(pos -> "[" + BlockPos.getX(pos) + "," + BlockPos.getY(pos) + "," + BlockPos.getZ(pos) + "]").toArray());
            } catch (Throwable t) {
                DragIt.LOGGER.error("Error handling ItemFrameSavePacket", t);
            }
        });
        context.get().setPacketHandled(true);
    }
}

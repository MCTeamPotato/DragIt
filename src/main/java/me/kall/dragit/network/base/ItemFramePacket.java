package me.kall.dragit.network.base;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class ItemFramePacket {
    public final ResourceLocation dimension;
    public final ResourceLocation textureLocation;
    public final byte[] textureBytes;
    public final long[] positions;
    public final int[] columns;
    public final int[] rows;
    public final int totalColumns;
    public final int totalRows;

    public ItemFramePacket(ResourceLocation dimension, ResourceLocation textureLocation, byte[] textureBytes, long[] positions, int[] columns, int[] rows, int totalColumns, int totalRows) {
        this.dimension = dimension;
        this.textureLocation = textureLocation;
        this.textureBytes = textureBytes;
        this.positions = positions;
        this.columns = columns;
        this.rows = rows;
        this.totalColumns = totalColumns;
        this.totalRows = totalRows;
    }

    public ItemFramePacket(@NotNull FriendlyByteBuf buffer) {
        this.dimension = buffer.readResourceLocation();
        this.textureLocation = buffer.readResourceLocation();
        this.textureBytes = buffer.readByteArray();
        this.totalColumns = buffer.readInt();
        this.totalRows = buffer.readInt();
        int frameCount = buffer.readInt();
        this.positions = new long[frameCount];
        this.columns = new int[frameCount];
        this.rows = new int[frameCount];
        for (int index = 0; index < frameCount; index++) {
            this.positions[index] = buffer.readLong();
            this.columns[index] = buffer.readInt();
            this.rows[index] = buffer.readInt();
        }
    }

    public void save(@NotNull FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.dimension);
        buffer.writeResourceLocation(this.textureLocation);
        buffer.writeByteArray(this.textureBytes);
        buffer.writeInt(this.totalColumns);
        buffer.writeInt(this.totalRows);
        buffer.writeInt(this.positions.length);
        for (int index = 0; index < this.positions.length; index++) {
            buffer.writeLong(this.positions[index]);
            buffer.writeInt(this.columns[index]);
            buffer.writeInt(this.rows[index]);
        }
    }

    public abstract void handle(@NotNull Supplier<NetworkEvent.Context> context);
}
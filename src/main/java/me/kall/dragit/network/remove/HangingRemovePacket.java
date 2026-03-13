package me.kall.dragit.network.remove;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.kall.dragit.data.ClientTextureData;
import me.kall.dragit.data.itemframe.ClientItemFrames;
import me.kall.dragit.data.painting.ClientPaintings;
import me.kall.dragit.network.base.Handler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HangingRemovePacket extends Handler {
    private final ResourceLocation dimension;
    private final long pos;
    private final byte type;

    public static final byte PAINTING = 0;
    public static final byte ITEM_FRAME = 1;

    public HangingRemovePacket(ResourceLocation dimension, long pos, byte type) {
        this.dimension = dimension;
        this.pos = pos;
        this.type = type;
    }

    public HangingRemovePacket(@NotNull FriendlyByteBuf buffer) {
        this(buffer.readResourceLocation(), buffer.readLong(), buffer.readByte());
    }

    public void save(@NotNull FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.dimension);
        buffer.writeLong(this.pos);
        buffer.writeByte(this.type);
    }

    @Override
    public void handle(@Nullable ServerPlayer player) {
        if (this.type == PAINTING) {
            Long2ObjectMap<ClientTextureData> imageMap = ClientPaintings.PAINTINGS.get(this.dimension);
            if (imageMap == null) return;
            if (imageMap.containsKey(this.pos)) {
                ClientTextureData image = imageMap.get(this.pos);
                image.dynamicTexture().close();
                Minecraft.getInstance().getTextureManager().release(image.textureLocation());
                imageMap.remove(this.pos);
            }
            if (imageMap.isEmpty()) ClientPaintings.PAINTINGS.remove(this.dimension);
        } else if (this.type == ITEM_FRAME) {
            Long2ObjectMap<ClientItemFrames.FrameEntry> frameMap = ClientItemFrames.ITEM_FRAMES.get(dimension);
            if (frameMap == null) return;
            ClientItemFrames.FrameEntry frameEntry = frameMap.get(pos);
            if (frameEntry == null) return;

            ResourceLocation textureToRemove = frameEntry.textureLocation();
            frameMap.values().removeIf(entry -> entry.textureLocation().equals(textureToRemove));

            Minecraft.getInstance().getTextureManager().release(textureToRemove);
            frameEntry.dynamicTexture().close();

            if (frameMap.isEmpty()) ClientItemFrames.ITEM_FRAMES.remove(dimension);
        }
    }
}

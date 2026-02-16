package me.kall.dragit.network.skin;

import me.kall.dragit.data.skin.ClientSkins;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Supplier;

public class SkinLoadPacket {
    public final UUID uuid;
    public final ResourceLocation textureLocation;
    public final byte[] textureBytes;

    public SkinLoadPacket(UUID uuid, ResourceLocation textureLocation, byte[] textureBytes) {
        this.uuid = uuid;
        this.textureLocation = textureLocation;
        this.textureBytes = textureBytes;
    }

    public SkinLoadPacket(@NotNull FriendlyByteBuf buf) {
        this.uuid = buf.readUUID();
        this.textureLocation = buf.readResourceLocation();
        this.textureBytes = buf.readByteArray();
    }

    public void save(@NotNull FriendlyByteBuf buf) {
        buf.writeUUID(this.uuid);
        buf.writeResourceLocation(this.textureLocation);
        buf.writeByteArray(this.textureBytes);
    }

    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientSkins.registerSkin(this.uuid, this.textureLocation, this.textureBytes));
        ctx.get().setPacketHandled(true);
    }
}

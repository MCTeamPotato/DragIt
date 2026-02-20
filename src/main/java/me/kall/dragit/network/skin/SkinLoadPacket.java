package me.kall.dragit.network.skin;

import me.kall.dragit.DragIt;
import me.kall.dragit.data.skin.ClientSkins;
import me.kall.dragit.network.base.SkinPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Supplier;

public class SkinLoadPacket extends SkinPacket {
    public SkinLoadPacket(UUID uuid, ResourceLocation textureLocation, byte[] textureBytes) {
        super(uuid, textureLocation, textureBytes);
    }

    public SkinLoadPacket(@NotNull FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void handle(@NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            try {
                ClientSkins.registerSkin(this.uuid, this.textureLocation, this.textureBytes);
            } catch (Throwable throwable) {
                DragIt.LOGGER.error("Error handling SkinLoadPacket", throwable);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

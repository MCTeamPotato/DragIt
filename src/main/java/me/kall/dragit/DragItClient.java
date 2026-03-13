package me.kall.dragit;

import com.mojang.blaze3d.platform.NativeImage;
import me.kall.dragit.config.DragCommonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

public class DragItClient {
    public static boolean canSync() {
        DragCommonConfig config = DragCommonConfig.INSTANCE;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;
        UUID uuid = player.getUUID();
        if (config.blacklisted(uuid)) return false;
        return config.all() || config.whitelisted(uuid);
    }

    public static @NotNull NativeImage buildImage(byte @NotNull [] bytes) throws IOException {
        ByteBuffer direct = BufferUtils.createByteBuffer(bytes.length);
        direct.put(bytes).flip();
        return NativeImage.read(direct);
    }
}

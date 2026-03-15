package me.kall.dragit.callback.invoker;

import me.kall.dragit.callback.DragCallback;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.video.VideoScreenPacket;
import me.kall.dragit.util.ItemFrameLocator;
import me.kall.dragit.util.VideoFiles;
import me.kall.narutoloading.common.env.config.NarutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFWDropCallback;

public class VideoDropInvoker implements DragCallback.Invoker {
    @Override
    public boolean invoke(long window, int count, long names) {
        String filePath = GLFWDropCallback.getName(names, 0);
        if (!VideoFiles.isVideoFile(filePath)) return false;
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        HitResult target = minecraft.hitResult;
        LocalPlayer player = minecraft.player;

        if (level == null || target == null || player == null) return false;
        if (target.getType() != HitResult.Type.ENTITY) return false;
        if (!(((EntityHitResult)target).getEntity() instanceof ItemFrame leftBottom)) return false;

        ItemFrame rightBottom = ItemFrameLocator.findOtherCorner(leftBottom, player, level);
        DragNetworker.sendToServer(new VideoScreenPacket(leftBottom.getId(), rightBottom.getId(), NarutoConfig.relative(VideoFiles.copyFileToConfig(filePath))));
        return true;
    }
}

package me.kall.dragit.callback.invoker;

import me.kall.dragit.DragIt;
import me.kall.dragit.callback.DragCallback;
import me.kall.dragit.network.DragNetworker;
import me.kall.dragit.network.video.VideoScreenPacket;
import me.kall.dragit.util.ItemFrameLocator;
import me.kall.narutoloading.common.env.config.NarutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFWDropCallback;

import java.io.IOException;

public class VideoDropInvoker implements DragCallback.Invoker {
    @Override
    public boolean invoke(long window, int count, long names) {
        try {
            String filePath = NarutoConfig.relative(NoWorldDropInvoker.copyFileToConfig(GLFWDropCallback.getName(names, 0)));
            Minecraft minecraft = Minecraft.getInstance();
            ClientLevel level = minecraft.level;
            HitResult target = minecraft.hitResult;
            LocalPlayer player = minecraft.player;

            if (level == null || target == null || player == null) return false;
            if (target.getType() != HitResult.Type.ENTITY) return false;
            if (!(((EntityHitResult)target).getEntity() instanceof ItemFrame)) return false;
            ItemFrame leftBottom = (ItemFrame) ((EntityHitResult)target).getEntity();
            ItemFrame rightBottom = ItemFrameLocator.findOtherCorner(leftBottom, player, level);
            DragNetworker.sendToServer(new VideoScreenPacket(leftBottom.getId(), rightBottom.getId(), filePath));
            return true;
        } catch (IOException exception) {
            DragIt.LOGGER.error("Error copying file to config directory.", exception);
            return false;
        }
    }
}

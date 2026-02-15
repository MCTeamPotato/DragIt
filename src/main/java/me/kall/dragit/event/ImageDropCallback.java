package me.kall.dragit.event;

import com.mojang.blaze3d.platform.NativeImage;
import me.kall.dragit.DragIt;
import me.kall.dragit.data.ClientImages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;

import java.io.FileInputStream;
import java.io.IOException;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = DragIt.MOD_ID, value = Dist.CLIENT)
public class ImageDropCallback extends GLFWDropCallback {
    @Override
    public void invoke(long window, int count, long names) {
        for (int i = 0; i < count; i++) {
            String filePath = GLFWDropCallback.getName(names, i);
            try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
                Minecraft minecraft = Minecraft.getInstance();
                LocalPlayer player = minecraft.player;
                ClientLevel level = minecraft.level;
                if (player == null || level == null) return;
                HitResult target = minecraft.hitResult;
                if (target == null || !target.getType().equals(HitResult.Type.ENTITY)) return;
                Entity entity = ((EntityHitResult)target).getEntity();
                if (entity instanceof Painting painting) {
                    ClientImages.registerImage(level.dimension().location(), painting.blockPosition().asLong(), NativeImage.read(fileInputStream), ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "painting_" + Math.abs(filePath.hashCode())));
                }
            } catch (IOException exception) {
                DragIt.LOGGER.error("Failed to load image: {}", filePath);
                DragIt.LOGGER.error(exception.getMessage(), exception);
            }
        }
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        GLFW.glfwSetDropCallback(Minecraft.getInstance().getWindow().getWindow(), new ImageDropCallback());
    }
}

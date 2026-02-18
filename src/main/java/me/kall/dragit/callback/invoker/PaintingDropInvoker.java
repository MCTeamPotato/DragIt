package me.kall.dragit.callback.invoker;

import me.kall.dragit.callback.DragCallback;
import me.kall.dragit.gui.PaintingConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFWDropCallback;

public class PaintingDropInvoker implements DragCallback.Invoker {
    @Override
    public boolean invoke(long names) {
        String filePath = GLFWDropCallback.getName(names, 0);
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        HitResult target = minecraft.hitResult;

        if (level != null && target != null && target.getType().equals(HitResult.Type.ENTITY) && ((EntityHitResult) target).getEntity() instanceof Painting painting) {
            minecraft.setScreen(new PaintingConfigScreen(filePath, level.dimension().location(), painting.blockPosition().asLong()));
            return true;
        }
        return false;
    }
}
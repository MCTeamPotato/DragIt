package me.kall.dragit;

import me.kall.dragit.config.DragCommonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;

import java.util.UUID;

public class DragItClient {
    public static final ThreadLocal<GuiGraphics> DRAG_IT_GUI_GRAPHICS = new ThreadLocal<>();

    public static boolean canSync() {
        DragCommonConfig config = DragCommonConfig.INSTANCE;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;
        UUID uuid = player.getUUID();
        if (config.blacklisted(uuid)) return false;
        return config.all() || config.whitelisted(uuid);
    }
}

package me.kall.dragit;

import me.kall.dragit.config.DragCommonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@EventBusSubscriber(value = Dist.CLIENT, modid = DragIt.MOD_ID)
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

    private static int interval = 0;

    @SubscribeEvent
    public static void tickInterval(ClientTickEvent.Pre event) {
        interval--;
    }

    @SubscribeEvent
    public static void showGuide(PlayerInteractEvent.@NotNull RightClickItem event) {
        if (interval > 0) return;
        ItemStack stack = event.getItemStack();
        //noinspection DataFlowIssue
        if (stack.is(Items.PAPER) && stack.has(DataComponents.CUSTOM_DATA) && stack.get(DataComponents.CUSTOM_DATA).contains("DragItGuide") && event.getEntity() instanceof LocalPlayer) {
            for (Component component : GuideComponents.GUIDE) {
                Minecraft.getInstance().gui.getChat().addMessage(component);
            }

            interval = 20;
        }
    }
}

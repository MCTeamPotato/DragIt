package me.kall.dragit;

import me.kall.dragit.config.DragCommonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = DragIt.MOD_ID)
public class DragItClient {
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
    public static void tickInterval(TickEvent.@NotNull ClientTickEvent event) {
        if (event.phase.equals(TickEvent.Phase.START)) {
            interval--;
        }
    }

    @SubscribeEvent
    public static void showGuide(PlayerInteractEvent.@NotNull RightClickItem event) {
        if (interval > 0) return;
        ItemStack stack = event.getItemStack();
        //noinspection DataFlowIssue
        if (stack.is(Items.PAPER) && stack.hasTag() && stack.getTag().getBoolean("DragItGuide") && event.getEntity() instanceof LocalPlayer) {
            for (Component component : GuideComponents.GUIDE) {
                Minecraft.getInstance().gui.getChat().addMessage(component);
            }

            interval = 20;
        }
    }
}

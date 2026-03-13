package me.kall.dragit.cache;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.kall.dragit.DragIt;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = DragIt.MOD_ID, value = Dist.CLIENT)
public class PendingRegistrations {
    public interface Registration {
        void apply(byte[] bytes);
    }

    private static final Map<ResourceLocation, List<Registration>> PENDING = new Object2ObjectOpenHashMap<>();

    public static synchronized void add(@NotNull ResourceLocation location, @NotNull Registration registration) {
        PENDING.computeIfAbsent(location, k -> new ObjectArrayList<>()).add(registration);
    }

    public static synchronized void resolve(@NotNull ResourceLocation location, byte @NotNull [] bytes) {
        List<Registration> registrations = PENDING.remove(location);
        if (registrations == null) return;
        for (Registration registration : registrations) {
            try {
                registration.apply(bytes);
            } catch (Throwable t) {
                DragIt.LOGGER.error("PendingRegistrations: error resolving [{}]", location, t);
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        PENDING.clear();
    }
}
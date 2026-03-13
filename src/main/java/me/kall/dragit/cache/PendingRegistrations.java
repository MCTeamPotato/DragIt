package me.kall.dragit.cache;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.kall.dragit.DragIt;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = DragIt.MOD_ID, value = Dist.CLIENT)
public class PendingRegistrations {
    public interface Registration {
        void apply(byte[] bytes);
    }

    private static final Map<Identifier, List<Registration>> PENDING = new Object2ObjectOpenHashMap<>();

    public static synchronized void add(@NotNull Identifier location, @NotNull Registration registration) {
        PENDING.computeIfAbsent(location, k -> new ObjectArrayList<>()).add(registration);
    }

    public static synchronized void resolve(@NotNull Identifier location, byte @NotNull [] bytes) {
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
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        PENDING.clear();
    }
}
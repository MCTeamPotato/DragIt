package me.kall.dragit;

import me.kall.dragit.config.DragClientConfig;
import me.kall.dragit.config.DragCommonConfig;
import me.kall.dragit.network.DragNetworker;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DragIt.MOD_ID)
public final class DragIt {
    public static final String MOD_ID = "dragit";
    public static final Logger LOGGER = LogManager.getLogger(DragIt.class);

    public DragIt(IEventBus modBus, Dist dist, ModContainer container) {
        DragNetworker.register();
        DragCommonConfig.INSTANCE.register(container, modBus);
        if (dist.isClient()) {
            DragClientConfig.INSTANCE.register(container);
        }
    }
}

package me.kall.dragit;

import me.kall.dragit.config.DragChatConfig;
import me.kall.dragit.config.DragCommonConfig;
import me.kall.dragit.network.DragNetworker;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DragIt.MOD_ID)
public final class DragIt {
    public static final String MOD_ID = "dragit";
    public static final Logger LOGGER = LogManager.getLogger(DragIt.class);

    public DragIt(FMLJavaModLoadingContext context) {
        DragNetworker.register();
        DragCommonConfig.INSTANCE.register(context, context.getModEventBus());
        if (FMLLoader.getDist().isClient()) {
            DragChatConfig.INSTANCE.register(context);
        }
    }
}

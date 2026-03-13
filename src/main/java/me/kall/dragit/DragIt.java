package me.kall.dragit;

import me.kall.dragit.config.DragClientConfig;
import me.kall.dragit.config.DragCommonConfig;
import me.kall.dragit.network.DragNetworker;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DragIt.MOD_ID)
public final class DragIt {
    public static final String MOD_ID = "dragit";
    public static final Logger LOGGER = LogManager.getLogger(DragIt.class);

    public DragIt() {
        ModLoadingContext context = ModLoadingContext.get();
        DragNetworker.register();
        DragCommonConfig.INSTANCE.register(context, FMLJavaModLoadingContext.get().getModEventBus());
        if (FMLLoader.getDist().isClient()) {
            DragClientConfig.INSTANCE.register(context);
        }
    }
}

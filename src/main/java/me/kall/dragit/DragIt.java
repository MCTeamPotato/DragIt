package me.kall.dragit;

import me.kall.dragit.network.DragNetworker;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DragIt.MOD_ID)
public final class DragIt {
    public static final String MOD_ID = "dragit";
    public static final Logger LOGGER = LogManager.getLogger(DragIt.class);

    public DragIt(FMLJavaModLoadingContext context) {
        DragNetworker.register();
        context.registerConfig(ModConfig.Type.COMMON, CONFIG);
    }

    public static final ForgeConfigSpec CONFIG;
    public static final ForgeConfigSpec.BooleanValue OP_REQUIRED;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("DragIt");
        OP_REQUIRED = builder
                .comment("If enabled, images data on the players' clients cannot be delivered to server. And so other players cannot see the images.")
                .define("RequireOp", false);
        builder.pop();
        CONFIG = builder.build();
    }
}

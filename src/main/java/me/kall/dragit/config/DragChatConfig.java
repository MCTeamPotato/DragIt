package me.kall.dragit.config;

import me.kall.dragit.config.sodium.SodiumIntegration;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;

public class DragChatConfig {
    public static final DragChatConfig INSTANCE = new DragChatConfig();

    private final ForgeConfigSpec configSpec;
    private final ForgeConfigSpec.IntValue maxWidth, maxHeight;

    private DragChatConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("DragChatConfig");
        this.maxWidth = builder.comment("Max rendering width of the chat images.", "This limitation below doesn't mean to compress or stretch the images. They're scaled proportionally.").defineInRange("MaxWidth", 64, 0, Integer.MAX_VALUE);
        this.maxHeight = builder.comment("Max rendering height of the chat images", "This limitation below doesn't mean to compress or stretch the images. They're scaled proportionally.").defineInRange("MaxHeight", 64, 0, Integer.MAX_VALUE);
        builder.pop();
        this.configSpec = builder.build();
    }

    public void register(@NotNull FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.CLIENT, this.configSpec);
        if (ModList.get().isLoaded("sodium")) SodiumIntegration.register();
    }

    public int getMaxWidth() {
        return this.maxWidth.get();
    }

    public int getMaxHeight() {
        return this.maxHeight.get();
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth.set(maxWidth);
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight.set(maxHeight);
    }
}

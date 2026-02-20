package me.kall.dragit.config;

import me.kall.dragit.config.sodium.SodiumIntegration;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;

public class DragClientConfig {
    public static final DragClientConfig INSTANCE = new DragClientConfig();

    private final ForgeConfigSpec configSpec;
    private final ForgeConfigSpec.IntValue chatMaxWidth, chatMaxHeight, chatMaxPixels, paintingMaxPixels;

    private DragClientConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("DragClientConfig");
        builder.push("Chat");
        this.chatMaxWidth = builder
                .comment(
                        "Max rendering width of the chat images.",
                        "This pixel limitation doesn't mean to compress or stretch the images. They're scaled proportionally."
                )
                .defineInRange("MaxWidth", 64, 0, Integer.MAX_VALUE);
        this.chatMaxHeight = builder
                .comment(
                        "Max rendering height of the chat images.",
                        "This pixel limitation doesn't mean to compress or stretch the images. They're scaled proportionally."
                )
                .defineInRange("MaxHeight", 64, 0, Integer.MAX_VALUE);
        this.chatMaxPixels = builder
                .comment(
                        "Max images' pixels that are allowed to be rendered and sent to server.",
                        "This pixel limitation DOES mean to compress the images. They would become more pixel-like and lose details if you decrease this value."
                )
                .defineInRange("MaxPixels", 8192, 0, Integer.MAX_VALUE);
        builder.pop();
        builder.push("Painting");
        this.paintingMaxPixels = builder
                .comment(
                        "Max images' pixels that are allowed to be rendered and sent to server.",
                        "This pixel limitation DOES mean to compress the images. They would become more pixel-like and lose details if you decrease this value."
                )
                .defineInRange("MaxPixels", 8192, 0, Integer.MAX_VALUE);
        builder.pop();
        builder.pop();
        this.configSpec = builder.build();
    }

    public void register(@NotNull FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.CLIENT, this.configSpec);
        if (FMLLoader.getLoadingModList().getModFileById("embeddium") != null) SodiumIntegration.register();
    }

    public int getChatMaxWidth() {
        return this.chatMaxWidth.get();
    }

    public int getChatMaxHeight() {
        return this.chatMaxHeight.get();
    }

    public int getChatMaxPixels() {
        int clientLimit = this.chatMaxPixels.get();
        int commonLimit = DragCommonConfig.INSTANCE.getMaxPixels();
        if (clientLimit > commonLimit) clientLimit = commonLimit;
        return clientLimit;
    }

    public int getPaintingMaxPixels() {
        int clientLimit = this.paintingMaxPixels.get();
        int commonLimit = DragCommonConfig.INSTANCE.getMaxPixels();
        if (clientLimit > commonLimit) clientLimit = commonLimit;
        return clientLimit;
    }

    public void setChatMaxWidth(int chatMaxWidth) {
        this.chatMaxWidth.set(chatMaxWidth);
    }

    public void setChatMaxHeight(int chatMaxHeight) {
        this.chatMaxHeight.set(chatMaxHeight);
    }

    public void setChatMaxPixels(int chatMaxPixels) {
        this.chatMaxPixels.set(chatMaxPixels);
    }

    public void setPaintingMaxPixels(int paintingMaxPixels) {
        this.paintingMaxPixels.set(paintingMaxPixels);
    }
}

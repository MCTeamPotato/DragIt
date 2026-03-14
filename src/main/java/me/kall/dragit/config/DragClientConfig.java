package me.kall.dragit.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

public class DragClientConfig {
    public static final DragClientConfig INSTANCE = new DragClientConfig();

    private final ModConfigSpec configSpec;
    private final ModConfigSpec.IntValue chatMaxWidth, chatMaxHeight, chatMaxPixels, paintingMaxPixels, itemFrameMaxPixels, capeMaxPixels;

    private DragClientConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("DragClientConfig");
        builder.push("Chat");
        this.chatMaxWidth = builder.defineInRange("MaxWidth", 64, 0, Integer.MAX_VALUE);
        this.chatMaxHeight = builder.defineInRange("MaxHeight", 64, 0, Integer.MAX_VALUE);
        this.chatMaxPixels = builder.defineInRange("MaxPixels", 8192, 0, Integer.MAX_VALUE);
        builder.pop();
        builder.push("Painting");
        this.paintingMaxPixels = builder.defineInRange("MaxPixels", 8192, 0, Integer.MAX_VALUE);
        builder.pop();
        builder.push("ItemFrame");
        this.itemFrameMaxPixels = builder.defineInRange("MaxPixels", 8192, 0, Integer.MAX_VALUE);
        builder.pop();
        builder.push("Cape");
        this.capeMaxPixels = builder.defineInRange("MaxPixels", 8192, 0, Integer.MAX_VALUE);
        builder.pop();
        builder.pop();
        this.configSpec = builder.build();
    }

    public void register(@NotNull ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, this.configSpec);
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

    public int getItemFrameMaxPixels() {
        int clientLimit = this.itemFrameMaxPixels.get();
        int commonLimit = DragCommonConfig.INSTANCE.getMaxPixels();
        if (clientLimit > commonLimit) clientLimit = commonLimit;
        return clientLimit;
    }

    public int getCapeMaxPixels() {
        int clientLimit = this.capeMaxPixels.get();
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

    public void setItemFrameMaxPixels(int itemFrameMaxPixels) {
        this.itemFrameMaxPixels.set(itemFrameMaxPixels);
    }

    public void setCapeMaxPixels(int capeMaxPixels) {
        this.capeMaxPixels.set(capeMaxPixels);
    }
}
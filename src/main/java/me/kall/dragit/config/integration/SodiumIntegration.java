package me.kall.dragit.config.integration;

import me.kall.dragit.config.DragClientConfig;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@ConfigEntryPointForge("dragit")
public class SodiumIntegration implements ConfigEntryPoint {
    private final StorageEventHandler storageHandler = () -> {};

    @Override
    public void registerConfigLate(@NotNull ConfigBuilder builder) {
        builder.registerOwnModOptions()
                .addPage(
                        builder.createOptionPage()
                                .setName(Component.translatable("config.dragit.page.chat"))
                                .addOptionGroup(
                                        builder.createOptionGroup()
                                                .addOption(
                                                        builder.createIntegerOption(Identifier.fromNamespaceAndPath("dragit", "chat_width"))
                                                                .setName(Component.translatable("config.dragit.chat_width.name"))
                                                                .setTooltip(Component.translatable("config.dragit.chat_width.tooltip"))
                                                                .setRange(4, 128, 4)
                                                                .setValueFormatter(value -> Component.literal(String.valueOf(value)))
                                                                .setStorageHandler(this.storageHandler)
                                                                .setBinding(DragClientConfig.INSTANCE::setChatMaxWidth, DragClientConfig.INSTANCE::getChatMaxWidth)
                                                                .setDefaultValue(40)
                                                )
                                                .addOption(
                                                        builder.createIntegerOption(Identifier.fromNamespaceAndPath("dragit", "chat_height"))
                                                                .setName(Component.translatable("config.dragit.chat_height.name"))
                                                                .setTooltip(Component.translatable("config.dragit.chat_height.tooltip"))
                                                                .setRange(4, 128, 4)
                                                                .setValueFormatter(value -> Component.literal(String.valueOf(value)))
                                                                .setStorageHandler(this.storageHandler)
                                                                .setBinding(DragClientConfig.INSTANCE::setChatMaxHeight, DragClientConfig.INSTANCE::getChatMaxHeight)
                                                                .setDefaultValue(20)
                                                )
                                                .addOption(
                                                        builder.createIntegerOption(Identifier.fromNamespaceAndPath("dragit", "chat_pixels"))
                                                                .setName(Component.translatable("config.dragit.chat_pixels.name"))
                                                                .setTooltip(Component.translatable("config.dragit.chat_pixels.tooltip"))
                                                                .setRange(512, 8192, 256)
                                                                .setValueFormatter(value -> Component.literal(String.valueOf(value)))
                                                                .setStorageHandler(this.storageHandler)
                                                                .setImpact(OptionImpact.MEDIUM)
                                                                .setBinding(DragClientConfig.INSTANCE::setChatMaxPixels, DragClientConfig.INSTANCE::getChatMaxPixels)
                                                                .setDefaultValue(2048)
                                                )
                                )

                                .addOptionGroup(
                                        builder.createOptionGroup()
                                                .addOption(
                                                        builder.createIntegerOption(Identifier.fromNamespaceAndPath("dragit", "painting_pixels"))
                                                                .setName(Component.translatable("config.dragit.painting_pixels.name"))
                                                                .setTooltip(Component.translatable("config.dragit.painting_pixels.tooltip"))
                                                                .setRange(512, 8192, 256)
                                                                .setValueFormatter(value -> Component.literal(String.valueOf(value)))
                                                                .setStorageHandler(this.storageHandler)
                                                                .setImpact(OptionImpact.MEDIUM)
                                                                .setBinding(DragClientConfig.INSTANCE::setPaintingMaxPixels, DragClientConfig.INSTANCE::getPaintingMaxPixels)
                                                                .setDefaultValue(2048)
                                                )
                                )

                                .addOptionGroup(
                                        builder.createOptionGroup()
                                                .addOption(
                                                        builder.createIntegerOption(Identifier.fromNamespaceAndPath("dragit", "item_frame_pixels"))
                                                                .setName(Component.translatable("config.dragit.item_frame_pixels.name"))
                                                                .setTooltip(Component.translatable("config.dragit.item_frame_pixels.tooltip"))
                                                                .setRange(512, 8192, 256)
                                                                .setValueFormatter(value -> Component.literal(String.valueOf(value)))
                                                                .setStorageHandler(this.storageHandler)
                                                                .setImpact(OptionImpact.MEDIUM)
                                                                .setBinding(DragClientConfig.INSTANCE::setItemFrameMaxPixels, DragClientConfig.INSTANCE::getItemFrameMaxPixels)
                                                                .setDefaultValue(2048)
                                                )
                                )
                                .addOptionGroup(
                                        builder.createOptionGroup()
                                                .addOption(
                                                        builder.createIntegerOption(Identifier.fromNamespaceAndPath("dragit", "cape_pixels"))
                                                                .setName(Component.translatable("config.dragit.cape_pixels.name"))
                                                                .setTooltip(Component.translatable("config.dragit.cape_pixels.tooltip"))
                                                                .setRange(512, 8192, 256)
                                                                .setValueFormatter(value -> Component.literal(String.valueOf(value)))
                                                                .setStorageHandler(this.storageHandler)
                                                                .setImpact(OptionImpact.MEDIUM)
                                                                .setBinding(DragClientConfig.INSTANCE::setCapeMaxPixels, DragClientConfig.INSTANCE::getCapeMaxPixels)
                                                                .setDefaultValue(2048)
                                                )
                                )
                );
    }
}
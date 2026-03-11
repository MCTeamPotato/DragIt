package me.kall.dragit.config.integration;

import com.google.common.collect.ImmutableList;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import me.jellysquid.mods.sodium.client.gui.options.OptionGroup;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpact;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlValueFormatter;
import me.jellysquid.mods.sodium.client.gui.options.control.SliderControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.SodiumOptionsStorage;
import me.kall.dragit.DragIt;
import me.kall.dragit.config.DragClientConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.embeddedt.embeddium.api.OptionGUIConstructionEvent;
import org.embeddedt.embeddium.client.gui.options.OptionIdentifier;
import org.jetbrains.annotations.NotNull;

public class SodiumIntegration {
    private static final SodiumOptionsStorage sodiumOpts = new SodiumOptionsStorage();

    private static void addConfigPage(@NotNull OptionGUIConstructionEvent event) {
        OptionImpl<SodiumGameOptions, Integer> chatWidth = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "chat_width"))
                .setName(Component.translatable("config.dragit.chat_width.name"))
                .setTooltip(Component.translatable("config.dragit.chat_width.tooltip"))
                .setControl(option -> new SliderControl(option, 4, 128, 4, ControlValueFormatter.number()))
                .setBinding((options, value) -> DragClientConfig.INSTANCE.setChatMaxWidth(value), sodiumGameOptions -> DragClientConfig.INSTANCE.getChatMaxWidth())
                .build();

        OptionImpl<SodiumGameOptions, Integer> chatHeight = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "chat_height"))
                .setName(Component.translatable("config.dragit.chat_height.name"))
                .setTooltip(Component.translatable("config.dragit.chat_height.tooltip"))
                .setControl(option -> new SliderControl(option, 4, 128, 4, ControlValueFormatter.number()))
                .setBinding((options, value) -> DragClientConfig.INSTANCE.setChatMaxHeight(value), sodiumGameOptions -> DragClientConfig.INSTANCE.getChatMaxHeight())
                .build();

        OptionImpl<SodiumGameOptions, Integer> chatPixels = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "chat_pixels"))
                .setName(Component.translatable("config.dragit.chat_pixels.name"))
                .setTooltip(Component.translatable("config.dragit.chat_pixels.tooltip"))
                .setControl(option -> new SliderControl(option, 512, 8192, 256, ControlValueFormatter.number()))
                .setBinding((options, value) -> DragClientConfig.INSTANCE.setChatMaxPixels(value), sodiumGameOptions -> DragClientConfig.INSTANCE.getChatMaxPixels())
                .setImpact(OptionImpact.MEDIUM)
                .build();

        OptionGroup chatGroup = OptionGroup.createBuilder().setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "chat_group")).add(chatWidth).add(chatHeight).add(chatPixels).build();


        OptionImpl<SodiumGameOptions, Integer> paintingPixels = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "painting_pixels"))
                .setName(Component.translatable("config.dragit.painting_pixels.name"))
                .setTooltip(Component.translatable("config.dragit.painting_pixels.tooltip"))
                .setControl(option -> new SliderControl(option, 512, 8192, 256, ControlValueFormatter.number()))
                .setBinding((options, value) -> DragClientConfig.INSTANCE.setPaintingMaxPixels(value), sodiumGameOptions -> DragClientConfig.INSTANCE.getPaintingMaxPixels())
                .setImpact(OptionImpact.MEDIUM)
                .build();

        OptionGroup paintingGroup = OptionGroup.createBuilder().setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "painting_group")).add(paintingPixels).build();

        OptionImpl<SodiumGameOptions, Integer> itemFramePixels = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "item_frame_pixels"))
                .setName(Component.translatable("config.dragit.item_frame_pixels.name"))
                .setTooltip(Component.translatable("config.dragit.item_frame_pixels.tooltip"))
                .setControl(option -> new SliderControl(option, 512, 8192, 256, ControlValueFormatter.number()))
                .setBinding((options, value) -> DragClientConfig.INSTANCE.setItemFrameMaxPixels(value), sodiumGameOptions -> DragClientConfig.INSTANCE.getItemFrameMaxPixels())
                .setImpact(OptionImpact.MEDIUM)
                .build();

        OptionGroup itemFrameGroup = OptionGroup.createBuilder().setId(ResourceLocation.fromNamespaceAndPath(DragIt.MOD_ID, "item_frame_group")).add(itemFramePixels).build();

        ImmutableList<OptionGroup> groups = ImmutableList.of(chatGroup, paintingGroup, itemFrameGroup);
        event.addPage(new OptionPage(OptionIdentifier.create(DragIt.MOD_ID, "client_config"), Component.translatable("config.dragit.page.chat"), groups));
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(SodiumIntegration::addConfigPage);
    }
}
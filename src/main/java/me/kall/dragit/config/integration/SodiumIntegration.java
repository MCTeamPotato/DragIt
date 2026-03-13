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
import me.kall.dragit.config.DragClientConfig;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

public class SodiumIntegration {
    private static final SodiumOptionsStorage sodiumOpts = new SodiumOptionsStorage();

    public static @NotNull OptionPage addConfigPage() {
        OptionImpl<SodiumGameOptions, Integer> chatWidth = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setName(new TranslatableComponent("config.dragit.chat_width.name"))
                .setTooltip(new TranslatableComponent("config.dragit.chat_width.tooltip"))
                .setControl(option -> new SliderControl(option, 4, 128, 4, ControlValueFormatter.number()))
                .setBinding((options, value) -> DragClientConfig.INSTANCE.setChatMaxWidth(value), sodiumGameOptions -> DragClientConfig.INSTANCE.getChatMaxWidth())
                .build();

        OptionImpl<SodiumGameOptions, Integer> chatHeight = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setName(new TranslatableComponent("config.dragit.chat_height.name"))
                .setTooltip(new TranslatableComponent("config.dragit.chat_height.tooltip"))
                .setControl(option -> new SliderControl(option, 4, 128, 4, ControlValueFormatter.number()))
                .setBinding((options, value) -> DragClientConfig.INSTANCE.setChatMaxHeight(value), sodiumGameOptions -> DragClientConfig.INSTANCE.getChatMaxHeight())
                .build();

        OptionImpl<SodiumGameOptions, Integer> chatPixels = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setName(new TranslatableComponent("config.dragit.chat_pixels.name"))
                .setTooltip(new TranslatableComponent("config.dragit.chat_pixels.tooltip"))
                .setControl(option -> new SliderControl(option, 512, 8192, 256, ControlValueFormatter.number()))
                .setBinding((options, value) -> DragClientConfig.INSTANCE.setChatMaxPixels(value), sodiumGameOptions -> DragClientConfig.INSTANCE.getChatMaxPixels())
                .setImpact(OptionImpact.MEDIUM)
                .build();

        OptionGroup chatGroup = OptionGroup.createBuilder().add(chatWidth).add(chatHeight).add(chatPixels).build();


        OptionImpl<SodiumGameOptions, Integer> paintingPixels = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setName(new TranslatableComponent("config.dragit.painting_pixels.name"))
                .setTooltip(new TranslatableComponent("config.dragit.painting_pixels.tooltip"))
                .setControl(option -> new SliderControl(option, 512, 8192, 256, ControlValueFormatter.number()))
                .setBinding((options, value) -> DragClientConfig.INSTANCE.setPaintingMaxPixels(value), sodiumGameOptions -> DragClientConfig.INSTANCE.getPaintingMaxPixels())
                .setImpact(OptionImpact.MEDIUM)
                .build();

        OptionGroup paintingGroup = OptionGroup.createBuilder().add(paintingPixels).build();

        OptionImpl<SodiumGameOptions, Integer> itemFramePixels = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setName(new TranslatableComponent("config.dragit.item_frame_pixels.name"))
                .setTooltip(new TranslatableComponent("config.dragit.item_frame_pixels.tooltip"))
                .setControl(option -> new SliderControl(option, 512, 8192, 256, ControlValueFormatter.number()))
                .setBinding((options, value) -> DragClientConfig.INSTANCE.setItemFrameMaxPixels(value), sodiumGameOptions -> DragClientConfig.INSTANCE.getItemFrameMaxPixels())
                .setImpact(OptionImpact.MEDIUM)
                .build();

        OptionGroup itemFrameGroup = OptionGroup.createBuilder().add(itemFramePixels).build();

        OptionImpl<SodiumGameOptions, Integer> capePixels = OptionImpl.createBuilder(Integer.TYPE, sodiumOpts)
                .setName(new TranslatableComponent("config.dragit.cape_pixels.name"))
                .setTooltip(new TranslatableComponent("config.dragit.cape_pixels.tooltip"))
                .setControl(option -> new SliderControl(option, 512, 8192, 256, ControlValueFormatter.number()))
                .setBinding((options, value) -> DragClientConfig.INSTANCE.setCapeMaxPixels(value), sodiumGameOptions -> DragClientConfig.INSTANCE.getCapeMaxPixels())
                .setImpact(OptionImpact.MEDIUM)
                .build();

        OptionGroup capeGroup = OptionGroup.createBuilder().add(capePixels).build();

        ImmutableList<OptionGroup> groups = ImmutableList.of(chatGroup, paintingGroup, itemFrameGroup, capeGroup);
        return new OptionPage(new TranslatableComponent("config.dragit.page.chat"), groups);
    }
}
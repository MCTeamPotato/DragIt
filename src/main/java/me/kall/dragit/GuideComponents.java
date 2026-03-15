package me.kall.dragit;

import net.minecraft.network.chat.Component;

public final class GuideComponents {
    public static final Component PAPER = Component.translatable("paper.dragit.guide");

    private static final Component SKIN = Component.translatable("guide.dragit.skin");
    private static final Component CAPE = Component.translatable("guide.dragit.cape");
    private static final Component CHAT = Component.translatable("guide.dragit.chat");
    private static final Component ITEM_FRAME = Component.translatable("guide.dragit.item_frame");
    private static final Component PAINTING = Component.translatable("guide.dragit.painting");

    private static final Component VIDEO = Component.translatable("guide.dragit.naruto_loading");
    private static final Component NOTE = Component.translatable("guide.dragit.naruto_loading.note");

    public static final Component[] GUIDE = {SKIN, CAPE, CHAT, ITEM_FRAME, PAINTING, VIDEO, NOTE};
}

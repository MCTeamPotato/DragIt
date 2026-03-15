package me.kall.dragit;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public final class GuideComponents {
    public static final Component PAPER = new TranslatableComponent("paper.dragit.guide");

    private static final Component SKIN = new TranslatableComponent("guide.dragit.skin");
    private static final Component CAPE = new TranslatableComponent("guide.dragit.cape");
    private static final Component CHAT = new TranslatableComponent("guide.dragit.chat");
    private static final Component ITEM_FRAME = new TranslatableComponent("guide.dragit.item_frame");
    private static final Component PAINTING = new TranslatableComponent("guide.dragit.painting");

    private static final Component VIDEO = new TranslatableComponent("guide.dragit.naruto_loading");
    private static final Component NOTE = new TranslatableComponent("guide.dragit.naruto_loading.note");

    public static final Component[] GUIDE = {SKIN, CAPE, CHAT, ITEM_FRAME, PAINTING, VIDEO, NOTE};
}
